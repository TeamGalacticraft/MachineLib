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
import dev.galacticraft.machinelib.api.storage.exposed.ExposedStorage;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.impl.storage.slot.UnmodifiableStorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

@ApiStatus.Internal
public final class ExposedSlotGroup<T, V extends TransferVariant<T>> implements ExposedStorage<T, V> {
    private final @NotNull ResourceStorage<T, V, ?> storage;
    private final boolean insertion;
    private final boolean extraction;
    private final boolean[] slots;

    public ExposedSlotGroup(@NotNull ResourceStorage<T, V, ?> storage, @NotNull SlotGroup group, boolean allowInsertion, boolean allowExtraction) {
        this.storage = storage;

        this.slots = new boolean[storage.size()];
        SlotGroup[] types = storage.getGroups();
        boolean insertion = false;
        boolean extraction = false;
        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(group)) {
                this.slots[i] = true;
                if (allowInsertion) insertion |= storage.canExposedInsert(i);
                if (allowExtraction) extraction |= storage.canExposedExtract(i);
            }
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
                if (this.slots[i] && this.storage.canExposedInsert(i)) {
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
                if (this.slots[i] && this.storage.canExposedExtract(i)) {
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
        return new ExtractionLimitingIterator(this.storage.iterator());
    }

    @Override
    public long getVersion() {
        return this.storage.getVersion();
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
            return UnmodifiableStorageView.maybeCreate(this.iterator.next(),
                    ExposedSlotGroup.this.supportsExtraction()
                            && ExposedSlotGroup.this.slots[this.index]
                            && ExposedSlotGroup.this.storage.canExposedExtract(this.index++));
        }
    }
}
