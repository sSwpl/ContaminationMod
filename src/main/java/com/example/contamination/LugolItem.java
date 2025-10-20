package com.example.contamination;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.server.level.ServerPlayer;

public class LugolItem extends Item {

    public LugolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            int protectionTicks = ContaminationMod.getProtectionTicks();
            player.getPersistentData().putInt("contamination_protection", protectionTicks);
            player.getPersistentData().putInt("contamination_protection_max", protectionTicks);

            player.displayClientMessage(net.minecraft.network.chat.Component.literal("You drank Lugol Iodine! Now you have resistance for " + (ContaminationMod.getProtectionSeconds()) + " seconds"), true);

            level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0F, 1.0F);

            if (!player.isCreative()) {
                itemStack.shrink(1);
            }

            player.awardStat(Stats.ITEM_USED.get(this));

            if (player instanceof ServerPlayer serverPlayer) {
                ContaminationMod.createBossBarForPlayer(serverPlayer, protectionTicks);
            }
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}