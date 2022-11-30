package dev.galacticraft.machinelib.impl.storage.slot;

import dev.galacticraft.machinelib.impl.storage.InternalSlottedItemStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class ItemSlotImpl implements InternalItemSlot {
    private final InternalSlottedItemStorage storage;
    private final int capacity;
    private ItemStack stack;
    private long modCount;

    public ItemSlotImpl(InternalSlottedItemStorage storage, int capacity) {
        this.storage = storage;
        this.capacity = capacity;
    }

    @Override
    public ItemStack getStack() {
        return this.stack;
    }

    @Override
    public ItemStack copyStack() {
        return this.stack.copy();
    }

    @Override
    public int getAmount() {
        return this.stack.getCount();
    }

    @Override
    public Item getItem() {
        return this.stack.getItem();
    }

    @Override
    public int getCapacity() {
        return this.capacity;
    }

    @Override
    public int getCurrentCapacity() {
        return this.isEmpty() ? this.capacity : Math.min(this.capacity, this.getItem().getMaxStackSize());
    }

    @Override
    public void silentSetStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public void setStack(ItemStack stack) {
        this.stack = stack;
        this.markDirty();
        this.storage.markDirty();
    }

    @Override
    public int insert(Item item, int amount) {
        if (this.stack.isEmpty()) {
            amount = clampSize(item, amount);
            this.setStack(new ItemStack(item, amount));
            return amount;
        } else if (this.stack.getItem() == item && this.stack.getTag() == null) {
            return growStack(item, amount);
        }
        return 0;
    }

    @Override
    public int insertCopyNbt(Item item, CompoundTag tag, int amount) {
        if (this.stack.isEmpty()) {
            amount = clampSize(item, amount);
            ItemStack stack = new ItemStack(item, amount);
            stack.setTag(tag != null ? tag.copy() : null);
            this.setStack(stack);
            return amount;
        } else if (this.stack.getItem() == item && Objects.equals(this.stack.getTag(), tag)) {
            return growStack(item, amount);
        }
        return 0;
    }

    @Override
    public int insert(Item item, CompoundTag tag, int amount) {
        if (this.stack.isEmpty()) {
            amount = clampSize(item, amount);
            ItemStack stack = new ItemStack(item, amount);
            stack.setTag(tag);
            this.setStack(stack);
            return amount;
        } else if (this.stack.getItem() == item && Objects.equals(this.stack.getTag(), tag)) {
            return growStack(item, amount);
        }
        return 0;
    }

    @Override
    public int simulateInsert(Item item, int amount) {
        if (this.stack.isEmpty()) {
            return clampSize(item, amount);
        } else if (this.stack.getItem() == item) {
            return calcGrowth(item, amount);
        }
        return 0;
    }

    @Override
    public int simulateInsert(Item item, CompoundTag tag, int amount) {
        if (this.stack.isEmpty()) {
            return clampSize(item, amount);
        } else if (this.stack.getItem() == item && Objects.equals(this.stack.getTag(), tag)) {
            return calcGrowth(item, amount);
        }
        return 0;
    }

    @Override
    public int simulateInsert(ItemStack stack) {
        if (this.stack.isEmpty()) {
            return clampSize(stack.getItem(), stack.getCount());
        } else if (this.stack.getItem() == stack.getItem() && Objects.equals(this.stack.getTag(), stack.getTag())) {
            return calcGrowth(stack.getItem(), stack.getCount());
        }
        return 0;
    }

    @Override
    public boolean extract(Item item) {
        if (this.stack.isEmpty()) return false;
        if (this.stack.getItem() == item) {
            this.stack.shrink(1);
            this.markDirty();
            return true;
        }
        return false;
    }

    @Override
    public boolean extractExact(Item item, int amount) {
        return this.simulateExtract(item, amount) == amount && this.extract(item, amount) == amount;
    }

    @Override
    public ItemStack extract(int amount) {
        ItemStack stack = this.stack.copy();
        stack.setCount(this.shrinkStack(amount));
        return stack;
    }

    @Override
    public int extract(Item item, int amount) {
        if (this.stack.getItem() == item) {
            return shrinkStack(amount);
        }
        return 0;
    }

    @Override
    public int extract(Item item, CompoundTag tag, int amount) {
        if (this.stack.getItem() == item && Objects.equals(this.stack.getTag(), tag)) {
            return shrinkStack(amount);
        }
        return 0;
    }

    @Override
    public boolean simulateExtract(Item item) {
        if (this.stack.isEmpty()) return false;
        return this.stack.getItem() == item;
    }

    @Override
    public boolean simulateExtractExact(Item item, int amount) {
        return this.simulateExtract(item, amount) == amount;
    }

    @Override
    public ItemStack simulateExtract(int amount) {
        if (this.stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack stack = this.stack.copy();
        stack.setCount(Math.min(amount, this.stack.getCount()));
        return stack;
    }

    @Override
    public int simulateExtract(Item item, int amount) {
        if (this.stack.isEmpty() || item != this.stack.getItem()) return 0;
        return Math.min(amount, this.stack.getCount());
    }

    @Override
    public int simulateExtract(Item item, CompoundTag tag, int amount) {
        if (this.stack.isEmpty() || item != this.stack.getItem() || !Objects.equals(this.stack.getTag(), tag)) return 0;
        return Math.min(amount, this.stack.getCount());
    }

    @Override
    public ItemStack swap(ItemStack stack) {
        ItemStack temp = this.stack;
        this.setStack(stack);
        return temp;
    }

    @Override
    public boolean isEmpty() {
        return this.stack.isEmpty();
    }

    @Override
    public long getModCount() {
        return this.modCount;
    }

    @Override
    public void markDirty() {
        if (this.stack.isEmpty()) this.stack = ItemStack.EMPTY;
        this.modCount++;
    }

    @Override
    public void setModCount(long modCount) {
        this.modCount = modCount;
    }

    private int clampSize(Item item, int amount) {
        return Math.min(this.capacity, Math.min(item.getMaxStackSize(), amount));
    }

    private int calcGrowth(Item item, int amount) {
        return Math.max(0, this.stack.getCount() - clampSize(item, this.stack.getCount() + amount));
    }

    private int growStack(Item item, int amount) {
        amount = calcGrowth(item, amount);
        this.stack.grow(amount);
        this.markDirty();
        return amount;
    }

    private int clampShrink(int amount) {
        return Math.min(amount, this.stack.getCount());
    }

    private int shrinkStack(int amount) {
        amount = this.clampShrink(amount);
        this.stack.shrink(amount);
        this.markDirty();
        return amount;
    }
}
