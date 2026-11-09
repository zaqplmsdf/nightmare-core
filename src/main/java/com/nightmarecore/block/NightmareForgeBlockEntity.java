package com.nightmarecore.block;

import com.nightmarecore.menu.NightmareForgeMenu;
import com.nightmarecore.registry.ModBlockEntities;
import com.nightmarecore.registry.ModBlocks;
import com.nightmarecore.registry.ModRecipes;
import com.nightmarecore.recipe.NightmareForgeRecipe;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.MenuProvider;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class NightmareForgeBlockEntity extends BlockEntity implements Container, ContainerData, MenuProvider {

    public static final int FUEL_SLOT = 0;
    public static final int INPUT_START = 1;
    public static final int INPUT_COUNT = 9;
    public static final int OUTPUT_SLOT = 10;
    public static final int SLOT_COUNT = 11;
    public static final int MAX_PROGRESS = 100;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int progress;
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new InvWrapper(this));

    public NightmareForgeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NIGHTMARE_FORGE.get(), pos, state);
    }

    public void tick() {
        Level level = this.level;
        if (level == null || level.isClientSide) {
            return;
        }

        Optional<NightmareForgeRecipe> found = level.getRecipeManager()
                .getRecipeFor(ModRecipes.FORGING_TYPE.get(), this, level);
        if (found.isEmpty() || !canOutput(found.get().getResult()) || !hasFuel(found.get().getFragmentCost())) {
            if (this.progress != 0) {
                this.progress = 0;
                this.setChanged();
            }
            return;
        }

        this.progress++;
        if (this.progress >= MAX_PROGRESS) {
            this.progress = 0;
            this.craft(found.get());
        }
        this.setChanged();
    }

    private boolean hasFuel(int cost) {
        ItemStack fuel = this.getItem(FUEL_SLOT);
        return fuel.is(com.nightmarecore.registry.ModItems.NIGHTMARE_FRAGMENT.get())
                && fuel.getCount() >= cost;
    }

    private boolean canOutput(ItemStack result) {
        ItemStack output = this.getItem(OUTPUT_SLOT);
        if (output.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameTags(output, result)
                && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void craft(NightmareForgeRecipe recipe) {
        for (int slot = INPUT_START; slot < INPUT_START + INPUT_COUNT; slot++) {
            if (!this.getItem(slot).isEmpty()) {
                this.removeItem(slot, 1);
            }
        }
        this.removeItem(FUEL_SLOT, recipe.getFragmentCost());

        ItemStack result = recipe.getResult().copy();
        ItemStack output = this.getItem(OUTPUT_SLOT);
        if (output.isEmpty()) {
            this.setItem(OUTPUT_SLOT, result);
        } else {
            output.grow(result.getCount());
        }
        this.setChanged();
    }

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return MAX_PROGRESS;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public int get(int index) {
        return index == 0 ? this.progress : MAX_PROGRESS;
    }

    @Override
    public void set(int index, int value) {
        if (index == 0) {
            this.progress = value;
        }
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(this.items, slot, amount);
        if (!stack.isEmpty()) {
            this.setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(this.worldPosition.getX() + 0.5D,
                this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == OUTPUT_SLOT) {
            return false;
        }
        if (slot == FUEL_SLOT) {
            return stack.is(com.nightmarecore.registry.ModItems.NIGHTMARE_FRAGMENT.get());
        }
        return true;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.items);
        tag.putInt("Progress", this.progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.items.clear();
        ContainerHelper.loadAllItems(tag, this.items);
        this.progress = tag.getInt("Progress");
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }

    public void dropContents() {
        if (this.level == null) {
            return;
        }
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(this.level,
                        this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), stack);
            }
        }
        this.items.clear();
    }

    public Component getDisplayName() {
        return Component.translatable("container.nightmare_core.nightmare_forge");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new NightmareForgeMenu(id, inventory, this, this);
    }

    public LazyOptional<IItemHandler> getItemHandler() {
        return this.itemHandler;
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.itemHandler.invalidate();
    }

    @Override
    public <T> LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap,
                                             net.minecraft.core.Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return this.itemHandler.cast();
        }
        return super.getCapability(cap, side);
    }
}
