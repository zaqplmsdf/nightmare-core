package com.nightmarecore.event;

import com.nightmarecore.CurioSlots;
import com.nightmarecore.NightmareCore;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Hard safety net for the "one copy of each blessing" rule. Normal equips are
 * already rejected by {@code canEquip}; this catches command-inserted duplicates
 * and ejects the later copy back to the player.
 */
@Mod.EventBusSubscriber(modid = NightmareCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BlessingUniquenessHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player instanceof FakePlayer
                || player.isSpectator()
                || player.tickCount % 20 != 0) {
            return;
        }

        CuriosApi.getCuriosInventory(player).ifPresent(handler ->
                handler.getStacksHandler(CurioSlots.BLESSING).ifPresent(stacks -> {
                    Set<Item> seen = new HashSet<>();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack stack = stacks.getStacks().getStackInSlot(i);
                        if (stack.isEmpty()) {
                            continue;
                        }
                        if (seen.add(stack.getItem())) {
                            continue;
                        }

                        ItemStack duplicate = stack.copy();
                        stacks.getStacks().setStackInSlot(i, ItemStack.EMPTY);
                        if (!player.getInventory().add(duplicate)) {
                            player.drop(duplicate, false);
                        }
                        player.displayClientMessage(
                                Component.translatable("message.nightmare_core.blessing.duplicate")
                                        .withStyle(ChatFormatting.RED),
                                true);
                    }
                }));
    }

    private BlessingUniquenessHandler() {
    }
}
