package com.example.contamination.registry;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    // Rejestr pod modId = "contamination"
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, "contamination");

    // Półprodukt craftowany w stole
    public static final RegistryObject<Item> UNCOMPLETE_LUGOLS_IODINE = ITEMS.register(
            "uncomplete_lugols_iodine",
            () -> new Item(new Item.Properties().stacksTo(16))
    );

    // Finalny płyn (otrzymywany w Brewing Stand)
    public static final RegistryObject<Item> LUGOLS_IODINE = ITEMS.register(
            "lugols_iodine",
            () -> new Item(new Item.Properties().stacksTo(16))
    );

    // Rejestracja DeferredRegister na MOD Event Bus – wywoływana z konstruktora moda
    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}