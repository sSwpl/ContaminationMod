package com.example.contamination;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import com.example.contamination.registry.ModBrewing;
import com.example.contamination.registry.ModItems;

@Mod(ContaminationMod.MODID)
public class ContaminationMod {
    public static final String MODID = "contamination";

    public ContaminationMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // KLUCZOWE: podpięcie DeferredRegister dla itemów
        ModItems.register(modBus);

        // Jeżeli ModBrewing używa @EventBusSubscriber(bus = Bus.MOD),
        // nie trzeba nic tu dodawać. Jeśli nie – można też:
        // modBus.addListener(ModBrewing::onCommonSetup);
    }
}