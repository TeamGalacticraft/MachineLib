/*
 * Copyright (c) 2019-2022 Team Galacticraft
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

package dev.galacticraft.api.machine.storage;

import dev.galacticraft.api.block.entity.MachineBlockEntity;
import dev.galacticraft.api.machine.storage.display.TankDisplay;
import dev.galacticraft.api.machine.storage.io.SlotType;
import dev.galacticraft.api.screen.MachineScreenHandler;
import dev.galacticraft.impl.fluid.FluidStack;
import dev.galacticraft.impl.machine.storage.MachineFluidStorageImpl;
import dev.galacticraft.impl.machine.storage.empty.EmptyMachineFluidStorage;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.fluid.Fluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluid storage for machines.
 */
public interface MachineFluidStorage extends ResourceStorage<Fluid, FluidVariant, FluidStack> {
    /**
     * Adds tanks to a screen handler for display.
     * @param handler The screen handler to add tanks to.
     * @param <M> The type of machine.
     */
    <M extends MachineBlockEntity> void addTanks(MachineScreenHandler<M> handler);

    /**
     * Returns the default empty fluid storage.
     * @return The default empty fluid storage.
     */
    static MachineFluidStorage empty() {
        return EmptyMachineFluidStorage.INSTANCE;
    }

    /**
     * Creates a new fluid storage builder.
     * @return The fluid storage builder.
     */
    @Contract(value = " -> new", pure = true)
    static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * A builder for creating fluid storages.
     */
    class Builder {
        private int size = 0;
        private final List<SlotType<Fluid, FluidVariant>> types = new ArrayList<>();
        private final List<TankDisplay> displays = new ArrayList<>();
        private final LongList counts = new LongArrayList();

        public Builder() {}

        /**
         * Creates a new builder.
         * @return The new builder.
         */
        @Contract(value = " -> new", pure = true)
        public static @NotNull Builder create() {
            return new Builder();
        }

        /**
         * Adds a tank to the storage.
         * @param type The type of tank.
         * @param capacity The capacity of the tank.
         * @param display The display for the tank.
         * @return The builder.
         */
        public @NotNull Builder addSlot(SlotType<Fluid, FluidVariant> type, long capacity, TankDisplay display) {
            this.size++;
            this.types.add(type);
            this.displays.add(display);
            this.counts.add(capacity);
            return this;
        }

        /**
         * Builds the machine fluid storage.
         * @return The machine fluid storage.
         */
        @Contract(pure = true, value = " -> new")
        public @NotNull MachineFluidStorage build() {
            if (this.size == 0) return empty();
            return new MachineFluidStorageImpl(this.size, this.types.toArray(new SlotType[0]), this.counts.toLongArray(), this.displays.toArray(new TankDisplay[0]));
        }
    }
}
