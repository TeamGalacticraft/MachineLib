/*
 * Copyright (c) 2021-2024 Team Galacticraft
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

import dev.galacticraft.machinelib.api.menu.sync.MenuSynchronizable;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.impl.storage.MachineFluidStorageImpl;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface MachineFluidStorage extends ResourceStorage<Fluid, FluidResourceSlot>, MenuSynchronizable {
    static @NotNull MachineFluidStorage create(FluidResourceSlot @NotNull ... slots) {
        if (slots.length == 0) return empty();
        return new MachineFluidStorageImpl(slots);
    }

    static @NotNull Supplier<MachineFluidStorage> of(FluidResourceSlot.Builder @NotNull ... slots) {
        if (slots.length == 0) return MachineFluidStorage::empty;
        return () -> {
            FluidResourceSlot[] slots1 = new FluidResourceSlot[slots.length];
            for (int i = 0; i < slots.length; i++) {
                slots1[i] = slots[i].build();
            }
            return new MachineFluidStorageImpl(slots1);
        };
    }

    @Contract(pure = true)
    static @NotNull MachineFluidStorage empty() {
        return MachineFluidStorageImpl.EMPTY;
    }
}
