package dev.galacticraft.machinelib.impl.storage;

import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.slot.MachineFluidTank;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

public final class EmptyMachineFluidStorage implements MachineFluidStorage {
    public static final EmptyMachineFluidStorage INSTANCE = new EmptyMachineFluidStorage();

    private EmptyMachineFluidStorage() {}

    @Override
    public long insert(Fluid Fluid, long amount) {
        return 0;
    }

    @Override
    public long insert(Fluid Fluid, CompoundTag tag, long amount) {
        return 0;
    }

    @Override
    public long simulateInsert(Fluid Fluid, long amount) {
        return 0;
    }

    @Override
    public long simulateInsert(Fluid Fluid, CompoundTag tag, long amount) {
        return 0;
    }

    @Override
    public boolean extract(Fluid Fluid) {
        return false;
    }

    @Override
    public boolean extractExact(Fluid Fluid, long amount) {
        return false;
    }

    @Override
    public long extract(Fluid Fluid, long amount) {
        return 0;
    }

    @Override
    public long extract(Fluid Fluid, CompoundTag tag, long amount) {
        return 0;
    }

    @Override
    public boolean simulateExtract(Fluid Fluid) {
        return false;
    }

    @Override
    public boolean simulateExtractExact(Fluid Fluid, long amount) {
        return false;
    }

    @Override
    public long simulateExtract(Fluid Fluid, long amount) {
        return 0;
    }

    @Override
    public long simulateExtract(Fluid Fluid, CompoundTag tag, long amount) {
        return 0;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public SlotGroup getGroup(int slot) {
        throw new IndexOutOfBoundsException("No tanks");
    }

    @Override
    public ResourceFilter<Fluid> getFilter(int slot) {
        throw new IndexOutOfBoundsException("No tanks");
    }

    @Override
    public boolean canPlayerInsert(int slot) {
        throw new IndexOutOfBoundsException("No tanks");
    }

    @Override
    public boolean canExternalInsert(int slot) {
        throw new IndexOutOfBoundsException("No tanks");
    }

    @Override
    public boolean canExternalExtract(int slot) {
        throw new IndexOutOfBoundsException("No tanks");
    }

    @Override
    public long simulateInsertMerge(Fluid Fluid, long amount) {
        return 0;
    }

    @Override
    public long simulateInsertMerge(Fluid Fluid, CompoundTag tag, long amount) {
        return 0;
    }

    @Override
    public long insertMerge(Fluid Fluid, long amount) {
        return 0;
    }

    @Override
    public long insertMerge(Fluid Fluid, CompoundTag tag, long amount) {
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
    public MachineFluidTank getTank(int slot) {
        throw new IndexOutOfBoundsException("No tanks");
    }

    @Override
    public void markDirty() {

    }

    @Override
    public void setModCount(long modCount) {

    }
}
