/*
 * Copyright (c) 2021-2024 Team Galacticraft
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

package dev.galacticraft.machinelib.api.util;

import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.storage.StorageAccess;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility methods for dealing with {@link Storage storages}.
 */
@SuppressWarnings("UnstableApiUsage")
public final class StorageHelper {
    @Contract(value = " -> fail", pure = true)
    private StorageHelper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static <Resource, Variant extends TransferVariant<Resource>, S extends Storage<Variant>> long calculateCapacity(Variant variant, @NotNull S storage, @Nullable TransactionContext context) {
        if (variant.isBlank()) return 0;

        long capacity = 0;
        for (StorageView<Variant> view : storage) {
            if (variant.equals(view.getResource())) {
                capacity += view.getCapacity();
            }
        }
        return capacity;
    }

    public static <Resource, Variant extends TransferVariant<Resource>, S extends Storage<Variant>> long calculateAmount(Variant variant, @NotNull S storage, @Nullable TransactionContext context) {
        if (variant.isBlank()) return 0;

        long amount = 0;
        for (StorageView<Variant> view : storage) {
            if (variant.equals(view.getResource())) {
                amount += view.getAmount();
            }
        }
        return amount;
    }

    public static <Resource, Variant extends TransferVariant<Resource>, S extends Storage<Variant>> long move(Variant variant, @Nullable StorageAccess<Resource> from, @Nullable S to, long maxAmount, @Nullable TransactionContext context) {
        if (from == null || to == null || variant.isBlank() || maxAmount == 0) return 0;
        StoragePreconditions.notNegative(maxAmount);
        long maxExtracted;
        try (Transaction test = Transaction.openNested(context)) {
            maxExtracted = from.extract(variant.getObject(), variant.getNbt(), maxAmount, test);
        }

        try (Transaction moveTransaction = Transaction.openNested(context)) {
            long accepted = to.insert(variant, maxExtracted, moveTransaction);

            if (from.extract(variant.getObject(), variant.getNbt(), accepted, moveTransaction) == accepted) {
                moveTransaction.commit();
                return accepted;
            }
        }

        return 0;
    }

    public static <Resource, Variant extends TransferVariant<Resource>, S extends Storage<Variant>> long move(Variant variant, @Nullable S from, @Nullable StorageAccess<Resource> to, long maxAmount, @Nullable TransactionContext context) {
        if (from == null || to == null || variant.isBlank() || maxAmount == 0) return 0;
        StoragePreconditions.notNegative(maxAmount);

        long maxExtracted;
        try (Transaction test = Transaction.openNested(context)) {
            maxExtracted = from.extract(variant, maxAmount, test);
        }

        try (Transaction moveTransaction = Transaction.openNested(context)) {
            long accepted = to.insert(variant.getObject(), variant.getNbt(), maxExtracted, moveTransaction);

            if (from.extract(variant, accepted, moveTransaction) == accepted) {
                moveTransaction.commit();
                return accepted;
            }
        }

        return 0;
    }

    public static <Variant, S extends Storage<Variant>> long move(Variant variant, @Nullable S from, @Nullable S to, long maxAmount, @Nullable TransactionContext context) {
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

    @Contract("_, null, _, _, _ -> false; _, !null, null, _, _ -> false")
    public static <Resource, Variant extends TransferVariant<Resource>, S extends Storage<Variant>> boolean moveAll(@NotNull ResourceFilter<Resource> filter, @Nullable S from, @Nullable StorageAccess<Resource> to, long maxAmount, @Nullable TransactionContext context) {
        if (from == null || to == null || maxAmount == 0 || !from.supportsExtraction()) return false;
        boolean changed = false;
        StoragePreconditions.notNegative(maxAmount);

        for (StorageView<Variant> view : from) {
            Variant variant;
            long maxExtracted = 0;
            try (Transaction test = Transaction.openNested(context)) {
                variant = view.getResource();
                if (filter.test(variant.getObject(), variant.getNbt())) {
                    maxExtracted = from.extract(variant, maxAmount, test);
                }
            }

            if (maxExtracted == 0 || variant.isBlank()) continue;

            try (Transaction moveTransaction = Transaction.openNested(context)) {
                long accepted = to.insert(variant.getObject(), variant.getNbt(), maxExtracted, moveTransaction);

                if (view.extract(variant, accepted, moveTransaction) == accepted) {
                    moveTransaction.commit();
                    changed = true;
                }
            }
        }
        return changed;
    }

    @Contract("_, null, _, _, _ -> false; _, !null, null, _, _ -> false")
    public static <Resource, Variant extends TransferVariant<Resource>, S extends Storage<Variant>> boolean moveAll(@NotNull ResourceFilter<Resource> filter, @Nullable S from, @Nullable S to, long maxAmount, @Nullable TransactionContext context) {
        if (from == null || to == null || maxAmount == 0 || !from.supportsExtraction() || !to.supportsInsertion()) return false;
        boolean changed = false;
        StoragePreconditions.notNegative(maxAmount);

        for (StorageView<Variant> view : from) {
            Variant variant;
            long maxExtracted = 0;
            try (Transaction test = Transaction.openNested(context)) {
                variant = view.getResource();
                if (filter.test(variant.getObject(), variant.getNbt())) {
                    maxExtracted = from.extract(variant, maxAmount, test);
                }
            }

            if (maxExtracted == 0 || variant.isBlank()) continue;

            try (Transaction moveTransaction = Transaction.openNested(context)) {
                long accepted = to.insert(variant, maxExtracted, moveTransaction);

                if (view.extract(variant, accepted, moveTransaction) == accepted) {
                    moveTransaction.commit();
                    changed = true;
                }
            }
        }
        return changed;
    }

    @Contract("null, _, _, _ -> false; !null, null, _, _ -> false")
    public static <Resource, Variant extends TransferVariant<Resource>, S extends Storage<Variant>> boolean moveAll(@Nullable S from, @Nullable S to, long maxAmount, @Nullable TransactionContext context) {
        if (from == null || to == null || maxAmount == 0 || !from.supportsExtraction() || !to.supportsInsertion()) return false;
        boolean changed = false;
        StoragePreconditions.notNegative(maxAmount);

        for (StorageView<Variant> view : from) {
            Variant variant;
            long maxExtracted;
            try (Transaction test = Transaction.openNested(context)) {
                variant = view.getResource();
                maxExtracted = from.extract(variant, maxAmount, test);
            }

            if (maxExtracted == 0 || variant.isBlank()) continue;

            try (Transaction moveTransaction = Transaction.openNested(context)) {
                long accepted = to.insert(variant, maxExtracted, moveTransaction);

                if (view.extract(variant, accepted, moveTransaction) == accepted) {
                    moveTransaction.commit();
                    changed = true;
                }
            }
        }
        return changed;
    }

    public record StorageContents(long amount, long capacity) {}
}
