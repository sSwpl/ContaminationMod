package com.example.contamination.compat.jei;

import com.example.contamination.ContaminationMod;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BrewingCategory implements IRecipeCategory<BrewingRecipe> {
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slotDrawable;

    public BrewingCategory(IGuiHelper guiHelper) {
        // Create a clean background for the brewing recipe display
        this.background = guiHelper.createBlankDrawable(116, 54);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Items.BREWING_STAND));
        this.slotDrawable = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<BrewingRecipe> getRecipeType() {
        return ContaminationJeiPlugin.LUGOL_BREWING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.contamination.category.lugol_brewing");
    }

    @Override
    @SuppressWarnings("removal")
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BrewingRecipe recipe, IFocusGroup focuses) {
        // Position slots for a clean brewing stand-like layout
        // Input bottle (left side)
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 19)
                .addItemStack(recipe.input())
                .setBackground(slotDrawable, -1, -1);
        
        // Ingredient (top center - the catalyst for brewing)
        builder.addSlot(RecipeIngredientRole.CATALYST, 40, 1)
                .addItemStack(recipe.ingredient())
                .setBackground(slotDrawable, -1, -1);
        
        // Output (right side)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 81, 19)
                .addItemStack(recipe.output())
                .setBackground(slotDrawable, -1, -1);
    }
}