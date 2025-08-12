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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record StorageSpec(@Nullable MachineItemStorage.Spec item, @Nullable MachineEnergyStorage.Spec energy,
                          @Nullable MachineFluidStorage.Spec fluid) {
    public static StorageSpec of(MachineItemStorage.Spec item, MachineEnergyStorage.Spec energy, MachineFluidStorage.Spec fluid) {
        return new StorageSpec(item, energy, fluid);
    }

    public static StorageSpec of(MachineItemStorage.Spec item, MachineEnergyStorage.Spec energy) {
        return of(item, energy, null);
    }

    public static StorageSpec of(MachineItemStorage.Spec item) {
        return of(item, null, null);
    }

    public static StorageSpec of(MachineEnergyStorage.Spec energy) {
        return of(null, energy, null);
    }

    public static StorageSpec of(MachineFluidStorage.Spec fluid) {
        return of(null, null, fluid);
    }

    public static StorageSpec empty() {
        return of(null, null, null);
    }

    public @NotNull MachineItemStorage createItemStorage() {
        return this.item == null ? MachineItemStorage.empty() : this.item.create();
    }

    public @NotNull MachineFluidStorage createFluidStorage() {
        return this.fluid == null ? MachineFluidStorage.empty() : this.fluid.create();
    }

    public @NotNull MachineEnergyStorage createEnergyStorage() {
        return this.energy == null ? MachineEnergyStorage.empty() : this.energy.create();
    }
}
