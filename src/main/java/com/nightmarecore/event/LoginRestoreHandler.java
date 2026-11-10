package com.nightmarecore.event;

import com.nightmarecore.CurioSlots;
import com.nightmarecore.NightmareCore;
import com.nightmarecore.item.NightmareCoreItem;
import com.nightmarecore.util.CurioUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/**
 * Curios can reload the nightmare core into its slot without rebuilding the
 * core's +3 blessing-slot modifier. The blessing items are then treated as
 * invalid and moved out. Persist the equipped snapshot and restore it after
 * login, once Curios has finished loading the player.
 */
@Mod.EventBusSubscriber(modid = NightmareCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LoginRestoreHandler {

    private static final String SNAPSHOT_KEY = "nightmare_core:equipped_snapshot";
    private static final String CORE_KEY = "Core";
    private static final String BLESSINGS_KEY = "Blessings";
    private static final Map<UUID, PendingRestore> PENDING = new HashMap<>();

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            ItemStack core = handler.getStacksHandler(CurioSlots.NIGHTMARE)
                    .map(stacks -> stacks.getStacks().getStackInSlot(0).copy())
                    .orElse(ItemStack.EMPTY);
            ListTag blessings = new ListTag();
            handler.getStacksHandler(CurioSlots.BLESSING).ifPresent(stacks -> {
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStacks().getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        blessings.add(stack.save(new CompoundTag()));
                    }
                }
            });

            CompoundTag data = player.getPersistentData();
            if (core.isEmpty() && blessings.isEmpty()) {
                data.remove(SNAPSHOT_KEY);
                return;
            }
            CompoundTag snapshot = new CompoundTag();
            if (!core.isEmpty()) {
                snapshot.put(CORE_KEY, core.save(new CompoundTag()));
            }
            snapshot.put(BLESSINGS_KEY, blessings);
            data.put(SNAPSHOT_KEY, snapshot);
        });
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        PendingRestore pending = readSnapshot(player);
        PENDING.put(player.getUUID(), new PendingRestore(
                pending.core(), pending.blessings(), player.tickCount + 2, 0));
    }

    private static PendingRestore readSnapshot(ServerPlayer player) {
        CompoundTag snapshot = player.getPersistentData().getCompound(SNAPSHOT_KEY);
        if (snapshot.isEmpty()) {
            return new PendingRestore(ItemStack.EMPTY, List.of(), 0, 0);
        }
        ItemStack core = snapshot.contains(CORE_KEY, Tag.TAG_COMPOUND)
                ? ItemStack.of(snapshot.getCompound(CORE_KEY)) : ItemStack.EMPTY;
        List<ItemStack> blessings = new ArrayList<>();
        ListTag list = snapshot.getList(BLESSINGS_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            ItemStack stack = ItemStack.of(list.getCompound(i));
            if (!stack.isEmpty()) {
                blessings.add(stack);
            }
        }
        return new PendingRestore(core, blessings, 0, 0);
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
        NightmareCoreItem.ensureBlessingSlots(player, handler);
    }

    private static int firstEmptySlot(ICurioStacksHandler stacks) {
        for (int i = 0; i < stacks.getSlots(); i++) {
            if (stacks.getStacks().getStackInSlot(i).isEmpty()) {
                return i;
            }
        }
        return -1;
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

    private LoginRestoreHandler() {
    }
}
