package com.nightmarecore.item;

import com.nightmarecore.util.CurioUtil;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * A reusable scouting tool. It only reveals hostile entities, so it does not
 * accidentally turn passive farms into glowing lag sources.
 */
public class NightmarePrismItem extends Item {

    private static final int RADIUS = 24;
    private static final int GLOWING_TICKS = 20 * 20;

    public NightmarePrismItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }
        if (!CurioUtil.hasNightmareCore(serverPlayer)) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.nightmare_core.requires_core")
                            .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }
        if (serverPlayer.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                serverPlayer.getBoundingBox().inflate(RADIUS),
                entity -> entity != serverPlayer
                        && entity.isAlive()
                        && entity instanceof Enemy);

        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOWING_TICKS, 0, false, false));
        }

        ServerLevel serverLevel = serverPlayer.serverLevel();
        if (targets.isEmpty()) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.nightmare_core.prism.none"), true);
            serverPlayer.getCooldowns().addCooldown(this, 20 * 5);
        } else {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.nightmare_core.prism.revealed", targets.size()), true);
            serverPlayer.getCooldowns().addCooldown(this, 20 * 60);
        }

        serverLevel.playSound(null,
                serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.65F, 1.55F);
        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                serverPlayer.getX(), serverPlayer.getY() + 1.0D, serverPlayer.getZ(),
                26, 1.1D, 0.85D, 1.1D, 0.02D);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.nightmare_core.requires_core")
                .withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("tooltip.nightmare_core.prism.use")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.nightmare_core.prism.cooldown")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.nightmare_core.prism.flavor")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }
}
