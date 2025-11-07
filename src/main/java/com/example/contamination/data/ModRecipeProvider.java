package com.example.contamination.data;

import com.example.contamination.ContaminationMod;
import com.example.contamination.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeCategory;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> exporter) {
        // incomplete_lugols_iodine: 9 ingredients (glass bottle + others)
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.INCOMPLETE_LUGOLS_IODINE.get())
                .requires(Items.GLASS_BOTTLE)
                .requires(Items.DRIED_KELP_BLOCK)
                .requires(Items.AMETHYST_SHARD)
                .requires(Items.COPPER_INGOT)
                .requires(Items.REDSTONE)
                .requires(Items.GLOWSTONE_DUST)
                .requires(Items.CHARCOAL)
                .requires(Items.NETHER_WART)
                .requires(Items.BROWN_MUSHROOM)
                .unlockedBy("has_glass_bottle", has(Items.GLASS_BOTTLE))
                .save(exporter, new ResourceLocation(ContaminationMod.MODID, "incomplete_lugols_iodine"));

        // lugol: incomplete + ghast tear -> lugol
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ContaminationMod.LUGOL.get())
                .requires(ModItems.INCOMPLETE_LUGOLS_IODINE.get())
                .requires(Items.GHAST_TEAR)
                .unlockedBy("has_incomplete", has(ModItems.INCOMPLETE_LUGOLS_IODINE.get()))
                .save(exporter, new ResourceLocation(ContaminationMod.MODID, "lugol"));
    }
}