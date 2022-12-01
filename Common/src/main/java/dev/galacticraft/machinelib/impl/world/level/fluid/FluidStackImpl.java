package dev.galacticraft.machinelib.impl.world.level.fluid;

import dev.galacticraft.machinelib.api.world.level.fluid.FluidStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public final class FluidStackImpl implements FluidStack {
    public static final FluidStack EMPTY = new FluidStackImpl(null, null, 0);

    private final Fluid fluid;
    private CompoundTag tag = null;
    private long amount;

    public FluidStackImpl(Fluid fluid, CompoundTag tag, long amount) {
        this.fluid = fluid;
        this.tag = tag;
        this.amount = amount;
    }

    public FluidStackImpl(Fluid fluid, long amount) {
        this.fluid = fluid;
        this.amount = amount;
    }

    @Override
    public @Nullable Fluid getFluid() {
        return this.fluid;
    }

    @Override
    public CompoundTag getTag() {
        return this.tag;
    }

    @Override
    public CompoundTag getOrCreateTag() {
        if (this.tag == null) this.setTag(new CompoundTag());
        return this.tag;
    }

    @Override
    public CompoundTag copyTag() {
        return this.tag == null ? null : this.tag.copy();
    }

    @Override
    public long getAmount() {
        return this.amount;
    }

    @Override
    public FluidStack copy() {
        return this.isEmpty() ? EMPTY : new FluidStackImpl(this.fluid, this.copyTag(), this.amount);
    }

    @Override
    public boolean isEmpty() {
        return this.amount == 0;
    }

    @Override
    public void setTag(CompoundTag tag) {
        if (this == EMPTY) return;
        this.tag = tag;
    }

    @Override
    public void setAmount(long amount) {
        if (this == EMPTY) return;
        this.amount = Math.max(0, amount);
    }

    @Override
    public void shrink(long amount) {
        if (this == EMPTY) return;
        assert amount >= 0;
        this.amount = Math.max(0, this.amount - amount);
    }

    @Override
    public void grow(long amount) {
        if (this == EMPTY) return;
        assert amount >= 0;
        this.amount += amount;
    }
}
