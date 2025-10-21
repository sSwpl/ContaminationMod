package com.example.contamination;

import net.minecraftforge.common.ForgeConfigSpec;

public class ContaminationConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue BARRIER_RADIUS;
    public static final ForgeConfigSpec.IntValue PROTECTION_SECONDS;

    // Punkt 6: Konfiguracja warzenia
    public static final ForgeConfigSpec.BooleanValue ENABLE_LUGOL_BREWING;
    public static final ForgeConfigSpec.ConfigValue<String> BREWING_CATALYST;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Contamination mod settings").push("general");

        BARRIER_RADIUS = builder
                .comment("Barrier radius in blocks (distance from origin where contamination begins)")
                .defineInRange("barrierRadius", 1000, 1, 3_000_000);

        PROTECTION_SECONDS = builder
                .comment("Lugol protection duration in seconds (default used when drinking)")
                .defineInRange("protectionSeconds", 60, 1, 60 * 60 * 24);

        builder.pop();

        builder.comment("Brewing settings").push("brewing");

        ENABLE_LUGOL_BREWING = builder
                .comment("Enable registration of the Lugol brewing recipe")
                .define("enableBrewing", true);

        BREWING_CATALYST = builder
                .comment("Item ID used as the catalyst for brewing Lugol (e.g. minecraft:ghast_tear)")
                .define("brewingCatalyst", "minecraft:ghast_tear");

        builder.pop();

        SPEC = builder.build();
    }
}