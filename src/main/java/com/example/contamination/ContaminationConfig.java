package com.example.contamination;

import net.minecraftforge.common.ForgeConfigSpec;

public class ContaminationConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue BARRIER_RADIUS;
    public static final ForgeConfigSpec.IntValue PROTECTION_SECONDS;

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

        SPEC = builder.build();
    }
}