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

package dev.galacticraft.impl.machine.storage.io;

import dev.galacticraft.api.machine.storage.ResourceStorage;
import dev.galacticraft.api.machine.storage.io.ExposedSlot;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class PlayerExposedStorageSlot<T, V extends TransferVariant<T>> implements ExposedSlot<T, V> {
    private final @NotNull ResourceStorage<T, V, ?> storage;
    private final @NotNull StorageView<V> slot;
    private final int index;
    private final boolean insertion;

    public PlayerExposedStorageSlot(@NotNull ResourceStorage<T, V, ?> storage, int index, boolean insert) {
        this.storage = storage;
        this.index = index;
        this.insertion = insert && storage.canPlayerInsert(index);
        this.slot = this.storage.getSlot(this.index);
    }

    @Override
    public boolean supportsInsertion() {
        return this.insertion;
    }

    @Override
    public long insert(V resource, long maxAmount, TransactionContext transaction) {
        if (this.supportsInsertion()) {
            if (this.storage.canAccept(this.index, resource)) {
                return this.storage.insert(this.index, resource, maxAmount, transaction);
            }
        }
        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }

    @Override
    public long extract(V resource, long maxAmount, TransactionContext transaction) {
        if (this.supportsExtraction()) {
            return this.storage.extract(this.index, resource, maxAmount, transaction);
        }
        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        return this.slot.isResourceBlank();
    }

    @Override
    public V getResource() {
        return this.slot.getResource();
    }

    @Override
    public long getAmount() {
        return this.slot.getAmount();
    }

    @Override
    public long getCapacity() {
        return this.slot.getCapacity();
    }

    @Override
    public Iterator<StorageView<V>> iterator() {
        return new ExtractionLimitingIterator(this.storage.iterator());
    }

    @Override
    public @NotNull V getResource(int slot) {
        return this.storage.getVariant(slot);
    }

    @Override
    public long getAmount(int slot) {
        return this.storage.getAmount(slot);
    }

    @Override
    public long getCapacity(int slot) {
        return this.storage.getMaxCount(slot);
    }

    @Override
    public @NotNull Predicate<V> getFilter(int slot) {
        return v -> this.storage.canAccept(slot, v);
    }

    @Override
    public long getVersion() {
        return this.storage.getVersion();
    }

    private class ExtractionLimitingIterator implements Iterator<StorageView<V>> {
        private int index = 0;
        private final Iterator<? extends StorageView<V>> iterator;

        public ExtractionLimitingIterator(Iterator<? extends StorageView<V>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public StorageView<V> next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            return UnmodifiableStorageView.maybeCreate(this.iterator.next(), PlayerExposedStorageSlot.this.supportsExtraction() && PlayerExposedStorageSlot.this.index == this.index++);
        }
    }
}
