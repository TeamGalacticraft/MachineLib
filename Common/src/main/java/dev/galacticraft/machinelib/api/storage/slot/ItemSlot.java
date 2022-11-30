package dev.galacticraft.machinelib.api.storage.slot;

import dev.galacticraft.machinelib.api.storage.ChangeTracking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface ItemSlot extends ChangeTracking {
    // do not store this stack on the assertion that it will reflect the future inventory
    // try not to modify the stack. if you do, you must call markDirty()
    ItemStack getStack();
    ItemStack copyStack();

    int getAmount();
    Item getItem();

    int getCapacity();
    int getCurrentCapacity();

    void setStack(ItemStack stack);

    // returns amount inserted
    default int insert(Item item, int amount, boolean simulate) {
        return simulate ? this.simulateInsert(item, amount) : this.insert(item, amount);
    }
    default int insert(Item item, CompoundTag tag, int amount, boolean simulate) {
        return simulate ? this.simulateInsert(item, tag, amount) : this.insert(item, tag, amount);
    }
    default int insert(ItemStack stack, boolean simulate) {
        return simulate ? this.simulateInsert(stack) : this.insert(stack);
    }

    int insert(Item item, int amount);
    int insertCopyNbt(Item item, CompoundTag tag, int amount);
    int insert(Item item, CompoundTag tag, int amount);
    default int insert(ItemStack stack) {
        return this.insert(stack.getItem(), stack.getTag(), stack.getCount());
    }

    int simulateInsert(Item item, int amount);
    int simulateInsert(Item item, CompoundTag tag, int amount);
    default int simulateInsert(ItemStack stack) {
        return this.simulateInsert(stack.getItem(), stack.getTag(), stack.getCount());
    }

    default boolean extract(Item item, boolean simulate) { // returns true if one was extracted
        return simulate ? this.simulateExtract(item) : this.extract(item);
    }
    default boolean extractExact(Item item, int amount, boolean simulate) { // returns true if one was extracted
        return simulate ? this.simulateExtractExact(item, amount) : this.extractExact(item, amount);
    }
    // returns amount extracted
    default ItemStack extract(int amount, boolean simulate) {
        return simulate ? this.simulateExtract(amount) : this.extract(amount);
    }
    default int extract(Item item, int amount, boolean simulate) {
        return simulate ? this.simulateExtract(item, amount) : this.extract(item, amount);
    }
    default int extract(Item item, CompoundTag tag, int amount, boolean simulate) {
        return simulate ? this.simulateExtract(item, amount) : this.extract(item, amount);
    }

    boolean extract(Item item);
    boolean extractExact(Item item, int amount);
    ItemStack extract(int amount);
    int extract(Item item, int amount);
    int extract(Item item, CompoundTag tag, int amount);

    boolean simulateExtract(Item item);
    boolean simulateExtractExact(Item item, int amount);
    ItemStack simulateExtract(int amount);
    int simulateExtract(Item item, int amount);
    int simulateExtract(Item item, CompoundTag tag, int amount);

    ItemStack swap(ItemStack stack);

    boolean isEmpty();

    @Override
    long getModCount();
}
