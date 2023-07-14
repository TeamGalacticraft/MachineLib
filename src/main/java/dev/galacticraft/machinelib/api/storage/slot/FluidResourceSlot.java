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

package dev.galacticraft.machinelib.api.storage.slot;

import dev.galacticraft.machinelib.api.storage.ResourceFilter;
import dev.galacticraft.machinelib.api.storage.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.io.InputType;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import dev.galacticraft.machinelib.impl.storage.slot.FluidResourceSlotImpl;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FluidResourceSlot extends ResourceSlot<Fluid> {
    @Contract("_ -> new")
    static @NotNull Builder builder(@NotNull InputType inputType) {
        return new Builder(inputType);
    }

    @Contract("_, _, _, _ -> new")
    static @NotNull FluidResourceSlot create(@NotNull InputType inputType, @NotNull TankDisplay display, long capacity, @NotNull ResourceFilter<Fluid> filter) {
        return create(inputType, display, capacity, filter, filter);
    }

    @Contract("_, _, _, _, _ -> new")
    static @NotNull FluidResourceSlot create(@NotNull InputType inputType, @NotNull TankDisplay display, long capacity, @NotNull ResourceFilter<Fluid> filter, @NotNull ResourceFilter<Fluid> externalFilter) {
        if (capacity < 0) throw new IllegalArgumentException();
        return new FluidResourceSlotImpl(inputType, display, capacity, filter, externalFilter);
    }

    @NotNull TankDisplay getDisplay();

    final class Builder {
        private final InputType inputType;
        private int x = 0;
        private int y = 0;
        private int height = 48;

        private ResourceFilter<Fluid> filter = ResourceFilters.any();
        private ResourceFilter<Fluid> strictFilter = null;
        private long capacity = FluidConstants.BUCKET;

        @Contract(pure = true)
        private Builder(@NotNull InputType inputType) {
            this.inputType = inputType;
        }

        @Contract("_, _ -> this")
        public @NotNull FluidResourceSlot.Builder pos(int x, int y) {
            this.x(x);
            this.y(y);
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull FluidResourceSlot.Builder x(int x) {
            this.x = x;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull FluidResourceSlot.Builder y(int y) {
            this.y = y;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull FluidResourceSlot.Builder height(int height) {
            this.height = height;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull FluidResourceSlot.Builder filter(@NotNull ResourceFilter<Fluid> filter) {
            this.filter = filter;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull FluidResourceSlot.Builder strictFilter(@Nullable ResourceFilter<Fluid> strictFilter) {
            this.strictFilter = strictFilter;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull FluidResourceSlot.Builder capacity(long capacity) {
            this.capacity = capacity;
            return this;
        }

        @Contract(pure = true)
        public @NotNull FluidResourceSlot build() {
            if (this.capacity <= 0) throw new IllegalArgumentException("capacity <= 0!");
            if (this.height < 0) throw new IllegalArgumentException("height is negative");

            return FluidResourceSlot.create(this.inputType, TankDisplay.create(this.x, this.y, this.height), this.capacity, this.filter, this.strictFilter == null ? this.filter : this.strictFilter);
        }
    }
}
