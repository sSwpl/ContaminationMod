package com.example.contamination.registry;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

public class ModItems {
    // Rejestr pod modId = "contamination"
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems("contamination");

    // Półprodukt craftowany w stole
    public static final DeferredItem<Item> INCOMPLETE_LUGOLS_IODINE = ITEMS.registerSimpleItem(
            "incomplete_lugols_iodine",
            new Item.Properties().stacksTo(16)
    );

    // Rejestracja DeferredRegister na MOD Event Bus – wywoływana z konstruktora moda
    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}