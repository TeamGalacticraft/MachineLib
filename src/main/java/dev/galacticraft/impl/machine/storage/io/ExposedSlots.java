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

package dev.galacticraft.impl.machine.storage.io;

import dev.galacticraft.api.machine.storage.ResourceStorage;
import dev.galacticraft.api.machine.storage.io.ExposedStorage;
import dev.galacticraft.api.machine.storage.io.SlotType;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

public class ExposedSlots<T, V extends TransferVariant<T>> implements ExposedStorage<T, V> {
    private final ResourceStorage<T, V, ?> storage;
    private final SlotType<T, V> type;
    private final boolean insertion;
    private final boolean extraction;
    private final int[] slots;

    public ExposedSlots(ResourceStorage<T, V, ?> storage, SlotType<T, V> type, boolean insert, boolean extract) {
        this.storage = storage;
        this.type = type;

        IntList list = new IntArrayList(1);
        SlotType<T, V>[] types = storage.getTypes();
        boolean insertion = false;
        boolean extraction = false;
        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(this.type)) {
                list.add(i);
                if (insert) insertion |= storage.canExposedInsert(i);
                if (extract) extraction |= storage.canExposedExtract(i);
            }
        }
        this.insertion = insertion;
        this.extraction = extraction;
        this.slots = list.toIntArray();
    }

    @Override
    public boolean supportsInsertion() {
        return this.insertion;
    }

    @Override
    public long insert(V resource, long maxAmount, TransactionContext transaction) {
        if (type.willAccept(resource) && this.supportsInsertion()) {
            long inserted = 0;
            for (int i = 0; i < this.slots.length; i++) {
                inserted += this.storage.insert(i, resource, maxAmount - inserted, transaction);
                if (inserted == maxAmount) {
                    break;
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
            for (int i = 0; i < this.slots.length; i++) {
                extracted += this.storage.extract(i, resource, maxAmount - extracted, transaction);
                if (extracted == maxAmount) {
                    break;
                }
            }
            return extracted;
        }
        return 0;
    }

    @Override
    public Iterator<StorageView<V>> iterator(TransactionContext transaction) {
        return new SkippingIterator(this.storage.iterator(transaction));
    }

    @Override
    public @Nullable StorageView<V> exactView(TransactionContext transaction, V resource) {
        return ExposedStorage.super.exactView(transaction, resource);
    }

    @Override
    public long getVersion() {
        return this.storage.getVersion();   // technically not perfect, as we don't expose all slots so there could be excessive churn
                                            // todo: individual modcounts? (is there that much to gain?)
    }

    private class SkippingIterator implements Iterator<StorageView<V>> {
        private int index = 0;
        private int invIndex = 0;
        private final Iterator<StorageView<V>> iterator;

        public SkippingIterator(Iterator<StorageView<V>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return this.iterator.hasNext() && this.index < ExposedSlots.this.slots.length;
        }

        @Override
        public StorageView<V> next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            int slot = ExposedSlots.this.slots[this.invIndex];
            if (slot != this.invIndex) {
                for (; this.invIndex < slot; this.invIndex++) {
                    this.iterator.next();
                }
            }
            this.index++;
            this.invIndex++;
            return this.iterator.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }

        @Override
        public void forEachRemaining(@NotNull Consumer<? super StorageView<V>> action) {
            Objects.requireNonNull(action);
            while (this.hasNext()) {
                action.accept(this.next());
            }
        }
    }
}
