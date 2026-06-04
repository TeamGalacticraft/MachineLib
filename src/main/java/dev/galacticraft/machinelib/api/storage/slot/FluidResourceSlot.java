/*
 * Copyright (c) 2021-2026 Team Galacticraft
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

import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.filter.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import dev.galacticraft.machinelib.impl.storage.slot.FluidResourceSlotImpl;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FluidResourceSlot extends ResourceSlot<Fluid> {
    @Contract("_ -> new")
    static @NotNull Spec builder(@NotNull TransferType transferType) {
        return new Spec(transferType);
    }

    @Contract("_, _, _, _ -> new")
    static @NotNull FluidResourceSlot create(@NotNull TransferType transferType, @Nullable TankDisplay display, long capacity, @NotNull ResourceFilter<Fluid> filter) {
        if (capacity < 0) throw new IllegalArgumentException("capacity < 0");
        return new FluidResourceSlotImpl(transferType, display, capacity, filter);
    }

    /**
     * {@return whether the slot is hidden from the UI}
     */
    boolean isHidden();

    /**
     * {@return the display properties of the slot}
     */
    @Nullable
    TankDisplay getDisplay();

    final class Spec {
        private final TransferType transferType;

        private boolean hidden = false;
        private boolean marked = true;

        private int x = 0;
        private int y = 0;
        private int width = 16;
        private int height = 48;

        private ResourceFilter<Fluid> filter = ResourceFilters.any();
        private long capacity = FluidConstants.BUCKET;

        @Contract(pure = true)
        private Spec(@NotNull TransferType transferType) {
            this.transferType = transferType;
        }

        @Contract("_, _ -> this")
        public @NotNull Spec pos(int x, int y) {
            this.x(x);
            this.y(y);
            return this;
        }

        @Contract(value = "-> this", mutates = "this")
        public @NotNull Spec hidden() {
            this.hidden = true;
            return this;
        }

        @Contract(value = "-> this", mutates = "this")
        public @NotNull Spec unmarked() {
            this.marked = false;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec x(int x) {
            if (this.hidden) throw new UnsupportedOperationException("hidden");
            this.x = x;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec y(int y) {
            if (this.hidden) throw new UnsupportedOperationException("hidden");
            this.y = y;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec width(int width) {
            if (this.hidden) throw new UnsupportedOperationException("hidden");
            this.width = width;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec height(int height) {
            if (this.hidden) throw new UnsupportedOperationException("hidden");
            this.height = height;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec filter(@NotNull ResourceFilter<Fluid> filter) {
            this.filter = filter;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec capacity(long capacity) {
            this.capacity = capacity;
            return this;
        }

        @Contract(pure = true)
        public @NotNull FluidResourceSlot create() {
            if (this.capacity <= 0) throw new IllegalArgumentException("capacity <= 0!");
            if (this.height < 0) throw new IllegalArgumentException("height is negative");
            if (this.hidden) {
                if (this.x != 0 || this.y != 0 || this.width != 16 || this.height != 48)
                    throw new UnsupportedOperationException("Display properties changed while hidden!");
            }

            return FluidResourceSlot.create(this.transferType, this.hidden ? null : TankDisplay.create(this.x, this.y, this.width, this.height, this.marked), this.capacity, this.filter);
        }
    }
}
