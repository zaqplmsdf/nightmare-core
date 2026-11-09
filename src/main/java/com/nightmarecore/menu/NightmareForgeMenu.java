package com.nightmarecore.menu;

import com.nightmarecore.block.NightmareForgeBlockEntity;
import com.nightmarecore.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class NightmareForgeMenu extends AbstractContainerMenu {

    private static final int PLAYER_SLOTS = 36;
    private final Container container;
    private final ContainerData data;

    public NightmareForgeMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
        this(id, inventory, resolve(inventory, buf.readBlockPos()));
    }

    private NightmareForgeMenu(int id, Inventory inventory, ClientHolder holder) {
        this(id, inventory, holder.container, holder.data);
    }

    public NightmareForgeMenu(int id, Inventory inventory,
                              NightmareForgeBlockEntity forge, ContainerData data) {
        this(id, inventory, (Container) forge, data);
    }

    private NightmareForgeMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(ModMenuTypes.NIGHTMARE_FORGE.get(), id);
        this.container = container;
        this.data = data;

        this.addSlot(new Slot(container, NightmareForgeBlockEntity.FUEL_SLOT, 8, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(com.nightmarecore.registry.ModItems.NIGHTMARE_FRAGMENT.get());
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = NightmareForgeBlockEntity.INPUT_START + row * 3 + col;
                this.addSlot(new Slot(container, index, 30 + col * 18, 17 + row * 18));
            }
        }

        this.addSlot(new Slot(container, NightmareForgeBlockEntity.OUTPUT_SLOT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, 9 + row * 9 + col, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }

        this.addDataSlots(data);
    }

    private static ClientHolder resolve(Inventory inventory, net.minecraft.core.BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof NightmareForgeBlockEntity forge) {
            return new ClientHolder(forge, forge);
        }
        return new ClientHolder(new SimpleContainer(NightmareForgeBlockEntity.SLOT_COUNT),
                new SimpleContainerData(2));
    }

    public int getProgress() {
        return this.data.get(0);
    }

    public int getMaxProgress() {
        return NightmareForgeBlockEntity.MAX_PROGRESS;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index == NightmareForgeBlockEntity.OUTPUT_SLOT) {
                if (!this.moveItemStackTo(stack, 11, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= NightmareForgeBlockEntity.INPUT_START
                    && index < NightmareForgeBlockEntity.INPUT_START + NightmareForgeBlockEntity.INPUT_COUNT) {
                if (!this.moveItemStackTo(stack, 11, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index == NightmareForgeBlockEntity.FUEL_SLOT) {
                if (!this.moveItemStackTo(stack, 11, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (stack.is(com.nightmarecore.registry.ModItems.NIGHTMARE_FRAGMENT.get())) {
                    if (!this.moveItemStackTo(stack, NightmareForgeBlockEntity.FUEL_SLOT,
                            NightmareForgeBlockEntity.FUEL_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stack, NightmareForgeBlockEntity.INPUT_START,
                        NightmareForgeBlockEntity.INPUT_START + NightmareForgeBlockEntity.INPUT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    private record ClientHolder(Container container, ContainerData data) {
    }
}
