package dev.galacticraft.machinelib.impl.storage.slot;

import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.slot.MachineFluidTank;
import dev.galacticraft.machinelib.api.world.level.fluid.FluidStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

import java.util.Objects;

public class MachineFluidTankImpl implements MachineFluidTank {
    private final MachineFluidStorage storage;
    private final long capacity;
    private FluidStack stack;
    private long modCount;

    public MachineFluidTankImpl(MachineFluidStorage storage, long capacity) {
        this.storage = storage;
        this.capacity = capacity;
    }

    @Override
    public FluidStack getStack() {
        return this.stack;
    }

    @Override
    public FluidStack copyStack() {
        return this.stack.copy();
    }

    @Override
    public long getAmount() {
        return this.stack.getAmount();
    }

    @Override
    public Fluid getFluid() {
        return this.stack.getFluid();
    }

    @Override
    public long getCapacity() {
        return this.capacity;
    }

    @Override
    public void silentSetStack(FluidStack stack) {
        this.stack = stack;
    }

    @Override
    public void setStack(FluidStack stack) {
        this.stack = stack;
        this.markDirty();
        this.storage.markDirty();
    }

    @Override
    public long insert(Fluid fluid, long amount) {
        if (this.stack.isEmpty()) {
            amount = Math.min(this.capacity, amount);
            this.setStack(FluidStack.create(fluid, amount));
            return amount;
        } else if (this.stack.getFluid() == fluid && this.stack.getTag() == null) {
            return growStack(amount);
        }
        return 0;
    }

    @Override
    public long insertCopyNbt(Fluid fluid, CompoundTag tag, long amount) {
        if (this.stack.isEmpty()) {
            amount = Math.min(this.capacity, amount);
            FluidStack stack = FluidStack.create(fluid, amount);
            stack.setTag(tag != null ? tag.copy() : null);
            this.setStack(stack);
            return amount;
        } else if (this.stack.getFluid() == fluid && Objects.equals(this.stack.getTag(), tag)) {
            return growStack(amount);
        }
        return 0;
    }

    @Override
    public long insert(Fluid fluid, CompoundTag tag, long amount) {
        if (this.stack.isEmpty()) {
            amount = Math.min(this.capacity, amount);
            FluidStack stack = FluidStack.create(fluid, amount);
            stack.setTag(tag);
            this.setStack(stack);
            return amount;
        } else if (this.stack.getFluid() == fluid && Objects.equals(this.stack.getTag(), tag)) {
            return growStack(amount);
        }
        return 0;
    }

    @Override
    public long simulateInsert(Fluid fluid, long amount) {
        if (this.stack.isEmpty()) {
            return Math.min(this.capacity, amount);
        } else if (this.stack.getFluid() == fluid) {
            return calcGrowth(amount);
        }
        return 0;
    }

    @Override
    public long simulateInsert(Fluid fluid, CompoundTag tag, long amount) {
        if (this.stack.isEmpty()) {
            return Math.min(this.capacity, amount);
        } else if (this.stack.getFluid() == fluid && Objects.equals(this.stack.getTag(), tag)) {
            return calcGrowth(amount);
        }
        return 0;
    }

    @Override
    public long simulateInsert(FluidStack stack) {
        if (this.stack.isEmpty()) {
            return Math.min(this.capacity, stack.getAmount());
        } else if (this.stack.getFluid() == stack.getFluid() && Objects.equals(this.stack.getTag(), stack.getTag())) {
            return calcGrowth(stack.getAmount());
        }
        return 0;
    }

    @Override
    public boolean extract(Fluid fluid) {
        if (this.stack.isEmpty()) return false;
        if (this.stack.getFluid() == fluid) {
            this.stack.shrink(1);
            this.markDirty();
            return true;
        }
        return false;
    }

    @Override
    public boolean extractExact(Fluid fluid, long amount) {
        return this.simulateExtract(fluid, amount) == amount && this.extract(fluid, amount) == amount;
    }

    @Override
    public FluidStack extract(long amount) {
        FluidStack stack = this.stack.copy();
        stack.setAmount(this.shrinkStack(amount));
        return stack;
    }

    @Override
    public long extract(Fluid fluid, long amount) {
        if (this.stack.getFluid() == fluid) {
            return shrinkStack(amount);
        }
        return 0;
    }

    @Override
    public long extract(Fluid fluid, CompoundTag tag, long amount) {
        if (this.stack.getFluid() == fluid && Objects.equals(this.stack.getTag(), tag)) {
            return shrinkStack(amount);
        }
        return 0;
    }

    @Override
    public boolean simulateExtract(Fluid fluid) {
        if (this.stack.isEmpty()) return false;
        return this.stack.getFluid() == fluid;
    }

    @Override
    public boolean simulateExtractExact(Fluid fluid, long amount) {
        return this.simulateExtract(fluid, amount) == amount;
    }

    @Override
    public FluidStack simulateExtract(long amount) {
        if (this.stack.isEmpty()) return FluidStack.empty();
        FluidStack stack = this.stack.copy();
        stack.setAmount(Math.min(amount, this.stack.getAmount()));
        return stack;
    }

    @Override
    public long simulateExtract(Fluid fluid, long amount) {
        if (this.stack.isEmpty() || fluid != this.stack.getFluid()) return 0;
        return Math.min(amount, this.stack.getAmount());
    }

    @Override
    public long simulateExtract(Fluid fluid, CompoundTag tag, long amount) {
        if (this.stack.isEmpty() || fluid != this.stack.getFluid() || !Objects.equals(this.stack.getTag(), tag)) return 0;
        return Math.min(amount, this.stack.getAmount());
    }

    @Override
    public FluidStack swap(FluidStack stack) {
        FluidStack temp = this.stack;
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
        if (this.stack.isEmpty()) this.stack = FluidStack.empty();
        this.modCount++;
    }

    @Override
    public void setModCount(long modCount) {
        this.modCount = modCount;
    }

    private long calcGrowth(long amount) {
        return Math.max(0, this.stack.getAmount() - Math.min(this.capacity, this.stack.getAmount() + amount));
    }

    private long growStack(long amount) {
        amount = calcGrowth(amount);
        this.stack.grow(amount);
        this.markDirty();
        return amount;
    }

    private long shrinkStack(long amount) {
        amount = Math.min(amount, this.stack.getAmount());
        this.stack.shrink(amount);
        this.markDirty();
        return amount;
    }
}
