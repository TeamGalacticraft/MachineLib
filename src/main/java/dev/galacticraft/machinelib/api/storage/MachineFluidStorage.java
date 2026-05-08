/*
 * Copyright (c) 2021-2025 Team Galacticraft
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

package dev.galacticraft.machinelib.api.storage;

import dev.galacticraft.machinelib.api.compat.transfer.ExposedStorage;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortTarget;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.impl.storage.MachineFluidStorageImpl;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents fluid storage in a machine or formed multiblock.
 */
public interface MachineFluidStorage extends ResourceStorage<Fluid, FluidResourceSlot> {

    /**
     * Creates fluid storage from concrete slots.
     *
     * @param slots fluid slots
     * @return created storage
     */
    static @NotNull MachineFluidStorage create(final FluidResourceSlot @NotNull ... slots) {
        if (slots.length == 0) {
            return empty();
        }

        return new MachineFluidStorageImpl(slots);
    }

    /**
     * Creates a fluid storage specification from slot specifications.
     *
     * @param slots slot specifications
     * @return storage specification
     */
    static @NotNull MachineFluidStorage.Spec spec(final FluidResourceSlot.Spec @NotNull ... slots) {
        if (slots.length == 0) {
            throw new IllegalArgumentException("Cannot create a storage with no slots");
        }

        return new Spec(List.of(slots));
    }

    /**
     * Creates an empty mutable fluid storage specification.
     *
     * @return storage specification
     */
    static @NotNull Spec spec() {
        return new Spec();
    }

    /**
     * Gets the shared empty fluid storage.
     *
     * @return empty fluid storage
     */
    @Contract(pure = true)
    static @NotNull MachineFluidStorage empty() {
        return MachineFluidStorageImpl.EMPTY;
    }

    @Override
    @Nullable
    ExposedStorage<Fluid, FluidVariant> getExposedStorage(@NotNull ResourceFlow flow);

    /**
     * Gets exposed fluid storage for an exact logical target or group.
     *
     * @param flow allowed transfer flow
     * @param target port target
     * @return exposed storage, or {@code null} if no matching tanks can expose that flow
     */
    @Nullable
    ExposedStorage<Fluid, FluidVariant> getExposedStorage(
            @NotNull ResourceFlow flow,
            @NotNull MultiblockPortTarget target
    );

    /**
     * Mutable fluid storage specification.
     */
    class Spec {

        private final List<FluidResourceSlot.Spec> slots;

        private Spec() {
            this(new ArrayList<>());
        }

        public Spec(final List<FluidResourceSlot.Spec> slots) {
            this.slots = slots;
        }

        /**
         * Adds a fluid slot specification.
         *
         * @param slot slot specification
         * @return this specification
         */
        public Spec add(final FluidResourceSlot.Spec slot) {
            this.slots.add(slot);
            return this;
        }

        /**
         * Creates fluid storage from this specification.
         *
         * @return created fluid storage
         */
        public MachineFluidStorage create() {
            final FluidResourceSlot[] slots = new FluidResourceSlot[this.slots.size()];

            for (int i = 0; i < this.slots.size(); i++) {
                slots[i] = this.slots.get(i).create();
            }

            return new MachineFluidStorageImpl(slots);
        }
    }
}