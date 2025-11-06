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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.bus.api.IEventBus;

import com.example.contamination.registry.ModItems;

@Mod(ContaminationMod.MODID)
public class ContaminationMod {
    public static final String MODID = "contamination";

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredItem<Item> LUGOL = ITEMS.registerItem("lugol",
            properties -> new LugolItem(properties.stacksTo(16)));

    // runtime overrides (set by commands) — not persisted to config file by these commands unless saved
    private static volatile int overrideRadius = -1; // blocks
    private static volatile int overrideProtectionSeconds = -1; // seconds

    // DAMAGE: Progressive damage system - increases over time in contamination zone
    private static final float BASE_DAMAGE_PER_SECOND = 0.5f;  // Starting damage
    private static final float MAX_DAMAGE_PER_SECOND = 4.0f;   // Maximum damage cap
    private static final int TICKS_TO_MAX_DAMAGE = 20 * 30;    // 30 seconds to reach max damage
    
    // VISUAL: Boundary visualization system
    private static final int BOUNDARY_VISIBILITY_RANGE = 50;   // Blocks from boundary where particles appear
    private static final int PARTICLE_SPAWN_INTERVAL = 10;     // Ticks between particle spawns
    
    // Fallback protection seconds if config missing
    private static final int FALLBACK_PROTECTION_SECONDS = 60;

    // mapa graczUUID -> ServerBossEvent
    private static final Map<UUID, ServerBossEvent> BOSS_BARS = new ConcurrentHashMap<>();

    public ContaminationMod(IEventBus modBus, ModContainer modContainer) {
        // register config
        modContainer.registerConfig(ModConfig.Type.COMMON, ContaminationConfig.SPEC);

        // Rejestr istniejących itemów (w tym 'lugol')
        ITEMS.register(modBus);

        // Rejestr nowych itemów (półprodukt do warzenia)
        ModItems.register(modBus);

        NeoForge.EVENT_BUS.register(this);

        // register command listener
        NeoForge.EVENT_BUS.register(new ContaminationCommands());
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
    
    // Spawn particles to visualize the contamination zone boundary
    private void spawnBoundaryParticles(ServerLevel level, Player player, int radius) {
        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();
        
        // Determine which boundaries to show based on player position
        boolean showNorthBoundary = Math.abs(playerZ + radius) < BOUNDARY_VISIBILITY_RANGE;
        boolean showSouthBoundary = Math.abs(playerZ - radius) < BOUNDARY_VISIBILITY_RANGE;
        boolean showWestBoundary = Math.abs(playerX + radius) < BOUNDARY_VISIBILITY_RANGE;
        boolean showEastBoundary = Math.abs(playerX - radius) < BOUNDARY_VISIBILITY_RANGE;
        
        // Spawn particles along visible boundaries
        int particleCount = 3; // Particles per spawn call
        
        if (showNorthBoundary) {
            spawnParticlesAlongLine(level, -radius, radius, -radius, playerY, particleCount);
        }
        if (showSouthBoundary) {
            spawnParticlesAlongLine(level, -radius, radius, radius, playerY, particleCount);
        }
        if (showWestBoundary) {
            spawnParticlesAlongLine(level, -radius, playerY, -radius, radius, particleCount);
        }
        if (showEastBoundary) {
            spawnParticlesAlongLine(level, radius, playerY, -radius, radius, particleCount);
        }
    }
    
    // Helper method to spawn particles along a line
    private void spawnParticlesAlongLine(ServerLevel level, double x, double y, double z1, double z2, int count) {
        for (int i = 0; i < count; i++) {
            double randomOffset = level.random.nextDouble() * (z2 - z1) + z1;
            double particleY = y + level.random.nextDouble() * 3.0 - 1.0; // Random Y offset ±1 block
            
            // Determine if this is a vertical or horizontal line
            if (Math.abs(z2 - z1) > Math.abs(x)) {
                // Horizontal line (varying Z)
                level.sendParticles(
                    ParticleTypes.WARPED_SPORE,
                    x, particleY, randomOffset,
                    1, 0.0, 0.0, 0.0, 0.0
                );
            } else {
                // Vertical line (varying X)
                level.sendParticles(
                    ParticleTypes.WARPED_SPORE,
                    randomOffset, particleY, z1,
                    1, 0.0, 0.0, 0.0, 0.0
                );
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

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
        int radius = getRadius();
        
        // Calculate distance to nearest boundary
        double distToXBoundary = Math.abs(Math.abs(x) - radius);
        double distToZBoundary = Math.abs(Math.abs(z) - radius);
        double distToBoundary = Math.min(distToXBoundary, distToZBoundary);
        
        // Spawn particles if player is near boundary (every PARTICLE_SPAWN_INTERVAL ticks)
        if (distToBoundary < BOUNDARY_VISIBILITY_RANGE && player.tickCount % PARTICLE_SPAWN_INTERVAL == 0) {
            spawnBoundaryParticles(serverLevel, player, radius);
        }

        if (Math.abs(x) > radius || Math.abs(z) > radius) {
            // Player is in contamination zone
            
            // Track time spent in contamination zone
            int ticksInZone = pd.contains("contamination_ticks_in_zone") ? pd.getInt("contamination_ticks_in_zone") : 0;
            ticksInZone++;
            pd.putInt("contamination_ticks_in_zone", ticksInZone);
            
            // Calculate progressive damage based on time in zone
            // Damage increases linearly from BASE_DAMAGE to MAX_DAMAGE over TICKS_TO_MAX_DAMAGE
            float damageProgress = Math.min(1.0f, (float) ticksInZone / TICKS_TO_MAX_DAMAGE);
            float currentDamagePerSecond = BASE_DAMAGE_PER_SECOND + (MAX_DAMAGE_PER_SECOND - BASE_DAMAGE_PER_SECOND) * damageProgress;
            float damagePerTick = currentDamagePerSecond / 20.0f;
            
            // Accumulate damage (fractional damage is stored until it reaches 1.0)
            double acc = pd.contains("contamination_damage_acc") ? pd.getDouble("contamination_damage_acc") : 0.0;
            acc += damagePerTick;
            
            if (acc >= 1.0) {
                int apply = (int) Math.floor(acc);
                pd.putDouble("contamination_damage_acc", acc - apply);
                player.hurt(serverLevel.damageSources().generic(), (float) apply);
            } else {
                pd.putDouble("contamination_damage_acc", acc);
            }
        } else {
            // Player is in safe zone - reset contamination tracking
            if (pd.contains("contamination_damage_acc")) pd.remove("contamination_damage_acc");
            if (pd.contains("contamination_ticks_in_zone")) pd.remove("contamination_ticks_in_zone");
            if (player instanceof ServerPlayer serverPlayer) {
                if (BOSS_BARS.containsKey(player.getUUID())) removeBossBarForPlayer(serverPlayer);
            }
        }
    }
}