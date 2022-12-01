package dev.galacticraft.machinelib.api.storage.slot;

import dev.galacticraft.machinelib.api.storage.ChangeTracking;
import dev.galacticraft.machinelib.api.world.level.fluid.FluidStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

public interface FluidTank extends ChangeTracking {
    // do not store this stack on the assertion that it will reflect the future inventory
    // try not to modify the stack. if you do, you must call markDirty()
    FluidStack getStack();
    FluidStack copyStack();

    long getAmount();
    Fluid getFluid();

    long getCapacity();

    void setStack(FluidStack stack);

    // returns amount inserted
    default long insert(Fluid fluid, long amount, boolean simulate) {
        return simulate ? this.simulateInsert(fluid, amount) : this.insert(fluid, amount);
    }
    default long insert(Fluid fluid, CompoundTag tag, long amount, boolean simulate) {
        return simulate ? this.simulateInsert(fluid, tag, amount) : this.insert(fluid, tag, amount);
    }
    default long insert(FluidStack stack, boolean simulate) {
        return simulate ? this.simulateInsert(stack) : this.insert(stack);
    }

    long insert(Fluid fluid, long amount);
    long insertCopyNbt(Fluid fluid, CompoundTag tag, long amount);
    long insert(Fluid fluid, CompoundTag tag, long amount);
    default long insert(FluidStack stack) {
        return this.insert(stack.getFluid(), stack.getTag(), stack.getAmount());
    }

    long simulateInsert(Fluid fluid, long amount);
    long simulateInsert(Fluid fluid, CompoundTag tag, long amount);
    default long simulateInsert(FluidStack stack) {
        return this.simulateInsert(stack.getFluid(), stack.getTag(), stack.getAmount());
    }

    default boolean extract(Fluid fluid, boolean simulate) { // returns true if one was extracted
        return simulate ? this.simulateExtract(fluid) : this.extract(fluid);
    }
    default boolean extractExact(Fluid fluid, long amount, boolean simulate) { // returns true if one was extracted
        return simulate ? this.simulateExtractExact(fluid, amount) : this.extractExact(fluid, amount);
    }
    // returns amount extracted
    default FluidStack extract(long amount, boolean simulate) {
        return simulate ? this.simulateExtract(amount) : this.extract(amount);
    }
    default long extract(Fluid fluid, long amount, boolean simulate) {
        return simulate ? this.simulateExtract(fluid, amount) : this.extract(fluid, amount);
    }
    default long extract(Fluid fluid, CompoundTag tag, long amount, boolean simulate) {
        return simulate ? this.simulateExtract(fluid, amount) : this.extract(fluid, amount);
    }

    boolean extract(Fluid fluid);
    boolean extractExact(Fluid fluid, long amount);
    FluidStack extract(long amount);
    long extract(Fluid fluid, long amount);
    long extract(Fluid fluid, CompoundTag tag, long amount);

    boolean simulateExtract(Fluid fluid);
    boolean simulateExtractExact(Fluid fluid, long amount);
    FluidStack simulateExtract(long amount);
    long simulateExtract(Fluid fluid, long amount);
    long simulateExtract(Fluid fluid, CompoundTag tag, long amount);

    FluidStack swap(FluidStack stack);

    boolean isEmpty();

    @Override
    long getModCount();
}
