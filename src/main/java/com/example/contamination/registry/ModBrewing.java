package com.example.contamination.registry;

import com.example.contamination.ContaminationConfig;
import com.example.contamination.ContaminationMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "contamination", bus = Mod.EventBusSubscriber.Bus.MOD)
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
                    ResourceLocation rl = new ResourceLocation(catalystId);
                    Item resolved = ForgeRegistries.ITEMS.getValue(rl);
                    if (resolved != null) {
                        catalyst = resolved;
                    }
                } catch (Exception ignored) {
                }
            }

            BrewingRecipeRegistry.addRecipe(
                    Ingredient.of(ModItems.INCOMPLETE_LUGOLS_IODINE.get()),
                    Ingredient.of(catalyst),
                    new ItemStack(ContaminationMod.LUGOL.get())
            );
        });
    }
}