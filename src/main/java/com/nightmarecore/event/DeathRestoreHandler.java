package com.nightmarecore.event;

import com.nightmarecore.CurioSlots;
import com.nightmarecore.NightmareCore;
import com.nightmarecore.util.CurioUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/**
 * Curios can retain kept items in the player inventory after death while the
 * core's blessing-slot modifier is temporarily gone. Re-equip the exact
 * core/blessing snapshot once the new player has ticked, so blessings are not
 * silently left unequipped.
 */
@Mod.EventBusSubscriber(modid = NightmareCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DeathRestoreHandler {

    private static final Map<UUID, PendingRestore> PENDING = new HashMap<>();

    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            snapshot(player, player.tickCount + 2);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()
                || !(event.getOriginal() instanceof ServerPlayer oldPlayer)
                || !(event.getEntity() instanceof ServerPlayer newPlayer)) {
            return;
        }

        PendingRestore atDeath = PENDING.get(newPlayer.getUUID());
        PendingRestore fromClone = snapshot(oldPlayer, newPlayer.tickCount + 2);
        PendingRestore chosen = atDeath != null && atDeath.blessings().size() > fromClone.blessings().size()
                ? new PendingRestore(atDeath.core(), atDeath.blessings(), newPlayer.tickCount + 2, 0)
                : fromClone;
        if (!chosen.core().isEmpty() || !chosen.blessings().isEmpty()) {
            PENDING.put(newPlayer.getUUID(), chosen);
        }
    }

    private static PendingRestore snapshot(ServerPlayer player, int readyAt) {
        return CuriosApi.getCuriosInventory(player).map(handler -> {
            ItemStack core = handler.getStacksHandler(CurioSlots.NIGHTMARE)
                    .map(stacks -> stacks.getStacks().getStackInSlot(0).copy())
                    .orElse(ItemStack.EMPTY);
            if (core.isEmpty()) {
                core = findInInventory(player, com.nightmarecore.registry.ModItems.NIGHTMARE_CORE.get());
            }
            List<ItemStack> blessings = new ArrayList<>();
            handler.getStacksHandler(CurioSlots.BLESSING).ifPresent(stacks -> {
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStacks().getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        blessings.add(stack.copy());
                    }
                }
            });
            return new PendingRestore(core, blessings, readyAt, 0);
        }).orElse(new PendingRestore(ItemStack.EMPTY, List.of(), readyAt, 0));
    }

    private static ItemStack findInInventory(ServerPlayer player, net.minecraft.world.item.Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(item)) {
                return stack.copyWithCount(1);
            }
        }
        return ItemStack.EMPTY;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        PendingRestore pending = PENDING.get(player.getUUID());
        if (pending == null || player.tickCount < pending.readyAt()) {
            return;
        }
        PENDING.remove(player.getUUID());
        restore(player, pending);
    }

    private static void restore(ServerPlayer player, PendingRestore pending) {
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            boolean corePresent = CurioUtil.hasNightmareCore(player);
            if (!corePresent) {
                ItemStack coreWanted = pending.core().isEmpty()
                        ? findInInventory(player, com.nightmarecore.registry.ModItems.NIGHTMARE_CORE.get())
                        : pending.core();
                ItemStack core = takeMatching(player, coreWanted);
                if (!core.isEmpty()) {
                    handler.setEquippedCurio(CurioSlots.NIGHTMARE, 0, core);
                    corePresent = true;
                }
            }
            if (corePresent) {
                ensureBlessingSlots(player, handler);
            }
            handler.processSlots();

            Optional<ICurioStacksHandler> blessingHandler =
                    handler.getStacksHandler(CurioSlots.BLESSING);
            if (blessingHandler.isEmpty()) {
                return;
            }
            ICurioStacksHandler stacks = blessingHandler.get();
            if (stacks.getSlots() <= 0 && !pending.blessings().isEmpty()) {
                if (pending.attempts() < 20) {
                    PENDING.put(player.getUUID(), new PendingRestore(
                            pending.core(), pending.blessings(), player.tickCount + 2, pending.attempts() + 1));
                }
                return;
            }
            for (ItemStack wanted : pending.blessings()) {
                if (handler.findFirstCurio(wanted.getItem()).isPresent()) {
                    continue;
                }
                ItemStack found = takeMatching(player, wanted);
                if (found.isEmpty()) {
                    continue;
                }
                int target = firstEmptySlot(stacks);
                if (target < 0) {
                    // No free slot: return the item to the inventory rather than voiding it.
                    if (!player.getInventory().add(found)) {
                        player.drop(found, false);
                    }
                    continue;
                }
                handler.setEquippedCurio(CurioSlots.BLESSING, target, found);
            }
        });
    }

    private static void ensureBlessingSlots(ServerPlayer player, ICuriosItemHandler handler) {
        int slots = handler.getStacksHandler(CurioSlots.BLESSING)
                .map(stacks -> stacks.getSlots()).orElse(0);
        if (slots >= 3) {
            return;
        }
        SlotContext coreSlot = new SlotContext(
                CurioSlots.NIGHTMARE, player, 0, false, true);
        UUID uuid = CuriosApi.getSlotUuid(coreSlot);
        handler.addPermanentSlotModifier(CurioSlots.BLESSING, uuid,
                "nightmare_core:core_blessing_slots", 3,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION);
        handler.processSlots();
    }

    private static int firstEmptySlot(ICurioStacksHandler stacks) {
        for (int i = 0; i < stacks.getSlots(); i++) {
            if (stacks.getStacks().getStackInSlot(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Removes exactly one matching stack from the player's regular inventory.
     */
    private static ItemStack takeMatching(ServerPlayer player, ItemStack wanted) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && ItemStack.isSameItemSameTags(stack, wanted)) {
                return player.getInventory().removeItem(i, 1);
            }
        }
        return ItemStack.EMPTY;
    }

    private record PendingRestore(ItemStack core, List<ItemStack> blessings, int readyAt, int attempts) {
    }

    private DeathRestoreHandler() {
    }
}
