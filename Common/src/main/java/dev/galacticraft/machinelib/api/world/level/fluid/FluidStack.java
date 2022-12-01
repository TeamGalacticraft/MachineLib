package dev.galacticraft.machinelib.api.world.level.fluid;

import dev.galacticraft.machinelib.impl.world.level.fluid.FluidStackImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface FluidStack {
    static FluidStack empty() {
        return FluidStackImpl.EMPTY;
    }

    @Contract("!null, _, _ -> new")
    static FluidStack create(Fluid fluid, CompoundTag tag, long amount) {
        if (fluid == null) return empty();
        return new FluidStackImpl(fluid, tag, amount);
    }

    @Contract("!null, _ -> new")
    static FluidStack create(Fluid fluid, long amount) {
        if (fluid == null) return empty();
        return new FluidStackImpl(fluid, amount);
    }

    @Nullable Fluid getFluid();

    CompoundTag getTag();

    CompoundTag getOrCreateTag();

    CompoundTag copyTag();

    long getAmount();

    FluidStack copy();

    boolean isEmpty();

    void setAmount(long amount);

    void shrink(long amount);

    void grow(long amount);

    void setTag(CompoundTag tag);

    default boolean typeAndTagEquals(FluidStack other) {
        return this.getFluid() == other.getFluid() && Objects.equals(this.getTag(), other.getTag());
    }
    default boolean typeAndTagEquals(Fluid fluid, CompoundTag tag) {
        return this.getFluid() == fluid && Objects.equals(this.getTag(), tag);
    }
}
