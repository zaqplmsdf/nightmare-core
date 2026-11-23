package com.nightmarecore.event;

import com.nightmarecore.NightmareCore;
import com.nightmarecore.item.NightmareCoreItem;
import com.nightmarecore.util.CurioUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Blessing items do not require the core in canEquip: Curios validates loaded
 * items before the nightmare slot is deserialized. The slot lock is applied at
 * runtime instead: with the core the three slots are visible, without it the
 * slots collapse to zero and any blessings move back to the inventory.
 */
@Mod.EventBusSubscriber(modid = NightmareCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BlessingCoreHandler {

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player instanceof FakePlayer
                || player.isSpectator()
                || !player.isAlive()
                || player.tickCount % 20 != 0) {
            return;
        }
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            if (CurioUtil.hasNightmareCore(player)) {
                // Also clears legacy +3 modifiers from older saves.
                NightmareCoreItem.ensureBlessingSlots(player, handler);
            } else {
                NightmareCoreItem.handleCoreLost(player, handler);
            }
        });
    }

    private BlessingCoreHandler() {
    }
}
