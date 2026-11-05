package com.nightmarecore.util;

import com.nightmarecore.CurioSlots;
import com.nightmarecore.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

public final class CurioUtil {

    /**
     * Checks the actual nightmare slot, not a generic inventory search. This
     * prevents an item moved into another slot by a command from enabling drops.
     */
    public static boolean hasNightmareCore(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .map(handler -> handler.getStacksHandler(CurioSlots.NIGHTMARE)
                        .map(stacks -> stacks.getSlots() > 0
                                && stacks.getStacks().getStackInSlot(0)
                                .is(ModItems.NIGHTMARE_CORE.get()))
                        .orElse(false))
                .orElse(false);
    }

    /**
     * Returns true if the given item is currently in any blessing slot.
     */
    public static boolean hasBlessing(LivingEntity entity, Item item) {
        return CuriosApi.getCuriosInventory(entity)
                .map(handler -> handler.getStacksHandler(CurioSlots.BLESSING)
                        .map(stacks -> {
                            for (int i = 0; i < stacks.getSlots(); i++) {
                                if (stacks.getStacks().getStackInSlot(i).is(item)) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .orElse(false))
                .orElse(false);
    }

    /**
     * Used by {@code canEquip}: a blessing may only have one copy equipped.
     * The target slot is excluded so replacing the stack already in that slot
     * is not treated as a duplicate.
     */
    public static boolean isDuplicateBlessing(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() == null) {
            return false;
        }
        return CuriosApi.getCuriosInventory(slotContext.entity())
                .map(handler -> handler.getStacksHandler(CurioSlots.BLESSING)
                        .map(stacks -> {
                            for (int i = 0; i < stacks.getSlots(); i++) {
                                if (i == slotContext.index()) {
                                    continue;
                                }
                                if (stacks.getStacks().getStackInSlot(i).is(stack.getItem())) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .orElse(false))
                .orElse(false);
    }

    private CurioUtil() {
    }
}
