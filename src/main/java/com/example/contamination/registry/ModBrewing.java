package com.example.contamination.registry;

import com.example.contamination.ContaminationMod;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = "contamination", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBrewing {
    @SubscribeEvent
    public static void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() ->
                BrewingRecipeRegistry.addRecipe(
                        Ingredient.of(ModItems.INCOMPLETE_LUGOLS_IODINE.get()),
                        Ingredient.of(Items.GHAST_TEAR),
                        new ItemStack(ContaminationMod.LUGOL.get())
                )
        );
    }
}