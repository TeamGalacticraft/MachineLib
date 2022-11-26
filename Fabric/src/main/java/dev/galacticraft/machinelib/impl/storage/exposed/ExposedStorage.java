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

package dev.galacticraft.machinelib.impl.storage.exposed;

import dev.galacticraft.machinelib.api.storage.ResourceStorage;
import dev.galacticraft.machinelib.impl.storage.slot.UnmodifiableStorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.Predicate;

@ApiStatus.Internal
public final class ExposedStorage<T, V extends TransferVariant<T>> implements dev.galacticraft.machinelib.api.storage.exposed.ExposedStorage<T, V> {
    private final @NotNull ResourceStorage<T, V, ?> storage;
    private final boolean insertion;
    private final boolean extraction;

    public ExposedStorage(@NotNull ResourceStorage<T, V, ?> storage, boolean insert, boolean extract) {
        this.storage = storage;
        boolean insertion = false;
        boolean extraction = false;
        for (int i = 0; i < this.storage.size(); i++) {
            if (insert) insertion |= storage.canExposedInsert(i);
            if (extract) extraction |= storage.canExposedExtract(i);
        }
        this.insertion = insertion;
        this.extraction = extraction;
    }

    @Override
    public boolean supportsInsertion() {
        return this.insertion;
    }

    @Override
    public long insert(V resource, long maxAmount, TransactionContext transaction) {
        if (this.supportsInsertion()) {
            long inserted = 0;
            for (int i = 0; i < this.storage.size(); i++) {
                if (this.storage.canExposedInsert(i)) {
                    inserted += this.storage.insert(i, resource, maxAmount - inserted, transaction);
                    if (inserted == maxAmount) {
                        break;
                    }
                }
            }
            return inserted;
        }
        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return this.extraction;
    }

    @Override
    public long extract(V resource, long maxAmount, TransactionContext transaction) {
        if (this.supportsExtraction()) {
            long extracted = 0;
            for (int i = 0; i < this.storage.size(); i++) {
                if (this.storage.canExposedExtract(i)) {
                    extracted += this.storage.extract(i, resource, maxAmount - extracted, transaction);
                    if (extracted == maxAmount) {
                        break;
                    }
                }
            }
            return extracted;
        }
        return 0;
    }

    @Override
    public Iterator<StorageView<V>> iterator() {
        return new LimitedIterator(this.storage.iterator());
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
        return this.storage.getFilter(slot);
    }

    @Override
    public long getVersion() {
        return this.storage.getVersion();
    }

    private class LimitedIterator implements Iterator<StorageView<V>> {
        private int i = 0;
        private final Iterator<? extends StorageView<V>> iterator;

        public LimitedIterator(Iterator<? extends StorageView<V>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Contract(" -> new")
        @Override
        public @NotNull StorageView<V> next() {
            return UnmodifiableStorageView.maybeCreate(this.iterator.next(), ExposedStorage.this.supportsExtraction() && ExposedStorage.this.storage.canExposedExtract(i++));
        }
    }
}
