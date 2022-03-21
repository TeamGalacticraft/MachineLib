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

package dev.galacticraft.impl.util;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class GenericStorageUtil {
    public static <T, S extends Storage<T>> long move(T variant, @Nullable S from, @Nullable S to, long maxAmount, @Nullable TransactionContext context) {
        if (from == null || to == null) return 0;
        StoragePreconditions.notNegative(maxAmount);

        long maxExtracted;
        try (Transaction extractionTestTransaction = Transaction.openNested(context)) {
            maxExtracted = from.extract(variant, maxAmount, extractionTestTransaction);
        }

        try (Transaction moveTransaction = Transaction.openNested(context)) {
            long accepted = to.insert(variant, maxExtracted, moveTransaction);

            if (from.extract(variant, accepted, moveTransaction) == accepted) {
                moveTransaction.commit();
                return accepted;
            }
        }

        return 0;
    }

    public static <T, S extends Storage<T>> void moveAll(@Nullable S from, @Nullable S to, long maxPerTransaction, @Nullable TransactionContext context) {
        if (from == null || to == null) return;
        StoragePreconditions.notNegative(maxPerTransaction);
        LongList list = new LongArrayList();
        try (Transaction indexingTransaction = Transaction.openNested(context)) {
            for (Iterator<StorageView<T>> it = from.iterator(indexingTransaction); it.hasNext(); ) {
                StorageView<T> storageView = it.next();
                list.add(to.insert(storageView.getResource(), storageView.extract(storageView.getResource(), maxPerTransaction, indexingTransaction), indexingTransaction));
            }
        }

        int i = 0;
        try (Transaction extractingTransaction = Transaction.openNested(context)) {
            for (Iterator<StorageView<T>> it = from.iterator(extractingTransaction); it.hasNext(); ) {
                StorageView<T> storageView = it.next();
                list.add(to.insert(storageView.getResource(), storageView.extract(storageView.getResource(), list.getLong(i++), extractingTransaction), extractingTransaction));
            }
            extractingTransaction.commit();
        }
    }
}
