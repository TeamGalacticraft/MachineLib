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

package dev.galacticraft.impl.machine.storage.slot;

import dev.galacticraft.impl.fluid.FluidStack;
import dev.galacticraft.impl.machine.ModCount;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

public class FluidSlot extends SingleVariantStorage<FluidVariant> {
    public final ModCount modCount = new ModCount();
    private final long capacity;

    public FluidSlot(long capacity) {
        this.capacity = capacity;
    }

    public int getModCount() {
        return this.modCount.getModCount();
    }

    public void setStack(FluidVariant variant, long amount, @NotNull TransactionContext context) {
        this.updateSnapshots(context);
        this.modCount.increment(context);
        this.variant = variant;
        this.amount = amount;
    }

    public void setVariant(FluidVariant variant, @NotNull TransactionContext context) {
        this.updateSnapshots(context);
        this.modCount.increment(context);
        this.variant = variant;
    }

    public void setAmount(long amount, @NotNull TransactionContext context) {
        this.updateSnapshots(context);
        this.modCount.increment(context);
        this.amount = amount;
    }

    public void extract(long amount, @NotNull TransactionContext context) {
        this.updateSnapshots(context);
        this.modCount.increment(context);
        this.amount -= amount;
        assert this.amount >= 0;
        if (this.amount == 0) {
            this.variant = FluidVariant.blank();
        }
    }

    @Override
    protected FluidVariant getBlankVariant() {
        return FluidVariant.blank();
    }

    @Override
    public long getCapacity(@NotNull FluidVariant variant) {
        return this.capacity;
    }

    public FluidStack copyStack() {
        if (this.variant.isBlank() || this.amount == 0) return FluidStack.EMPTY;
        return new FluidStack(this.variant, this.amount);
    }
}
