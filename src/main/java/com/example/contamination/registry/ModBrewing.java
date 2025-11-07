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

            // TODO: NeoForge 1.21.1 brewing recipe registration
            // The PotionBrewing API has changed significantly in 1.21.1
            // Brewing recipes now need to be registered through data packs or a different mechanism
            // For now, brewing is disabled until the proper API is implemented
            // The item can still be crafted through the crafting table recipe
        });
    }
}