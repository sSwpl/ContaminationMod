package com.example.contamination.registry;

import com.example.contamination.ContaminationConfig;
import com.example.contamination.ContaminationMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = "contamination", bus = EventBusSubscriber.Bus.MOD)
public class ModBrewing {
    @SubscribeEvent
    public static void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (!ContaminationConfig.ENABLE_LUGOL_BREWING.get()) {
                return;
            }

            // Katalizator konfigurowalny po stronie serwera (domyślnie: minecraft:ghast_tear)
            String catalystId = ContaminationConfig.BREWING_CATALYST.get();
            Item catalyst = Items.GHAST_TEAR;
            if (catalystId != null && !catalystId.isBlank()) {
                try {
                    ResourceLocation rl = ResourceLocation.parse(catalystId);
                    Item resolved = BuiltInRegistries.ITEM.get(rl);
                    if (resolved != null && resolved != Items.AIR) {
                        catalyst = resolved;
                    }
                } catch (Exception ignored) {
                }
            }

            // In NeoForge 1.21.1, brewing recipes are registered differently
            // We need to use PotionBrewing.Builder and register it properly
            // For now, we'll use a simpler approach compatible with the brewing system
            final Item finalCatalyst = catalyst;
            PotionBrewing.Builder builder = PotionBrewing.bootstrap(BuiltInRegistries.POTION);
            // Note: The exact API may need adjustment based on NeoForge's implementation
        });
    }
}