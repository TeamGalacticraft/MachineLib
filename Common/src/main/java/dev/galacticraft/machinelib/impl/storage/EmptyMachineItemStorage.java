package dev.galacticraft.machinelib.impl.storage;

import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.MachineItemSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

public final class EmptyMachineItemStorage implements MachineItemStorage {
    public static final EmptyMachineItemStorage INSTANCE = new EmptyMachineItemStorage();

    private EmptyMachineItemStorage() {}

    @Override
    public int insert(Item item, int amount) {
        return 0;
    }

    @Override
    public int insert(Item item, CompoundTag tag, int amount) {
        return 0;
    }

    @Override
    public int simulateInsert(Item item, int amount) {
        return 0;
    }

    @Override
    public int simulateInsert(Item item, CompoundTag tag, int amount) {
        return 0;
    }

    @Override
    public boolean extract(Item item) {
        return false;
    }

    @Override
    public boolean extractExact(Item item, int amount) {
        return false;
    }

    @Override
    public int extract(Item item, int amount) {
        return 0;
    }

    @Override
    public int extract(Item item, CompoundTag tag, int amount) {
        return 0;
    }

    @Override
    public boolean simulateExtract(Item item) {
        return false;
    }

    @Override
    public boolean simulateExtractExact(Item item, int amount) {
        return false;
    }

    @Override
    public int simulateExtract(Item item, int amount) {
        return 0;
    }

    @Override
    public int simulateExtract(Item item, CompoundTag tag, int amount) {
        return 0;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public SlotGroup getGroup(int slot) {
        throw new IndexOutOfBoundsException("No slots");
    }

    @Override
    public ResourceFilter<Item> getFilter(int slot) {
        throw new IndexOutOfBoundsException("No slots");
    }

    @Override
    public boolean canPlayerInsert(int slot) {
        throw new IndexOutOfBoundsException("No slots");
    }

    @Override
    public boolean canExternalInsert(int slot) {
        throw new IndexOutOfBoundsException("No slots");
    }

    @Override
    public boolean canExternalExtract(int slot) {
        throw new IndexOutOfBoundsException("No slots");
    }

    @Override
    public int simulateInsertMerge(Item item, int amount) {
        return 0;
    }

    @Override
    public int simulateInsertMerge(Item item, CompoundTag tag, int amount) {
        return 0;
    }

    @Override
    public int insertMerge(Item item, int amount) {
        return 0;
    }

    @Override
    public int insertMerge(Item item, CompoundTag tag, int amount) {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public long getModCount() {
        return 0;
    }

    @Override
    public MachineItemSlot getSlot(int slot) {
        throw new IndexOutOfBoundsException("No slots");
    }

    @Override
    public void markDirty() {

    }

    @Override
    public void setModCount(long modCount) {

    }
}
