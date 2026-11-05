package com.nightmarecore.event;

import com.nightmarecore.Config;
import com.nightmarecore.NightmareCore;
import com.nightmarecore.registry.ModItems;
import com.nightmarecore.util.CurioUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * The core no longer has a crafting recipe. A player receives it once, on their
 * first use of a bed, and it is dropped as an item rather than inserted into the
 * inventory.
 */
@Mod.EventBusSubscriber(modid = NightmareCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StarterCoreHandler {

    public static final String RECEIVED_TAG = "nightmare_core:received_starter_core";

    @SubscribeEvent
    public static void onSleep(PlayerSleepInBedEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)
                || !(player.level() instanceof ServerLevel level)
                || player.level().isClientSide
                || !Config.GIVE_CORE_ON_FIRST_SLEEP.get()) {
            return;
        }

        CompoundTag data = player.getPersistentData();
        if (data.getBoolean(RECEIVED_TAG)) {
            return;
        }

        boolean alreadyOwned = CurioUtil.hasNightmareCore(player)
                || player.getInventory().contains(new ItemStack(ModItems.NIGHTMARE_CORE.get()));
        if (alreadyOwned) {
            data.putBoolean(RECEIVED_TAG, true);
            return;
        }

        data.putBoolean(RECEIVED_TAG, true);
        ItemStack core = new ItemStack(ModItems.NIGHTMARE_CORE.get());
        serverPlayer.drop(core, false);

        BlockPos bed = event.getOptionalPos().orElse(player.blockPosition());
        level.playSound(null, bed, SoundEvents.ARMOR_EQUIP_NETHERITE, SoundSource.PLAYERS, 0.8F, 0.65F);
        level.sendParticles(ParticleTypes.SOUL,
                bed.getX() + 0.5D, bed.getY() + 0.7D, bed.getZ() + 0.5D,
                20, 0.7D, 0.6D, 0.7D, 0.02D);
        serverPlayer.displayClientMessage(
                Component.translatable("message.nightmare_core.starter_core")
                        .withStyle(ChatFormatting.DARK_PURPLE),
                false);
    }

    private StarterCoreHandler() {
    }
}
