package dev.galacticraft.machinelib.api.storage;

import dev.galacticraft.machinelib.api.world.level.fluid.FluidStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

public interface FluidStorage extends ChangeTracking {
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
    long insert(Fluid fluid, CompoundTag tag, long amount);
    default long insert(FluidStack stack) {
        return this.insert(stack.getFluid(), stack.getTag(), stack.getAmount());
    }

    long simulateInsert(Fluid fluid, long amount);
    long simulateInsert(Fluid fluid, CompoundTag tag, long amount);
    default long simulateInsert(FluidStack stack) {
        return this.simulateInsert(stack.getFluid(), stack.getTag(), stack.getAmount());
    }

    default boolean extract(Fluid fluid, boolean simulate) {
        return simulate ? this.simulateExtract(fluid) : this.extract(fluid);
    } // returns true if one was extracted
    default boolean extractExact(Fluid fluid, long amount, boolean simulate) {
        return simulate ? this.simulateExtractExact(fluid, amount) : this.extractExact(fluid, amount);
    }
    // returns amount extracted
    default long extract(Fluid fluid, long amount, boolean simulate) {
        return simulate ? this.simulateExtract(fluid, amount) : this.extract(fluid, amount);
    }
    default long extract(Fluid fluid, CompoundTag tag, long amount, boolean simulate) {
        return simulate ? this.simulateExtract(fluid, tag, amount) : this.extract(fluid, tag, amount);
    }

    boolean extract(Fluid fluid);
    boolean extractExact(Fluid fluid, long amount);
    long extract(Fluid fluid, long amount);
    long extract(Fluid fluid, CompoundTag tag, long amount);

    boolean simulateExtract(Fluid fluid);
    boolean simulateExtractExact(Fluid fluid, long amount);
    long simulateExtract(Fluid fluid, long amount);
    long simulateExtract(Fluid fluid, CompoundTag tag, long amount);

    @Override
    long getModCount();
}
