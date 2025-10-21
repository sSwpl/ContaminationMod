package com.example.contamination.compat.jei;

import net.minecraft.world.item.ItemStack;

public class BrewingRecipe {
    private final ItemStack input;
    private final ItemStack ingredient;
    private final ItemStack output;

    public BrewingRecipe(ItemStack input, ItemStack ingredient, ItemStack output) {
        this.input = input;
        this.ingredient = ingredient;
        this.output = output;
    }

    public ItemStack input() { return input; }
    public ItemStack ingredient() { return ingredient; }
    public ItemStack output() { return output; }
}