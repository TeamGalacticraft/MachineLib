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

package dev.galacticraft.machinelib.api.storage.slot;

import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public interface StorageSlot<T, V extends TransferVariant<T>, S> extends SingleSlotStorage<V> {
    long getModCount();

    long getModCountUnsafe();

    long getCapacity(V variant);

    boolean isFull();

    boolean isEmpty();

    Predicate<V> getFilter();

    boolean canAccept(V variant);

    @NotNull S copyStack();

    void setStack(@NotNull V variant, long amount);

    void setStack(@NotNull V variant, long amount, @NotNull TransactionContext context);


    @Override
    long insert(V resource, long amount, @NotNull TransactionContext context);

    long insert(V resource, long amount);

    default long simulateInsert(V resource, long amount) {
        return this.simulateInsert(resource, amount, null);
    }

    @Override
    long extract(V resource, long amount, @NotNull TransactionContext context);

    long extract(V resource, long amount);

    default long simulateExtract(V resource, long amount) {
        return this.simulateExtract(resource, amount, null);
    }

    default S simulateExtract(long amount, @Nullable TransactionContext context) {
        try (Transaction transaction = Transaction.openNested(context)) {
            return this.extract(amount, transaction);
        }
    }

    default S simulateExtract(long amount) {
        return this.simulateExtract(amount, null);
    }

    long extractType(T resource, long amount, @NotNull TransactionContext context);

    long extractType(T resource, long amount);

    default long simulateExtractType(T resource, long amount) {
        return this.simulateExtractType(resource, amount, null);
    }

    default long simulateExtractType(T resource, long amount, @Nullable TransactionContext context) {
        try (Transaction transaction = Transaction.openNested(context)) {
            return this.extractType(resource, amount, transaction);
        }
    }

    default boolean extractExact(V resource, long amount, @Nullable TransactionContext context) {
        try (Transaction transaction = Transaction.openNested(context)) {
            if (this.extract(resource, amount, transaction) == amount) {
                transaction.commit();
                return true;
            }
            return false;
        }
    }

    default boolean extractExact(V resource, long amount) {
        return this.extractExact(resource, amount, null);
    }

    default boolean simulateExtractExact(V resource, long amount) {
        return this.simulateExtractExact(resource, amount, null);
    }

    default boolean simulateExtractExact(V resource, long amount, @Nullable TransactionContext context) {
        try (Transaction transaction = Transaction.openNested(context)) {
            return this.extractExact(resource, amount, transaction);
        }
    }

    default boolean extractExact(T resource, long amount, @Nullable TransactionContext context) {
        try (Transaction transaction = Transaction.openNested(context)) {
            if (this.extractType(resource, amount, transaction) == amount) {
                transaction.commit();
                return true;
            }
            return false;
        }
    }

    default boolean extractExact(T resource, long amount) {
        return this.extractExact(resource, amount, null);
    }

    default boolean simulateExtractExact(T resource, long amount) {
        return this.simulateExtractExact(resource, amount, null);
    }

    default boolean simulateExtractExact(T resource, long amount, @Nullable TransactionContext context) {
        try (Transaction transaction = Transaction.openNested(context)) {
            return this.extractExact(resource, amount, transaction);
        }
    }

    S extract(long amount, @NotNull TransactionContext context);

    S extract(long amount);
}
