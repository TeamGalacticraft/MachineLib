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

package dev.galacticraft.machinelib.impl.storage;

import dev.galacticraft.machinelib.api.compat.transfer.ExposedStorage;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.impl.compat.transfer.ExposedFluidSlotImpl;
import dev.galacticraft.machinelib.impl.compat.transfer.ExposedStorageImpl;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MachineFluidStorageImpl extends ResourceStorageImpl<Fluid, FluidResourceSlot> implements MachineFluidStorage {
    public static final MachineFluidStorageImpl EMPTY = new MachineFluidStorageImpl(new FluidResourceSlot[0]);
    private final ExposedStorage<Fluid, FluidVariant>[] exposedStorages = new ExposedStorage[3];

    public MachineFluidStorageImpl(@NotNull FluidResourceSlot @NotNull [] slots) {
        super(slots);
        for (int i = 0; i < 3; i++) {
            this.exposedStorages[i] = this.createExposedStorage(ResourceFlow.values()[i]);
        }
    }

    protected @Nullable ExposedStorage<Fluid, FluidVariant> createExposedStorage(@NotNull ResourceFlow flow) {
        ExposedFluidSlotImpl[] slots = new ExposedFluidSlotImpl[this.size()];
        boolean support = false;
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new ExposedFluidSlotImpl(this.getSlots()[i], flow);
            support |= slots[i].supportsInsertion() || slots[i].supportsExtraction();
        }
        return support ? new ExposedStorageImpl<>(this, slots) : null;
    }

    @Override
    public @Nullable ExposedStorage<Fluid, FluidVariant> getExposedStorage(@NotNull ResourceFlow flow) {
        return this.exposedStorages[flow.ordinal()];
    }
}
