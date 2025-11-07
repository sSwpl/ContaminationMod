package com.example.contamination.registry;

import com.example.contamination.ContaminationConfig;
import com.example.contamination.ContaminationMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

@EventBusSubscriber(modid = "contamination", bus = EventBusSubscriber.Bus.GAME)
public class ModBrewing {
    @SubscribeEvent
    public static void onRegisterBrewingRecipes(final RegisterBrewingRecipesEvent event) {
        if (!ContaminationConfig.ENABLE_LUGOL_BREWING.get()) {
            return;
        }

        // Get the catalyst item from config
        String catalystId = ContaminationConfig.BREWING_CATALYST.get();
        var catalystItem = Items.GHAST_TEAR; // Default to ghast tear
        
        // Try to parse the config value
        try {
            var parts = catalystId.split(":");
            if (parts.length == 2) {
                var namespace = parts[0];
                var path = parts[1];
                var foundItem = BuiltInRegistries.ITEM.get(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(namespace, path)
                );
                if (foundItem != Items.AIR) {
                    catalystItem = foundItem;
                }
            }
        } catch (Exception e) {
            // Use default if parsing fails
        }

        // Register the brewing recipe: INCOMPLETE_LUGOLS_IODINE + catalyst -> LUGOL
        // Using NeoForge's custom BrewingRecipe for non-potion items
        event.getBuilder().addRecipe(
            new BrewingRecipe(
                Ingredient.of(ModItems.INCOMPLETE_LUGOLS_IODINE.get()),
                Ingredient.of(catalystItem),
                new ItemStack(ContaminationMod.LUGOL.get())
            )
        );
    }
}