package com.nightmarecore.event;

import com.nightmarecore.NightmareCore;
import com.nightmarecore.registry.ModItems;
import com.nightmarecore.util.CurioUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Mechanic trinket: Fate Dice grants a 20% dodge with a short internal cooldown.
 */
@Mod.EventBusSubscriber(modid = NightmareCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TrinketMechanicsHandler {

    private static final String DODGE_READY_AT = "nightmare_core:fate_dice_ready_at";

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled()
                || !(event.getEntity() instanceof ServerPlayer player)
                || !CurioUtil.hasNightmareCore(player)
                || !CurioUtil.hasBlessing(player, ModItems.FATE_DICE.get())
                || player.getRandom().nextFloat() >= 0.20F) {
            return;
        }

        CompoundTag data = player.getPersistentData();
        long now = player.level().getGameTime();
        if (data.getLong(DODGE_READY_AT) > now) {
            return;
        }

        data.putLong(DODGE_READY_AT, now + 100L);
        event.setCanceled(true);
        player.displayClientMessage(
                Component.translatable("message.nightmare_core.fate_dice.dodge")
                        .withStyle(ChatFormatting.GOLD),
                true);
        player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1.0D, player.getZ(),
                16, 0.5D, 0.8D, 0.5D, 0.03D);
    }

    private TrinketMechanicsHandler() {
    }
}
