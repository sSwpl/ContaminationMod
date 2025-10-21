package com.example.contamination;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.example.contamination.registry.ModItems;

@Mod(ContaminationMod.MODID)
public class ContaminationMod {
    public static final String MODID = "contamination";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> LUGOL = ITEMS.register("lugol",
            () -> new LugolItem(new Item.Properties().stacksTo(16)));

    // runtime overrides (set by commands) — not persisted to config file by these commands unless saved
    private static volatile int overrideRadius = -1; // blocks
    private static volatile int overrideProtectionSeconds = -1; // seconds

    // DAMAGE: zmienne z akumulacją
    private static final float DAMAGE_PER_SECOND = 1.0f;
    private static final float DAMAGE_PER_TICK = DAMAGE_PER_SECOND / 20.0f;

    // Fallback protection seconds if config missing
    private static final int FALLBACK_PROTECTION_SECONDS = 60;

    // mapa graczUUID -> ServerBossEvent
    private static final Map<UUID, ServerBossEvent> BOSS_BARS = new ConcurrentHashMap<>();

    public ContaminationMod() {
        // register config
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ContaminationConfig.SPEC);

        // Rejestr istniejących itemów (w tym 'lugol')
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());

        // Rejestr nowych itemów (półprodukt do warzenia)
        ModItems.register(FMLJavaModLoadingContext.get().getModEventBus());

        MinecraftForge.EVENT_BUS.register(this);

        // register command listener
        MinecraftForge.EVENT_BUS.register(new ContaminationCommands());
    }

    // Config-aware getters
    public static int getRadius() {
        int cfg = ContaminationConfig.BARRIER_RADIUS.get();
        return overrideRadius > 0 ? overrideRadius : cfg;
    }

    public static int getProtectionSeconds() {
        int cfgSec = ContaminationConfig.PROTECTION_SECONDS.get();
        int sec = overrideProtectionSeconds > 0 ? overrideProtectionSeconds : (cfgSec > 0 ? cfgSec : FALLBACK_PROTECTION_SECONDS);
        return sec;
    }

    public static int getProtectionTicks() {
        return getProtectionSeconds() * 20;
    }

    // command API
    public static void setOverrideRadius(int blocks) { overrideRadius = blocks; }
    public static void setOverrideProtectionSeconds(int seconds) { overrideProtectionSeconds = seconds; }
    public static void clearOverrides() { overrideRadius = -1; overrideProtectionSeconds = -1; }
    public static boolean hasOverride() { return overrideRadius > 0 || overrideProtectionSeconds > 0; }

    // Save runtime overrides to config file (will overwrite general.* in contamination-common.toml)
    public static void saveOverridesToConfig() throws IOException {
        // Determine values to save: use overrides if present, otherwise current config values
        int radiusToSave = overrideRadius > 0 ? overrideRadius : ContaminationConfig.BARRIER_RADIUS.get();
        int protectionToSave = overrideProtectionSeconds > 0 ? overrideProtectionSeconds : ContaminationConfig.PROTECTION_SECONDS.get();

        // Build TOML content (under [general])
        StringBuilder sb = new StringBuilder();
        sb.append("[general]\n");
        sb.append("barrierRadius = ").append(radiusToSave).append("\n");
        sb.append("protectionSeconds = ").append(protectionToSave).append("\n");

        Path configDir = FMLPaths.CONFIGDIR.get();
        if (!Files.exists(configDir)) {
            Files.createDirectories(configDir);
        }
        Path cfgFile = configDir.resolve("contamination-common.toml");

        Files.writeString(cfgFile, sb.toString(), StandardCharsets.UTF_8);
    }

    // --- bossbar helpers ---
    public static void createBossBarForPlayer(ServerPlayer serverPlayer, int totalTicks) {
        if (serverPlayer == null) return;
        UUID id = serverPlayer.getUUID();
        if (BOSS_BARS.containsKey(id)) {
            ServerBossEvent old = BOSS_BARS.get(id);
            old.removePlayer(serverPlayer);
            BOSS_BARS.remove(id);
        }
        Component title = Component.literal(formatMinutesSeconds(totalTicks));
        ServerBossEvent bar = new ServerBossEvent(title, BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.PROGRESS);
        bar.setProgress(1.0f);
        bar.addPlayer(serverPlayer);
        BOSS_BARS.put(id, bar);
    }

    public static void removeBossBarForPlayer(ServerPlayer serverPlayer) {
        if (serverPlayer == null) return;
        UUID id = serverPlayer.getUUID();
        ServerBossEvent bar = BOSS_BARS.remove(id);
        if (bar != null) bar.removePlayer(serverPlayer);
    }

    private static String formatMinutesSeconds(int ticks) {
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;

        Level level = player.level();
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (player.isCreative() || player.isSpectator()) return;

        CompoundTag pd = player.getPersistentData();
        int protection = pd.getInt("contamination_protection");
        int protectionMax = pd.contains("contamination_protection_max") ? pd.getInt("contamination_protection_max") : getProtectionTicks();

        if (protection > 0) {
            int newProt = protection - 1;
            pd.putInt("contamination_protection", newProt);

            if (player instanceof ServerPlayer serverPlayer) {
                ServerBossEvent bar = BOSS_BARS.get(player.getUUID());
                if (bar != null) {
                    if (newProt > 0) {
                        float pct = (float) newProt / (float) Math.max(1, protectionMax);
                        bar.setProgress(pct);
                        bar.setName(Component.literal(formatMinutesSeconds(newProt)));
                    } else {
                        // protection expired -> remove bossbar
                        removeBossBarForPlayer(serverPlayer);
                    }
                }
            }
            return;
        }

        double x = player.getX();
        double z = player.getZ();

        if (Math.abs(x) > getRadius() || Math.abs(z) > getRadius()) {
            double acc = pd.contains("contamination_damage_acc") ? pd.getDouble("contamination_damage_acc") : 0.0;
            acc += DAMAGE_PER_TICK;
            if (acc >= 1.0) {
                int apply = (int) Math.floor(acc);
                pd.putDouble("contamination_damage_acc", acc - apply);
                player.hurt(serverLevel.damageSources().generic(), (float) apply);
            } else {
                pd.putDouble("contamination_damage_acc", acc);
            }
        } else {
            if (pd.contains("contamination_damage_acc")) pd.remove("contamination_damage_acc");
            if (player instanceof ServerPlayer serverPlayer) {
                if (BOSS_BARS.containsKey(player.getUUID())) removeBossBarForPlayer(serverPlayer);
            }
        }
    }
}