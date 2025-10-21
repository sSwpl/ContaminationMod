package com.example.contamination.compat.jei;

import com.example.contamination.ContaminationMod;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.RecipeIngredientRole; // <-- poprawny import dla JEI 15
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BrewingCategory implements IRecipeCategory<BrewingRecipe> {
    private final IDrawable background;
    private final IDrawable icon;

    public BrewingCategory(IGuiHelper guiHelper) {
        // Proste tło 110x40 i ikona statywu
        this.background = guiHelper.createBlankDrawable(110, 40);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Items.BREWING_STAND));
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
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BrewingRecipe recipe, IFocusGroup focuses) {
        // Lewo: INCOMPLETE (butelka/baza), środek: katalizator, prawo: wynik
        builder.addSlot(RecipeIngredientRole.INPUT, 6, 11)
                .addItemStack(recipe.input());
        builder.addSlot(RecipeIngredientRole.CATALYST, 46, 11)
                .addItemStack(recipe.ingredient());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 86, 11)
                .addItemStack(recipe.output());
    }
}