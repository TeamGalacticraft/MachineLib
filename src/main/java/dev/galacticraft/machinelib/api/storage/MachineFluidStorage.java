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

package dev.galacticraft.machinelib.api.storage;

import dev.galacticraft.machinelib.api.compat.transfer.ExposedStorage;
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
 * Represents a storage for fluids in a machine.
 *
 * @see ResourceStorage
 */
public interface MachineFluidStorage extends ResourceStorage<Fluid, FluidResourceSlot> {
    static @NotNull MachineFluidStorage create(FluidResourceSlot @NotNull ... slots) {
        if (slots.length == 0) return empty();
        return new MachineFluidStorageImpl(slots);
    }

    static @NotNull MachineFluidStorage.Spec spec(FluidResourceSlot.Spec @NotNull ... slots) {
        if (slots.length == 0) throw new IllegalArgumentException("Cannot create a storage with no slots");
        return new Spec(List.of(slots));
    }

    static @NotNull Spec spec() {
        return new Spec();
    }

    @Contract(pure = true)
    static @NotNull MachineFluidStorage empty() {
        return MachineFluidStorageImpl.EMPTY;
    }

    // overridden to set the variant type
    @Override
    @Nullable
    ExposedStorage<Fluid, FluidVariant> getExposedStorage(@NotNull ResourceFlow flow);

    class Spec {
        private final List<FluidResourceSlot.Spec> slots;

        private Spec() {
            this(new ArrayList<>());
        }

        public Spec(List<FluidResourceSlot.Spec> slots) {
            this.slots = slots;
        }

        public Spec add(FluidResourceSlot.Spec slot) {
            this.slots.add(slot);
            return this;
        }

        public MachineFluidStorage create() {
            FluidResourceSlot[] slots1 = new FluidResourceSlot[this.slots.size()];
            for (int i = 0; i < this.slots.size(); i++) {
                slots1[i] = this.slots.get(i).create();
            }
            return new MachineFluidStorageImpl(slots1);
        }
    }
}
