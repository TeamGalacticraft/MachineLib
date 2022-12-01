package dev.galacticraft.machinelib.api.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface ItemStorage extends ChangeTracking {
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
    int insert(Item item, CompoundTag tag, int amount);
    default int insert(ItemStack stack) {
        return this.insert(stack.getItem(), stack.getTag(), stack.getCount());
    }

    int simulateInsert(Item item, int amount);
    int simulateInsert(Item item, CompoundTag tag, int amount);
    default int simulateInsert(ItemStack stack) {
        return this.simulateInsert(stack.getItem(), stack.getTag(), stack.getCount());
    }

    default boolean extract(Item item, boolean simulate) {
        return simulate ? this.simulateExtract(item) : this.extract(item);
    } // returns true if one was extracted
    default boolean extractExact(Item item, int amount, boolean simulate) {
        return simulate ? this.simulateExtractExact(item, amount) : this.extractExact(item, amount);
    }
    // returns amount extracted
    default int extract(Item item, int amount, boolean simulate) {
        return simulate ? this.simulateExtract(item, amount) : this.extract(item, amount);
    }
    default int extract(Item item, CompoundTag tag, int amount, boolean simulate) {
        return simulate ? this.simulateExtract(item, tag, amount) : this.extract(item, tag, amount);
    }

    boolean extract(Item item);
    boolean extractExact(Item item, int amount);
    int extract(Item item, int amount);
    int extract(Item item, CompoundTag tag, int amount);

    boolean simulateExtract(Item item);
    boolean simulateExtractExact(Item item, int amount);
    int simulateExtract(Item item, int amount);
    int simulateExtract(Item item, CompoundTag tag, int amount);

    @Override
    long getModCount();
}
