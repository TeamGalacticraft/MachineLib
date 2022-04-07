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

import dev.galacticraft.impl.machine.ModCount;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class ResourceSlot<T, V extends TransferVariant<T>, S> extends SingleVariantStorage<V> {
    public final ModCount modCount = new ModCount();
    private final long capacity;

    public ResourceSlot(long capacity) {
        this.capacity = capacity;
    }

    public int getModCount() {
        return this.modCount.getModCount();
    }

    public void setStack(V variant, long amount, @NotNull TransactionContext context) {
        this.updateSnapshots(context);
        this.modCount.increment(context);
        this.variant = variant;
        this.amount = amount;
    }

    public void setVariant(V variant, @NotNull TransactionContext context) {
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
            this.variant = this.getBlankVariant();
        }
    }

    @Override
    public long getCapacity(@NotNull V variant) {
        return this.capacity;
    }

    @Contract(pure = true)
    protected abstract @NotNull S getEmptyStack();

    @Contract(pure = true)
    protected abstract @NotNull S createStack(@NotNull V variant, long amount);

    public @NotNull S copyStack() {
        if (this.variant.isBlank() || this.amount == 0) return this.getEmptyStack();
        return this.createStack(this.variant, this.amount);
    }
}
