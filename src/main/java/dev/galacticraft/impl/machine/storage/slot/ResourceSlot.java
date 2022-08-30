/*
 * Copyright (c) 2021-2022 Team Galacticraft
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

import dev.galacticraft.api.machine.storage.StorageSlot;
import dev.galacticraft.impl.machine.ModCount;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

public abstract class ResourceSlot<T, V extends TransferVariant<T>, S> extends SnapshotParticipant<ResourceAmount<V>>  implements StorageSlot<T, V, S> {
    private final ModCount modCount;
    private final long capacity;
    private V variant = this.getBlankVariant();
    private long amount = 0;

    public ResourceSlot(long capacity, @NotNull ModCount modCount) {
        this.capacity = capacity;
        this.modCount = ModCount.parented(modCount);
    }

    @Override
    public long getModCount() {
        return this.modCount.getModCount();
    }

    @Override
    public long getModCountUnsafe() {
        return this.modCount.getModCountUnsafe();
    }

    @TestOnly
    public void setStackUnsafe(@NotNull V variant, long amount, boolean markDirty) {
        StoragePreconditions.notNegative(amount);
        if (amount == 0) variant = this.getBlankVariant();
        else if (variant.isBlank()) amount = 0;
        this.variant = variant;
        this.amount = amount;
        if (markDirty) this.modCount.incrementUnsafe();
    }

    @Override
    public void setStack(@NotNull V variant, long amount, @NotNull TransactionContext context) {
        StoragePreconditions.notNegative(amount);
        this.updateSnapshots(context);
        this.modCount.increment(context);
        if (amount == 0) variant = this.getBlankVariant();
        else if (variant.isBlank()) amount = 0;
        this.variant = variant;
        this.amount = amount;
    }

    public void setAmount(long amount, @NotNull TransactionContext context) {
        StoragePreconditions.notNegative(amount);
        if (this.variant.isBlank()) {
            if (amount != 0) {
                throw new IllegalArgumentException("Cannot set amount of blank variant");
            }
        }
        this.updateSnapshots(context);
        this.modCount.increment(context);
        if (amount == 0) {
            this.variant = this.getBlankVariant();
        }
        this.amount = amount;
    }

    public S extract(long amount, @NotNull TransactionContext context) {
        if (this.variant.isBlank()) return this.getEmptyStack();
        V v = this.variant;
        long extract = this.extract(v, amount, context);
        return this.createStack(v, extract);
    }

    protected abstract @NotNull V getBlankVariant();

    protected abstract long getVariantCapacity(@NotNull V variant);

    @Contract(pure = true)
    protected abstract @NotNull S getEmptyStack();

    @Contract(pure = true)
    protected abstract @NotNull S createStack(@NotNull V variant, long amount);

    public @NotNull S copyStack() {
        if (this.variant.isBlank() || this.amount == 0) return this.getEmptyStack();
        return this.createStack(this.variant, this.amount);
    }

    @Override
    public long insert(V variant, long maxAmount, @NotNull TransactionContext context) {
        StoragePreconditions.notBlankNotNegative(variant, maxAmount);

        if (this.variant.isBlank() || this.variant.equals(variant)) {
            long insertedAmount = Math.min(maxAmount, Math.min(this.capacity, getCapacity(variant)) - this.amount);

            if (insertedAmount > 0) {
                updateSnapshots(context);

                if (this.variant.isBlank()) {
                    this.variant = variant;
                    this.amount = insertedAmount;
                } else {
                    this.amount += insertedAmount;
                }
                this.modCount.increment(context);
            }

            return insertedAmount;
        }

        return 0;
    }

    @Override
    public long extract(V variant, long amount, @NotNull TransactionContext context) {
        StoragePreconditions.notBlankNotNegative(variant, amount);

        if (variant.equals(this.variant)) {
            long extractedAmount = Math.min(amount, this.amount);

            if (extractedAmount > 0) {
                updateSnapshots(context);
                this.amount -= extractedAmount;
                this.modCount.increment(context);

                if (this.amount == 0) {
                    this.variant = getBlankVariant();
                }
            }

            return extractedAmount;
        }

        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        return this.variant.isBlank();
    }

    @Override
    public @NotNull V getResource() {
        return this.variant;
    }

    @Override
    public long getAmount() {
        return this.amount;
    }

    @Override
    public long getCapacity() {
        return this.getCapacity(this.variant);
    }

    public long getCapacity(V variant) {
        return Math.min(this.getVariantCapacity(variant), this.capacity);
    }

    @ApiStatus.Internal
    public void incrementModCountUnsafe() {
        this.modCount.incrementUnsafe();
    }

    @Override
    protected ResourceAmount<V> createSnapshot() {
        return new ResourceAmount<>(this.variant, this.amount);
    }

    @Override
    protected void readSnapshot(@NotNull ResourceAmount<V> snapshot) {
        this.variant = snapshot.resource();
        this.amount = snapshot.amount();
    }
}
