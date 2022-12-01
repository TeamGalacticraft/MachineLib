package dev.galacticraft.machinelib.api.storage;

import dev.galacticraft.machinelib.api.storage.slot.FluidTank;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.world.level.fluid.FluidStack;
import dev.galacticraft.machinelib.impl.storage.ResourceFilter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

public interface SlottedFluidStorage extends FluidStorage {
    int size();

    FluidTank getTank(int slot);
    SlotGroup getGroup(int slot);

    default FluidStack getStack(int slot) {
        return this.getTank(slot).getStack();
    }
    default FluidStack copyStack(int slot) {
        return this.getTank(slot).copyStack();
    }

    default long getAmount(int slot) {
        return this.getTank(slot).getAmount();
    }
    default Fluid getFluid(int slot) {
        return this.getTank(slot).getFluid();
    }

    ResourceFilter<Fluid> getFilter(int slot);

    boolean canPlayerInsert(int slot);

    boolean canExternalInsert(int slot);
    boolean canExternalExtract(int slot);

    default void setStack(int slot, FluidStack stack) {
        this.getTank(slot).setStack(stack);
    }

    default long insertMerge(Fluid fluid, long amount, boolean simulate) {
        return simulate ? this.simulateInsertMerge(fluid, amount) : this.insertMerge(fluid, amount);
    }
    default long insertMerge(Fluid fluid, CompoundTag tag, long amount, boolean simulate) {
        return simulate ? this.simulateInsertMerge(fluid, tag, amount) : this.insertMerge(fluid, tag, amount);
    }
    default long insertMerge(FluidStack stack, boolean simulate) {
        return simulate ? this.simulateInsertMerge(stack) : this.insertMerge(stack);
    }

    long simulateInsertMerge(Fluid fluid, long amount);
    long simulateInsertMerge(Fluid fluid, CompoundTag tag, long amount);
    default long simulateInsertMerge(FluidStack stack) {
        return this.simulateInsertMerge(stack.getFluid(), stack.getTag(), stack.getAmount());
    }

    long insertMerge(Fluid fluid, long amount);
    long insertMerge(Fluid fluid, CompoundTag tag, long amount);
    default long insertMerge(FluidStack stack) {
        return this.insert(stack.getFluid(), stack.getTag(), stack.getAmount());
    }

    // returns amount inserted
    default long insert(int slot, Fluid fluid, long amount, boolean simulate) {
        return this.getTank(slot).insert(fluid, amount, simulate);
    }
    default long insert(int slot, Fluid fluid, CompoundTag tag, long amount, boolean simulate) {
        return this.getTank(slot).insert(fluid, tag, amount, simulate);
    }
    default long insert(int slot, FluidStack stack, boolean simulate) {
        return this.getTank(slot).insert(stack, simulate);
    }

    default long insert(int slot, Fluid fluid, long amount) {
        return this.getTank(slot).insert(fluid, amount);
    }
    default long insertCopyNbt(int slot, Fluid fluid, CompoundTag tag, long amount) {
        return this.getTank(slot).insert(fluid, tag, amount);
    }
    default long insert(int slot, Fluid fluid, CompoundTag tag, long amount) {
        return this.getTank(slot).insert(fluid, tag, amount);
    }
    default long insert(int slot, FluidStack stack) {
        return this.getTank(slot).insert(stack);
    }

    default long simulateInsert(int slot, Fluid fluid, long amount) {
        return this.getTank(slot).simulateInsert(fluid, amount);
    }
    default long simulateInsert(int slot, Fluid fluid, CompoundTag tag, long amount) {
        return this.getTank(slot).simulateInsert(fluid, tag, amount);
    }
    default long simulateInsert(int slot, FluidStack stack) {
        return this.getTank(slot).simulateInsert(stack);
    }

    default boolean extract(int slot, Fluid fluid, boolean simulate) {
        return this.getTank(slot).extract(fluid, simulate);
    } // returns true if one was extracted
    default boolean extractExact(int slot, Fluid fluid, long amount, boolean simulate) {
        return this.getTank(slot).extractExact(fluid, amount, simulate);
    }
    // returns amount extracted
    default FluidStack extract(int slot, long amount, boolean simulate) {
        return this.getTank(slot).extract(amount, simulate);
    }
    default long extract(int slot, Fluid fluid, long amount, boolean simulate) {
        return this.getTank(slot).extract(fluid, amount, simulate);
    }
    default long extract(int slot, Fluid fluid, CompoundTag tag, long amount, boolean simulate) {
        return this.getTank(slot).extract(fluid, tag, amount, simulate);
    }

    default boolean extract(int slot, Fluid fluid) {
        return this.getTank(slot).extract(fluid);
    }
    default boolean extractExact(int slot, Fluid fluid, long amount) {
        return this.getTank(slot).extractExact(fluid, amount);
    }
    default FluidStack extract(int slot, long amount) {
        return this.getTank(slot).extract(amount);
    }
    default long extract(int slot, Fluid fluid, long amount) {
        return this.getTank(slot).extract(fluid, amount);
    }
    default long extract(int slot, Fluid fluid, CompoundTag tag, long amount) {
        return this.getTank(slot).extract(fluid, tag, amount);
    }

    default boolean simulateExtract(int slot, Fluid fluid) {
        return this.getTank(slot).simulateExtract(fluid);
    }
    default boolean simulateExtractExact(int slot, Fluid fluid, long amount) {
        return this.getTank(slot).simulateExtractExact(fluid, amount);
    }
    default FluidStack simulateExtract(int slot, long amount) {
        return this.getTank(slot).simulateExtract(amount);
    }
    default long simulateExtract(int slot, Fluid fluid, long amount) {
        return this.getTank(slot).simulateExtract(fluid, amount);
    }
    default long simulateExtract(int slot, Fluid fluid, CompoundTag tag, long amount) {
        return this.getTank(slot).simulateExtract(fluid, tag, amount);
    }

    default FluidStack swap(int slot, FluidStack stack) {
        return this.getTank(slot).swap(stack);
    }

    boolean isEmpty();
    default boolean isEmpty(int slot) {
        return this.getTank(slot).isEmpty();
    }

    default long getModCount(int slot) {
        return this.getTank(slot).getModCount();
    }
    @Override
    long getModCount();
}
