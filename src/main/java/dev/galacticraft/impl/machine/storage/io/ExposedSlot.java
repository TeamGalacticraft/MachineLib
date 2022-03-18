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
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

public class ExposedSlot<T, V extends TransferVariant<T>> implements ExposedStorage<T, V>, StorageView<V> {
    private final ResourceStorage<T, V, ?> storage;
    private final StorageView<V> slot;
    private final int index;
    private final boolean insertion;
    private final boolean extraction;

    public ExposedSlot(ResourceStorage<T, V, ?> storage, int index, boolean insert, boolean extract) {
        this.storage = storage;
        this.index = index;
        this.insertion = insert && storage.canExposedInsert(index);
        this.extraction = extract && storage.canExposedExtract(index);
        this.slot = this.storage.getSlot(this.index);
    }

    @Override
    public boolean supportsInsertion() {
        return this.insertion;
    }

    @Override
    public long insert(V resource, long maxAmount, TransactionContext transaction) {
        if (this.supportsInsertion()) {
            if (this.storage.canAccept(index, resource)) {
                return this.storage.insert(this.index, resource, maxAmount, transaction);
            }
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
            return this.storage.extract(index, resource, maxAmount, transaction);
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
    public Iterator<StorageView<V>> iterator(TransactionContext transaction) {
        return new SingleTransactiveIterator(transaction);
    }

    @Override
    public @Nullable StorageView<V> exactView(TransactionContext transaction, V resource) {
        return this;
    }

    @Override
    public long getVersion() {
        return this.storage.getVersion();
    }

    private class SingleTransactiveIterator implements Iterator<StorageView<V>>, Transaction.CloseCallback {
        private boolean complete = false;

        private SingleTransactiveIterator(TransactionContext context) {
            context.addCloseCallback(this);
        }

        @Override
        public boolean hasNext() {
            return !complete;
        }

        @Override
        public StorageView<V> next() {
            if (this.complete) {
                throw new NoSuchElementException();
            }
            return ExposedSlot.this;
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

        @Override
        public void onClose(TransactionContext transaction, TransactionContext.Result result) {
            this.complete = true;
        }
    }
}
