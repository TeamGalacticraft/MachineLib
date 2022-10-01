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

package dev.galacticraft.machinelib.impl.storage.slot;

import dev.galacticraft.machinelib.api.storage.slot.StorageSlot;
import dev.galacticraft.machinelib.api.transfer.cache.ModCount;
import dev.galacticraft.machinelib.api.util.GenericApiUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@ApiStatus.Internal
public abstract class ResourceSlot<T, V extends TransferVariant<T>, S> extends SnapshotParticipant<ResourceAmount<V>> implements StorageSlot<T, V, S> {
    private final Predicate<V> filter;
    private final ModCount modCount;
    private final long capacity;
    private V variant = this.getBlankVariant();
    private long amount = 0;

    public ResourceSlot(long capacity, Predicate<V> filter, @NotNull ModCount modCount) {
        this.capacity = capacity;
        this.filter = filter;
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

    @Override
    public boolean isFull() {
        return this.amount >= this.getCapacity(this.variant);
    }

    @Override
    public boolean isEmpty() {
        assert !(this.isResourceBlank() && this.amount != 0);
        return this.isResourceBlank();
    }

    @Override
    public void setStack(@NotNull V variant, long amount) {
        StoragePreconditions.notNegative(amount);
        GenericApiUtil.noTransaction();
        this.modCount.increment();

        if (amount == 0) {
            variant = this.getBlankVariant();
        } else if (variant.isBlank()) {
            amount = 0;
        }

        this.variant = variant;
        this.amount = amount;
    }

    @Override
    public void setStack(@NotNull V variant, long amount, @NotNull TransactionContext context) {
        StoragePreconditions.notNegative(amount);
        this.updateSnapshots(context);
        this.modCount.increment(context);

        if (amount == 0) {
            variant = this.getBlankVariant();
        } else if (variant.isBlank()) {
            amount = 0;
        }

        this.variant = variant;
        this.amount = amount;
    }

    @Override
    public S extract(long amount) {
        if (this.isResourceBlank() || amount == 0) return this.getEmptyStack();
        amount = Math.min(amount, this.getVariantCapacity(this.variant));

        V v = this.variant;
        long extract = this.extract(v, amount);
        return this.createStack(v, extract);
    }

    @Override
    public S extract(long amount, @NotNull TransactionContext context) {
        if (this.isResourceBlank() || amount == 0) return this.getEmptyStack();
        amount = Math.min(amount, this.getVariantCapacity(this.variant));

        V v = this.variant;
        long extract = this.extract(v, amount, context);
        return this.createStack(v, extract);
    }

    @Override
    public Predicate<V> getFilter() {
        return this.filter;
    }

    @Override
    public boolean canAccept(V variant) {
        return this.filter.test(variant);
    }

    protected abstract @NotNull V getBlankVariant();

    protected abstract long getVariantCapacity(@NotNull V variant);

    @Contract(pure = true)
    protected abstract @NotNull S getEmptyStack();

    @Contract(pure = true)
    protected abstract @NotNull S createStack(@NotNull V variant, long amount);

    public @NotNull S copyStack() {
        if (this.isResourceBlank()) return this.getEmptyStack();
        return this.createStack(this.variant, this.amount);
    }

    @Override
    public long insert(V resource, long amount) {
        GenericApiUtil.noTransaction();
        StoragePreconditions.notBlankNotNegative(resource, amount);

        if (this.isResourceBlank() || this.variant.equals(resource)) {
            long insertedAmount = Math.min(amount, Math.min(this.capacity, getCapacity(resource)) - this.amount);
            if (insertedAmount > 0) {
                this.modCount.increment();
                if (this.isResourceBlank()) {
                    this.variant = resource;
                    this.amount = insertedAmount;
                } else {
                    this.amount += insertedAmount;
                }
            }
            return insertedAmount;
        }
        return 0;
    }

    @Override
    public long insert(@NotNull V resource, long amount, @NotNull TransactionContext context) {
        StoragePreconditions.notBlankNotNegative(resource, amount);

        if (this.isResourceBlank() || this.variant.equals(resource)) {
            long insertedAmount = Math.min(amount, Math.min(this.capacity, getCapacity(resource)) - this.amount);
            if (insertedAmount > 0) {
                this.updateSnapshots(context);
                this.modCount.increment(context);
                if (this.isResourceBlank()) {
                    this.variant = resource;
                    this.amount = insertedAmount;
                } else {
                    this.amount += insertedAmount;
                }
            }
            return insertedAmount;
        }
        return 0;
    }

    @Override
    public long extract(V resource, long amount) {
        GenericApiUtil.noTransaction();
        StoragePreconditions.notBlankNotNegative(resource, amount);

        if (resource.equals(this.variant)) {
            long extractedAmount = Math.min(amount, this.amount);
            if (extractedAmount > 0) {
                this.modCount.increment();
                this.amount -= extractedAmount;

                if (this.amount == 0) {
                    this.variant = getBlankVariant();
                }
            }
            return extractedAmount;
        }
        return this.extractInternal(resource, amount);
    }

    @Override
    public long extract(V resource, long amount, @NotNull TransactionContext context) {
        StoragePreconditions.notBlankNotNegative(resource, amount);

        if (resource.equals(this.variant)) {
            long extractedAmount = Math.min(amount, this.amount);
            if (extractedAmount > 0) {
                this.updateSnapshots(context);
                this.modCount.increment(context);
                this.amount -= extractedAmount;

                if (this.amount == 0) {
                    this.variant = getBlankVariant();
                }
            }
            return extractedAmount;
        }
        return 0;
    }

    @Override
    public long extractType(T resource, long amount) {
        GenericApiUtil.noTransaction();
        this.modCount.increment();

        return this.extractInternalType(resource, amount);
    }

    @Override
    public long extractType(T resource, long amount, @NotNull TransactionContext context) {
        this.updateSnapshots(context);
        this.modCount.increment(context);

        return this.extractInternalType(resource, amount);
    }

    @Override
    public boolean isResourceBlank() {
        assert !(this.amount == 0 && !this.variant.isBlank());
        return this.amount == 0;
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

    @Override
    public long getCapacity(V variant) {
        return Math.min(this.getVariantCapacity(variant), this.capacity);
    }

    @Override
    public long getVersion() {
        return this.getModCount();
    }

    @Override
    public boolean supportsInsertion() {
        return true; // I/O handling covered by exposed storages
    }

    @Override
    public boolean supportsExtraction() {
        return true; // I/O handling covered by exposed storages
    }

    @Override
    protected ResourceAmount<V> createSnapshot() {
        return new ResourceAmount<>(this.variant, this.amount);
    }

    @Override
    protected void readSnapshot(@NotNull ResourceAmount<V> snapshot) {
        assert !(snapshot.resource().isBlank() && snapshot.amount() != 0);
        this.variant = snapshot.resource();
        this.amount = snapshot.amount();
    }

    private long extractInternal(@NotNull V resource, long amount) {
        StoragePreconditions.notBlankNotNegative(resource, amount);

        if (resource.equals(this.variant)) {
            long extractedAmount = Math.min(amount, this.amount);

            if (extractedAmount > 0) {
                this.amount -= extractedAmount;

                if (this.amount == 0) {
                    this.variant = getBlankVariant();
                }
            }

            return extractedAmount;
        }

        return 0;
    }

    private long extractInternalType(@NotNull T resource, long amount) {
        StoragePreconditions.notNegative(amount);
        if (resource == this.getBlankVariant().getObject()) throw new IllegalArgumentException("blank");

        if (this.variant.getObject() == resource) {
            long extractedAmount = Math.min(amount, this.amount);

            if (extractedAmount > 0) {
                this.amount -= extractedAmount;

                if (this.amount == 0) {
                    this.variant = getBlankVariant();
                }
            }

            return extractedAmount;
        }

        return 0;
    }
}
