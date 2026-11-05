package com.nightmarecore.item;

import com.nightmarecore.registry.ModItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * A consumable dropped by the Nightmare. It grants one random basic blessing
 * (one of the six attribute blessings; the Dreamhunter's Eye is excluded).
 */
public class BlessingBagItem extends Item {

    private static final List<Item> BASIC_BLESSINGS = List.of(
            ModItems.EMBER_HEART.get(),
            ModItems.DREAMREND_FANG.get(),
            ModItems.VIGIL_CARAPACE.get(),
            ModItems.STONEBOUND_PENDANT.get(),
            ModItems.FLEET_FEATHER.get(),
            ModItems.ECHO_BELL.get()
    );

    public BlessingBagItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bag = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(bag);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(bag);
        }

        Item reward = BASIC_BLESSINGS.get(serverPlayer.getRandom().nextInt(BASIC_BLESSINGS.size()));
        ItemStack rewardStack = new ItemStack(reward);
        if (!serverPlayer.getInventory().add(rewardStack)) {
            serverPlayer.drop(rewardStack, false);
        }

        if (!serverPlayer.isCreative()) {
            bag.shrink(1);
        }

        ServerLevel serverLevel = serverPlayer.serverLevel();
        serverLevel.sendParticles(ParticleTypes.END_ROD,
                serverPlayer.getX(), serverPlayer.getY() + 1.0D, serverPlayer.getZ(),
                18, 0.4D, 0.7D, 0.4D, 0.02D);
        serverLevel.playSound(null,
                serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9F, 1.4F);
        serverPlayer.displayClientMessage(
                Component.translatable("message.nightmare_core.blessing_bag.opened", reward.getDescription())
                        .withStyle(ChatFormatting.LIGHT_PURPLE),
                true);
        return InteractionResultHolder.consume(bag);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.nightmare_core.blessing_bag.use")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.nightmare_core.blessing_bag.source")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
}
