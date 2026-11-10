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
    private int storedFuel;
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
        return fuelValue(this.getItem(FUEL_SLOT)) + this.storedFuel >= cost;
    }

    private static int fuelValue(ItemStack stack) {
        return stack.getCount() * unitFuelValue(stack);
    }

    private static int unitFuelValue(ItemStack stack) {
        if (stack.is(com.nightmarecore.registry.ModItems.NIGHTMARE_FRAGMENT.get())) {
            return 1;
        }
        if (stack.is(com.nightmarecore.registry.ModItems.NIGHTMARE_MATTER.get())) {
            return 10;
        }
        return 0;
    }

    /**
     * Consumes exactly {@code cost} fuel value. A nightmare matter is worth 10;
     * any unused value is kept in the block entity for the next craft.
     */
    private boolean consumeFuel(int cost) {
        int remaining = cost;
        int used = Math.min(this.storedFuel, remaining);
        this.storedFuel -= used;
        remaining -= used;

        while (remaining > 0) {
            ItemStack fuel = this.getItem(FUEL_SLOT);
            int value = unitFuelValue(fuel);
            if (value <= 0) {
                return false;
            }
            this.removeItem(FUEL_SLOT, 1);
            this.storedFuel += value;
            used = Math.min(this.storedFuel, remaining);
            this.storedFuel -= used;
            remaining -= used;
        }
        return true;
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
        if (!this.consumeFuel(recipe.getFragmentCost())) {
            return;
        }
        for (int slot = INPUT_START; slot < INPUT_START + INPUT_COUNT; slot++) {
            if (!this.getItem(slot).isEmpty()) {
                this.removeItem(slot, 1);
            }
        }

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
        this.storedFuel = 0;
        this.setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == OUTPUT_SLOT) {
            return false;
        }
        if (slot == FUEL_SLOT) {
            return stack.is(com.nightmarecore.registry.ModItems.NIGHTMARE_FRAGMENT.get())
                    || stack.is(com.nightmarecore.registry.ModItems.NIGHTMARE_MATTER.get());
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
        tag.putInt("StoredFuel", this.storedFuel);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.items.clear();
        ContainerHelper.loadAllItems(tag, this.items);
        this.progress = tag.getInt("Progress");
        this.storedFuel = tag.getInt("StoredFuel");
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
        int fuel = this.storedFuel;
        this.storedFuel = 0;
        while (fuel > 0) {
            int count = Math.min(fuel, 64);
            net.minecraft.world.Containers.dropItemStack(this.level,
                    this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(),
                    new ItemStack(com.nightmarecore.registry.ModItems.NIGHTMARE_FRAGMENT.get(), count));
            fuel -= count;
        }
    }

    public int getStoredFuel() {
        return this.storedFuel;
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
