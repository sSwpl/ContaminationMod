package com.example.contamination.compat.jei;

import com.example.contamination.ContaminationMod;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BrewingCategory implements IRecipeCategory<BrewingRecipe> {
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slotDrawable;

    // Vanilla brewing stand GUI texture
    private static final ResourceLocation BREWING_STAND_LOCATION = 
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/brewing_stand.png");

    public BrewingCategory(IGuiHelper guiHelper) {
        // Use a portion of the vanilla brewing stand texture for a more authentic look
        // Background area showing the brewing slots (width: 64, height: 60)
        this.background = guiHelper.createDrawable(BREWING_STAND_LOCATION, 46, 16, 64, 60);
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
        // Position slots to match the brewing stand interface
        // Input bottle (bottom center)
        builder.addSlot(RecipeIngredientRole.INPUT, 24, 36)
                .addItemStack(recipe.input())
                .setBackground(slotDrawable, -1, -1);
        
        // Ingredient (top center - where you put the brewing ingredient)
        builder.addSlot(RecipeIngredientRole.CATALYST, 24, 1)
                .addItemStack(recipe.ingredient())
                .setBackground(slotDrawable, -1, -1);
        
        // Output (right side)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 42, 36)
                .addItemStack(recipe.output())
                .setBackground(slotDrawable, -1, -1);
    }
}