package com.example.contamination;

import net.minecraft.ChatFormatting;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.List;

public class LugolItem extends Item {

    public LugolItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        // Czas „picie” jak vanillowa mikstura
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // Standardowy flow picia — zaczynamy używanie i kończymy w finishUsingItem
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            if (!level.isClientSide) {
                int protectionTicks = ContaminationMod.getProtectionTicks();
                player.getPersistentData().putInt("contamination_protection", protectionTicks);
                player.getPersistentData().putInt("contamination_protection_max", protectionTicks);

                if (player instanceof ServerPlayer serverPlayer) {
                    ContaminationMod.createBossBarForPlayer(serverPlayer, protectionTicks);
                }

                if (!player.isCreative()) {
                    stack.shrink(1);
                }
            }

            // Dźwięk picia i statystyka
            level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        int seconds = ContaminationMod.getProtectionSeconds();
        tooltip.add(Component.translatable("item.contamination.lugol.tooltip", seconds).withStyle(ChatFormatting.GRAY));
    }
}