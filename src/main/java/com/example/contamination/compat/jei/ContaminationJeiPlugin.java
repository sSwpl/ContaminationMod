package com.example.contamination.compat.jei;

import com.example.contamination.ContaminationMod;
import com.example.contamination.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

@JeiPlugin
public class ContaminationJeiPlugin implements IModPlugin {
    public static final ResourceLocation UID = new ResourceLocation(ContaminationMod.MODID, "jei_plugin");
    public static final RecipeType<BrewingRecipe> LUGOL_BREWING =
            RecipeType.create(ContaminationMod.MODID, "lugol_brewing", BrewingRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new BrewingCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // JEI automatycznie pokaże crafting z JSON (incomplete_lugols_iodine.json)
        // Tutaj tylko warzenie: INCOMPLETE + Ghast Tear -> LUGOL
        ItemStack input = new ItemStack(ModItems.INCOMPLETE_LUGOLS_IODINE.get());
        ItemStack ingredient = new ItemStack(Items.GHAST_TEAR);
        ItemStack output = new ItemStack(ContaminationMod.LUGOL.get());

        registration.addRecipes(LUGOL_BREWING, List.of(new BrewingRecipe(input, ingredient, output)));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Items.BREWING_STAND), LUGOL_BREWING);
    }
}