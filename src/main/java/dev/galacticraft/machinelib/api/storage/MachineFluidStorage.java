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

package dev.galacticraft.machinelib.api.storage;

import dev.galacticraft.machinelib.api.fluid.FluidStack;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.impl.storage.EmptyMachineFluidStorage;
import dev.galacticraft.machinelib.impl.storage.MachineFluidStorageImpl;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface MachineFluidStorage extends ResourceStorage<Fluid, FluidStack, FluidResourceSlot, SlotGroup<Fluid, FluidStack, FluidResourceSlot>>, MenuSynchronizable {
    @Contract(value = " -> new", pure = true)
    static @NotNull Builder builder() {
        return new Builder();
    }

    @Contract(pure = true)
    static @NotNull MachineFluidStorage empty() {
        return EmptyMachineFluidStorage.INSTANCE;
    }

    class Builder {
        private final List<SlotGroup<Fluid, FluidStack, FluidResourceSlot>> groups = new ArrayList<>();

        private Builder() {
        }

        public @NotNull MachineFluidStorage.Builder addGroup(@Nullable SlotGroup<Fluid, FluidStack, FluidResourceSlot> group) {
            if (group == null || group.isEmpty()) return this;
            for (SlotGroup<Fluid, FluidStack, FluidResourceSlot> group1 : this.groups) {
                if (group1.getType() == group.getType()) {
                    throw new UnsupportedOperationException("duplicate group");
                }
            }
            this.groups.add(group);
            return this;
        }

        public @NotNull MachineFluidStorage build() {
            if (this.groups.isEmpty()) return MachineFluidStorage.empty();
            return new MachineFluidStorageImpl(this.groups.toArray(new SlotGroup[0]));
        }
    }
}
