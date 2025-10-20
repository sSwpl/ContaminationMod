package com.example.contamination;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ContaminationCommands {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("contamination")
                        .requires(src -> src.hasPermission(2)) // only ops by default
                        .then(Commands.literal("setradius")
                                .then(Commands.argument("blocks", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            int val = IntegerArgumentType.getInteger(ctx, "blocks");
                                            ContaminationMod.setOverrideRadius(val);
                                            CommandSourceStack src = ctx.getSource();
                                            src.sendSuccess(() -> Component.literal("Contamination radius override set to " + val + " blocks"), true);
                                            return 1;
                                        })))
                        .then(Commands.literal("setprotection")
                                .then(Commands.argument("seconds", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            int sec = IntegerArgumentType.getInteger(ctx, "seconds");
                                            ContaminationMod.setOverrideProtectionSeconds(sec);
                                            CommandSourceStack src = ctx.getSource();
                                            src.sendSuccess(() -> Component.literal("Lugol protection override set to " + sec + " seconds"), true);
                                            return 1;
                                        })))
                        .then(Commands.literal("reset")
                                .executes(ctx -> {
                                    ContaminationMod.clearOverrides();
                                    ctx.getSource().sendSuccess(() -> Component.literal("Contamination overrides cleared; using config values."), true);
                                    return 1;
                                }))
                        .then(Commands.literal("info")
                                .executes(ctx -> {
                                    CommandSourceStack src = ctx.getSource();
                                    int radius = ContaminationMod.getRadius();
                                    int protSec = ContaminationMod.getProtectionSeconds();
                                    boolean hasOverride = ContaminationMod.hasOverride();
                                    src.sendSuccess(() -> Component.literal(String.format("radius=%d, protection=%ds, override=%s", radius, protSec, hasOverride)), false);
                                    return 1;
                                }))
                        .then(Commands.literal("save")
                                .executes(ctx -> {
                                    CommandSourceStack src = ctx.getSource();
                                    try {
                                        ContaminationMod.saveOverridesToConfig();
                                        src.sendSuccess(() -> Component.literal("Contamination configuration saved to file."), true);
                                        return 1;
                                    } catch (Exception e) {
                                        src.sendFailure(Component.literal("Failed to save configuration: " + e.getMessage()));
                                        return 0;
                                    }
                                }))
        );
    }
}