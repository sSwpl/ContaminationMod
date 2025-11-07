package com.example.contamination.registry;

import com.example.contamination.ContaminationConfig;
import com.example.contamination.ContaminationMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventBusSubscriber(modid = "contamination", bus = EventBusSubscriber.Bus.GAME)
public class ModBrewing {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModBrewing.class);

    @SubscribeEvent
    public static void onRegisterBrewingRecipes(final RegisterBrewingRecipesEvent event) {
        if (!ContaminationConfig.ENABLE_LUGOL_BREWING.get()) {
            return;
        }

        // Get the catalyst item from config
        String catalystId = ContaminationConfig.BREWING_CATALYST.get();
        Item catalystItem = Items.GHAST_TEAR; // Default to ghast tear
        
        // Try to parse the config value
        try {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(catalystId);
            if (resourceLocation != null) {
                Item foundItem = BuiltInRegistries.ITEM.get(resourceLocation);
                if (foundItem != Items.AIR) {
                    catalystItem = foundItem;
                } else {
                    LOGGER.warn("Brewing catalyst item '{}' not found in registry, using default (ghast_tear)", catalystId);
                }
            } else {
                LOGGER.warn("Invalid brewing catalyst resource location '{}', using default (ghast_tear)", catalystId);
            }
        } catch (Exception e) {
            LOGGER.warn("Error parsing brewing catalyst config '{}', using default (ghast_tear): {}", catalystId, e.getMessage());
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