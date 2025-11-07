package com.example.contamination.registry;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.bus.api.IEventBus;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("contamination");

    public static final DeferredItem<Item> INCOMPLETE_LUGOLS_IODINE =
        ITEMS.registerItem("incomplete_lugols_iodine", props -> new Item(props.stacksTo(16)));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
