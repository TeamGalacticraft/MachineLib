/*
 * Copyright (c) 2021-2023 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.galacticraft.machinelib.api.fluid;

import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FluidStack {
    public static final FluidStack EMPTY = new FluidStack(null, null, 0);
    private final @Nullable Fluid fluid;
    private @Nullable CompoundTag tag;
    private long amount;

    @Contract(pure = true)
    private FluidStack(@Nullable Fluid fluid, @Nullable CompoundTag tag, long amount) {
        this.fluid = fluid;
        this.tag = tag;
        this.amount = amount;
    }

    @Contract(pure = true)
    public static FluidStack create(@Nullable Fluid fluid, @Nullable CompoundTag tag, long amount) {
        StoragePreconditions.notNegative(amount);
        if (fluid == null || amount == 0) return EMPTY;
        return new FluidStack(fluid, tag, amount);
    }

    public @Nullable Fluid getFluid() {
        return this.fluid;
    }

    public @Nullable CompoundTag getTag() {
        return this.tag;
    }

    public void setTag(@Nullable CompoundTag tag) {
        this.tag = tag;
    }

    public long getAmount() {
        return this.amount;
    }

    public void setAmount(long amount) {
        StoragePreconditions.notNegative(amount);
        this.amount = this.fluid == null ? 0 : amount;
    }

    public boolean isEmpty() {
        return this.amount == 0;
    }

    public void grow(long amount) {
        if (this.fluid != null) this.amount += amount;
    }

    public void shrink(long amount) {
        if (this.fluid != null) this.amount -= Math.min(this.amount, amount);
    }

    @Override
    @Contract(value = "null -> false", pure = true)
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FluidStack that = (FluidStack) o;
        return amount == that.amount && Objects.equals(fluid, that.fluid) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fluid, tag, amount);
    }
}
