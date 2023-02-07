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

package dev.galacticraft.machinelib.impl.storage.slot;

import dev.galacticraft.machinelib.api.fluid.FluidStack;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

public class FluidSlotGroupImpl<Slot extends ResourceSlot<Fluid, FluidStack>> extends SlotGroupImpl<Fluid, FluidStack, Slot> {
    public FluidSlotGroupImpl(@NotNull Slot @NotNull [] slots) {
        super(slots);
    }

    @Override
    public boolean canInsertStack(@NotNull FluidStack stack) {
        if (stack.isEmpty()) return true;
        assert stack.getFluid() != null && stack.getFluid() != Fluids.EMPTY && stack.getAmount() > 0;
        long inserted = 0;
        for (Slot slot : this) {
            inserted += slot.tryInsert(stack.getFluid(), stack.getTag(), stack.getAmount() - inserted);
            if (stack.getAmount() == inserted) return true;
        }
        return stack.getAmount() == inserted;
    }

    @Override
    public long tryInsertStack(@NotNull FluidStack stack) {
        if (stack.isEmpty()) return 0;
        assert stack.getFluid() != null && stack.getFluid() != Fluids.EMPTY && stack.getAmount() > 0;
        long inserted = 0;
        for (Slot slot : this) {
            inserted += slot.tryInsert(stack.getFluid(), stack.getTag(), stack.getAmount() - inserted);
            if (stack.getAmount() == inserted) break;
        }
        return inserted;
    }

    @Override
    public long insertStack(@NotNull FluidStack stack) {
        if (stack.isEmpty()) return 0;
        assert stack.getFluid() != null && stack.getFluid() != Fluids.EMPTY && stack.getAmount() > 0;
        long inserted = 0;
        for (Slot slot : this) {
            inserted += slot.insert(stack.getFluid(), stack.getTag(), stack.getAmount() - inserted);
            if (stack.getAmount() == inserted) break;
        }
        return inserted;
    }
}
