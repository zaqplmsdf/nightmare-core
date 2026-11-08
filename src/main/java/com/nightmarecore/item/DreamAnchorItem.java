package com.nightmarecore.item;

import com.nightmarecore.util.CurioUtil;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * A one-dimension return anchor. It remembers a safe spot and spends durability
 * when used, so it is useful without replacing proper base logistics.
 */
public class DreamAnchorItem extends Item {

    private static final String BOUND = "Bound";
    private static final String DIMENSION = "Dimension";
    private static final String X = "X";
    private static final String Y = "Y";
    private static final String Z = "Z";
    private static final String YAW = "Yaw";
    private static final String PITCH = "Pitch";

    public DreamAnchorItem(Properties properties) {
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

        if (player.isShiftKeyDown()) {
            return this.bind(serverPlayer, stack);
        }
        return this.teleport(serverPlayer, stack);
    }

    private InteractionResultHolder<ItemStack> bind(ServerPlayer player, ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(BOUND, true);
        tag.putString(DIMENSION, player.level().dimension().location().toString());
        tag.putDouble(X, player.getX());
        tag.putDouble(Y, player.getY());
        tag.putDouble(Z, player.getZ());
        tag.putFloat(YAW, player.getYRot());
        tag.putFloat(PITCH, player.getXRot());

        player.displayClientMessage(Component.translatable("message.nightmare_core.anchor.bound")
                .withStyle(ChatFormatting.LIGHT_PURPLE), true);
        player.serverLevel().playSound(null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.PLAYERS, 0.8F, 1.25F);
        player.serverLevel().sendParticles(ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1.0D, player.getZ(),
                18, 0.5D, 0.8D, 0.5D, 0.01D);
        return InteractionResultHolder.success(stack);
    }

    private InteractionResultHolder<ItemStack> teleport(ServerPlayer player, ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().getBoolean(BOUND)) {
            player.displayClientMessage(Component.translatable("message.nightmare_core.anchor.unbound")
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        CompoundTag tag = stack.getTag();
        ResourceLocation dimensionId = ResourceLocation.tryParse(tag.getString(DIMENSION));
        if (dimensionId == null) {
            player.displayClientMessage(Component.translatable("message.nightmare_core.anchor.unbound")
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }

        ResourceKey<Level> targetKey = ResourceKey.create(Registries.DIMENSION, dimensionId);
        if (!player.level().dimension().equals(targetKey)) {
            player.displayClientMessage(Component.translatable("message.nightmare_core.anchor.wrong_dimension")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        ServerLevel targetLevel = player.server.getLevel(targetKey);
        if (targetLevel == null) {
            player.displayClientMessage(Component.translatable("message.nightmare_core.anchor.wrong_dimension")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        double x = tag.getDouble(X);
        double y = tag.getDouble(Y);
        double z = tag.getDouble(Z);

        // Use the player's actual hitbox rather than only the two block cells,
        // so safe spots on slabs/carpets are not rejected.
        var targetBox = player.getBoundingBox().move(
                x - player.getX(), y - player.getY(), z - player.getZ());
        if (!targetLevel.noCollision(player, targetBox)) {
            player.displayClientMessage(Component.translatable("message.nightmare_core.anchor.blocked")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        ServerLevel sourceLevel = player.serverLevel();
        sourceLevel.playSound(null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.1F);
        sourceLevel.sendParticles(ParticleTypes.PORTAL,
                player.getX(), player.getY() + 1.0D, player.getZ(),
                30, 0.4D, 0.8D, 0.4D, 0.25D);

        player.teleportTo(targetLevel, x, y, z, tag.getFloat(YAW), tag.getFloat(PITCH));
        player.getCooldowns().addCooldown(this, 20 * 10);
        player.displayClientMessage(Component.translatable("message.nightmare_core.anchor.teleported")
                .withStyle(ChatFormatting.LIGHT_PURPLE), true);

        targetLevel.playSound(null, x, y, z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.1F);
        targetLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                x, y + 1.0D, z, 30, 0.4D, 0.8D, 0.4D, 0.12D);

        stack.hurtAndBreak(1, player, broken -> {
        });
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(BOUND);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.nightmare_core.requires_core")
                .withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("tooltip.nightmare_core.anchor.bind")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.nightmare_core.anchor.use")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.nightmare_core.anchor.cost")
                .withStyle(ChatFormatting.DARK_GRAY));

        if (stack.hasTag() && stack.getTag().getBoolean(BOUND)) {
            CompoundTag tag = stack.getTag();
            tooltip.add(Component.translatable("tooltip.nightmare_core.anchor.bound",
                    tag.getString(DIMENSION),
                    Mth.floor(tag.getDouble(X)),
                    Mth.floor(tag.getDouble(Y)),
                    Mth.floor(tag.getDouble(Z))).withStyle(ChatFormatting.DARK_PURPLE));
        } else {
            tooltip.add(Component.translatable("tooltip.nightmare_core.anchor.unbound")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
