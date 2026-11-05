package com.nightmarecore.client;

import com.nightmarecore.NightmareCore;
import com.nightmarecore.registry.ModEffects;
import net.minecraft.client.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side sanity effect: while insane, movement input occasionally reverses
 * for one second. This is a client input effect so it works with normal player
 * movement prediction.
 */
@Mod.EventBusSubscriber(modid = NightmareCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class InsanityClientHandler {

    private static long reverseUntil = 0L;

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        Player player = event.getEntity();
        if (!player.hasEffect(ModEffects.INSANITY.get())) {
            reverseUntil = 0L;
            return;
        }

        if (player.tickCount % 20 == 0 && player.level().random.nextFloat() < 0.25F) {
            reverseUntil = player.tickCount + 20L;
        }
        if (player.tickCount < reverseUntil) {
            Input input = event.getInput();
            input.forwardImpulse = -input.forwardImpulse;
            input.leftImpulse = -input.leftImpulse;
        }
    }

    private InsanityClientHandler() {
    }
}
