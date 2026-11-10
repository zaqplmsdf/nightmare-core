package com.nightmarecore.event;

import com.nightmarecore.CurioSlots;
import com.nightmarecore.NightmareCore;
import com.nightmarecore.util.CurioUtil;
import com.nightmarecore.util.TrinketTier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.util.FakePlayer;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Blessing items do not require the core in canEquip: Curios validates loaded
 * items before the nightmare slot is deserialized. This tick check preserves
 * the gameplay restriction without causing relog/death item loss.
 */
@Mod.EventBusSubscriber(modid = NightmareCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BlessingCoreHandler {

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player instanceof FakePlayer
                || player.tickCount % 20 != 0
                || CurioUtil.hasNightmareCore(player)) {
            return;
        }
        CuriosApi.getCuriosInventory(player).ifPresent(handler ->
                handler.getStacksHandler(CurioSlots.BLESSING).ifPresent(stacks -> {
                    boolean moved = false;
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack stack = stacks.getStacks().getStackInSlot(i);
                        if (stack.isEmpty() || TrinketTier.tierOf(stack.getItem()) <= 0) {
                            continue;
                        }
                        ItemStack copy = stack.copy();
                        stacks.getStacks().setStackInSlot(i, ItemStack.EMPTY);
                        if (!player.getInventory().add(copy)) {
                            player.drop(copy, false);
                        }
                        moved = true;
                    }
                    if (moved) {
                        player.displayClientMessage(
                                Component.translatable("message.nightmare_core.blessing.requires_core")
                                        .withStyle(ChatFormatting.RED), true);
                    }
                }));
    }

    private BlessingCoreHandler() {
    }
}
