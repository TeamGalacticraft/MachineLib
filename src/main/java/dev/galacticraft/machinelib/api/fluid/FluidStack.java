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

import dev.galacticraft.machinelib.impl.fluid.FluidStackImpl;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated
public interface FluidStack {
    @Contract(pure = true)
    static @NotNull FluidStack empty() {
        return FluidStackImpl.EMPTY;
    }

    @Contract(pure = true)
    static @NotNull FluidStack create(@Nullable Fluid fluid, long amount) {
        return create(fluid, null, amount);
    }

    @Contract(pure = true)
    static @NotNull FluidStack create(@Nullable Fluid fluid, @Nullable CompoundTag tag, long amount) {
        StoragePreconditions.notNegative(amount);
        if (fluid == null || fluid == Fluids.EMPTY || amount == 0) return empty();
        return new FluidStackImpl(fluid, tag, amount);
    }

    @Contract(pure = true)
    @Nullable Fluid getFluid();

    @Contract(pure = true)
    @Nullable CompoundTag getTag();

    @Contract(mutates = "this")
    void setTag(@Nullable CompoundTag tag);

    @Contract(pure = true)
    long getAmount();

    @Contract(mutates = "this")
    void setAmount(long amount);

    @Contract(pure = true)
    boolean isEmpty();

    @Contract(mutates = "this")
    void grow(long amount);

    @Contract(mutates = "this")
    void shrink(long amount);

    @Override
    @Contract(value = "null -> false", pure = true)
    boolean equals(Object o);

    @Override
    int hashCode();
}
