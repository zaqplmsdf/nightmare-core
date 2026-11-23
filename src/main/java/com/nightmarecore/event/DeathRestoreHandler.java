package com.nightmarecore.event;

import com.nightmarecore.CurioSlots;
import com.nightmarecore.NightmareCore;
import com.nightmarecore.item.NightmareCoreItem;
import com.nightmarecore.registry.ModItems;
import com.nightmarecore.util.CurioUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/**
 * Curios validates every saved curio through {@code canEquip} while rebuilding
 * the new player's inventory. A blessing can be rejected (and moved to the
 * player's inventory) if the core has not been installed into the fresh
 * inventory yet, or if another mod rejects the equip event during load. The
 * blessing slots themselves are fixed, so a rejected blessing is never lost,
 * but it stays unequipped until the player opens the Curios menu.
 *
 * <p>To make {@code keepInventory} respawns deterministic, snapshot the exact
 * equipped core/blessings on the death event (before Curios processes drops and
 * before the new player is created) and re-equip anything that did not survive
 * the clone. Restore is retried for a few ticks because Curios/other mods can
 * still flush invalid stacks right after the clone event.
 */
@Mod.EventBusSubscriber(modid = NightmareCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DeathRestoreHandler {

    /** Up to 40 retries, each scheduled two ticks apart. */
    private static final int MAX_RESTORE_ATTEMPTS = 40;
    private static final int RETRY_DELAY_TICKS = 2;

    private static final Map<UUID, PendingRestore> PENDING = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.level().isClientSide) {
            return;
        }

        // A previous death can leave a pending restore behind; never let it
        // outlive the next death.
        PENDING.remove(player.getUUID());

        // Only keepInventory deaths are allowed to re-equip the snapshot.
        // Without keepInventory Curios drops the blessings before the clone
        // event, and restoring them would bypass the drop rules.
        if (!player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            return;
        }

        PendingRestore snapshot = snapshot(player);
        if (!snapshot.core().isEmpty() || !snapshot.blessings().isEmpty()) {
            PENDING.put(player.getUUID(), snapshot);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()
                || !(event.getOriginal() instanceof ServerPlayer oldPlayer)
                || !(event.getEntity() instanceof ServerPlayer newPlayer)) {
            return;
        }

        PendingRestore atDeath = PENDING.remove(newPlayer.getUUID());
        PendingRestore fromClone = snapshot(oldPlayer);
        PendingRestore chosen = chooseSnapshot(atDeath, fromClone);
        if (!chosen.core().isEmpty() || !chosen.blessings().isEmpty()) {
            PENDING.put(newPlayer.getUUID(), new PendingRestore(
                    chosen.core(), chosen.blessings(), newPlayer.tickCount + RETRY_DELAY_TICKS, 0));
        }
    }

    /**
     * The at-death snapshot is taken first and is the authoritative one for
     * keepInventory deaths; the clone snapshot is a fallback so normal deaths
     * still restore a kept core.
     */
    private static PendingRestore chooseSnapshot(PendingRestore atDeath, PendingRestore fromClone) {
        if (atDeath == null) {
            return fromClone;
        }
        PendingRestore base = atDeath.blessings().size() >= fromClone.blessings().size()
                ? atDeath
                : fromClone;
        ItemStack core = base.core().isEmpty() ? fromClone.core() : base.core();
        return new PendingRestore(core, base.blessings(), 0, 0);
    }

    private static PendingRestore snapshot(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player).map(handler -> {
            ItemStack core = handler.getStacksHandler(CurioSlots.NIGHTMARE)
                    .map(stacks -> stacks.getStacks().getStackInSlot(0).copy())
                    .orElse(ItemStack.EMPTY);
            if (core.isEmpty()) {
                core = findInInventory(player, ModItems.NIGHTMARE_CORE.get());
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
            return new PendingRestore(core, blessings, 0, 0);
        }).orElse(new PendingRestore(ItemStack.EMPTY, List.of(), 0, 0));
    }

    private static ItemStack findInInventory(ServerPlayer player, Item item) {
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
                || !(event.player instanceof ServerPlayer player)
                || player.isRemoved()
                || player.isDeadOrDying()
                || player.isSpectator()) {
            return;
        }
        PendingRestore pending = PENDING.get(player.getUUID());
        if (pending == null || player.tickCount < pending.readyAt()) {
            return;
        }
        PENDING.remove(player.getUUID());

        if (!restore(player, pending) && pending.attempts() < MAX_RESTORE_ATTEMPTS) {
            PENDING.put(player.getUUID(), new PendingRestore(
                    pending.core(), pending.blessings(),
                    player.tickCount + RETRY_DELAY_TICKS, pending.attempts() + 1));
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PENDING.remove(event.getEntity().getUUID());
    }

    /**
     * Re-equips the snapshot. Returns true when everything in the snapshot is
     * either equipped or was legitimately dropped and no retry is needed.
     */
    private static boolean restore(ServerPlayer player, PendingRestore pending) {
        Optional<ICuriosItemHandler> optional = CuriosApi.getCuriosInventory(player).resolve();
        if (optional.isEmpty()) {
            return false;
        }
        ICuriosItemHandler handler = optional.get();

        // Curios may have queued the old blessings as invalid stacks while
        // loading. Flush them into the inventory before re-equipping,
        // otherwise they are ejected again after this restore.
        handler.handleInvalidStacks();

        boolean complete = true;
        boolean corePresent = CurioUtil.hasNightmareCore(player);
        if (!corePresent && !pending.core().isEmpty()) {
            ItemStack core = takeMatching(player, pending.core());
            if (!core.isEmpty()) {
                handler.setEquippedCurio(CurioSlots.NIGHTMARE, 0, core);
                corePresent = true;
            }
        }
        if (!corePresent && !pending.core().isEmpty()) {
            // The core is not available yet; retry instead of leaving the
            // blessings gated behind a missing core.
            complete = false;
        }
        if (corePresent) {
            NightmareCoreItem.ensureBlessingSlots(player, handler);
        } else {
            // No core on the respawned player: do not leave three visible
            // blessing slots behind.
            NightmareCoreItem.lockBlessingSlots(handler);
        }
        handler.processSlots();

        Optional<ICurioStacksHandler> blessingHandler =
                handler.getStacksHandler(CurioSlots.BLESSING);
        if (blessingHandler.isEmpty()) {
            return false;
        }
        ICurioStacksHandler stacks = blessingHandler.get();
        if (stacks.getSlots() <= 0) {
            return pending.blessings().isEmpty();
        }

        for (ItemStack wanted : pending.blessings()) {
            if (handler.findFirstCurio(wanted.getItem()).isPresent()) {
                continue;
            }
            ItemStack found = takeMatching(player, wanted);
            if (found.isEmpty()) {
                complete = false;
                continue;
            }
            int target = firstEmptySlot(stacks);
            if (target < 0) {
                // No free slot: return the item to the inventory rather than voiding it.
                if (!player.getInventory().add(found)) {
                    player.drop(found, false);
                }
                complete = false;
                continue;
            }
            handler.setEquippedCurio(CurioSlots.BLESSING, target, found);
        }
        return complete;
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
