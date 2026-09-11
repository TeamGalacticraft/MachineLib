/*
 * Copyright (c) 2021-2026 Team Galacticraft
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
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
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
public final class StorageHelper {
    @Contract(value = " -> fail", pure = true)
    private StorageHelper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static <Variant extends TransferVariant<?>> long theoreticalCapacity(@NotNull Storage<Variant> storage) {
        long capacity = 0;
        for (StorageView<Variant> view : storage) {
            capacity += view.getCapacity();
        }

        return capacity;
    }

    /**
     * Calculates the capacity for a specific variant in a storage.
     *
     * @param variant the variant to calculate the capacity for
     * @param storage the storage to calculate the capacity in
     * @param context the transaction context to use
     * @param <Variant> the type of variant
     * @return the capacity of the resource in the storage
     */
    public static <Variant extends TransferVariant<?>> long capacity(@NotNull Variant variant, @NotNull Storage<Variant> storage, @Nullable TransactionContext context) {
        if (variant.isBlank()) return theoreticalCapacity(storage);

        long capacity = 0;
        try (Transaction transaction = Transaction.openNested(context)) {
            for (int i = 0; i < 128; i++) {
                if (storage.insert(variant, Integer.MAX_VALUE / 2, transaction) <= 0) break;
            }

            for (StorageView<Variant> view : storage) {
                if (variant.equals(view.getResource())) {
                    capacity += view.getCapacity();
                }
            }
        }
        return capacity;
    }

    public static <Variant extends TransferVariant<?>> long calculateAmount(Variant variant, @NotNull Storage<Variant> storage) {
        if (variant.isBlank()) return 0;

        long amount = 0;
        for (StorageView<Variant> view : storage.nonEmptyViews()) {
            if (variant.equals(view.getResource())) {
                amount += view.getAmount();
            }
        }
        return amount;
    }

    /**
     * Moves the specified amount of a resource from one storage to another.
     *
     * @param variant the variant to move
     * @param from the source storage
     * @param to the destination storage
     * @param maxAmount the maximum amount of resources to move
     * @param context the transaction context to use (if {@code null} a new transaction will be created and committed)
     * @param <Resource> the type of resource
     * @param <Variant> the type of variant
     * @return the amount of resources moved
     */
    public static <Resource, Variant extends TransferVariant<Resource>> long move(@NotNull Variant variant, @Nullable StorageAccess<Resource> from, @Nullable Storage<Variant> to, long maxAmount, @Nullable TransactionContext context) {
        if (from == null || to == null || variant.isBlank() || maxAmount == 0) return 0;
        StoragePreconditions.notNegative(maxAmount);

        // limit movement to the amount that can be extracted
        maxAmount = from.tryExtract(variant.getObject(), variant.getComponents(), maxAmount);

        try (Transaction transaction = Transaction.openNested(context)) {
            // insert the resource into the target storage
            long accepted = to.insert(variant, maxAmount, transaction);

            // extract the resource from the source storage
            // and check if the amount extracted is equal to the amount inserted
            if (from.extract(variant.getObject(), variant.getComponents(), accepted, transaction) == accepted) {
                // commit the transaction if the move was successful
                transaction.commit();
                return accepted;
            }
        }

        return 0;
    }

    /**
     * Moves the specified amount of a resource from one storage to another.
     *
     * @param variant the variant to move
     * @param from the source storage
     * @param to the destination storage
     * @param maxAmount the maximum amount of resources to move
     * @param context the transaction context to use (if {@code null} a new transaction will be created and committed)
     * @param <Resource> the type of resource
     * @param <Variant> the type of variant
     * @return the amount of resources moved
     */
    public static <Resource, Variant extends TransferVariant<Resource>> long move(Variant variant, @Nullable Storage<Variant> from, @Nullable StorageAccess<Resource> to, long maxAmount, @Nullable TransactionContext context) {
        if (from == null || to == null || variant.isBlank() || maxAmount == 0) return 0;
        StoragePreconditions.notNegative(maxAmount);

        // limit movement to the amount that can be inserted
        maxAmount = to.tryInsert(variant.getObject(), variant.getComponents(), maxAmount);

        try (Transaction transaction = Transaction.openNested(context)) {
            // extract the resource from the source storage
            long extracted = from.extract(variant, maxAmount, transaction);

            // insert the resource into the target storage
            // and check if the amount inserted is equal to the amount extracted
            if (to.insert(variant.getObject(), variant.getComponents(), extracted, transaction) == extracted) {
                // commit the transaction if the move was successful
                transaction.commit();
                return extracted;
            }
        }

        return 0;
    }

    /**
     * Moves the specified amount of a resource from one storage to another.
     *
     * @param variant the variant to move
     * @param from the source storage
     * @param to the destination storage
     * @param maxAmount the maximum amount of resources to move
     * @param context the transaction context to use (if {@code null} a new transaction will be created and committed)
     * @param <Variant> the type of variant
     * @return the amount of resources moved
     */
    public static <Variant> long move(Variant variant, @Nullable Storage<Variant> from, @Nullable Storage<Variant> to, long maxAmount, @Nullable TransactionContext context) {
        if (from == null || to == null) return 0;
        StoragePreconditions.notNegative(maxAmount);

        // limit movement to the amount that can be extracted
        try (Transaction test = Transaction.openNested(context)) {
            maxAmount = from.extract(variant, maxAmount, test);
        }

        try (Transaction transaction = Transaction.openNested(context)) {
            // insert the resource into the target storage
            long accepted = to.insert(variant, maxAmount, transaction);

            // extract the resource from the source storage
            // and check if the amount extracted is equal to the amount inserted
            if (from.extract(variant, accepted, transaction) == accepted) {
                transaction.commit();
                return accepted;
            }
        }

        return 0;
    }

    /**
     * Moves the item(s) contained in the slot into the other storage.
     *
     * @param from the source storage
     * @param to the destination storage
     * @param maxAmount the maximum number of items to move
     * @param context the transaction context to use (if {@code null} a new transaction will be created and committed)
     * @return the amount of resources moved
     */
    public static long move(@Nullable ItemResourceSlot from, @Nullable Storage<ItemVariant> to, long maxAmount, @Nullable TransactionContext context) {
        if (from == null || to == null || from.isEmpty() || maxAmount == 0) return 0;
        StoragePreconditions.notNegative(maxAmount);

        // limit movement to the amount that can be extracted
        maxAmount = from.tryExtract(maxAmount);
        if (maxAmount == 0) return 0;

        try (Transaction transaction = Transaction.openNested(context)) {
            assert from.getResource() != null;
            ItemVariant variant = ItemVariant.of(from.getResource(), from.getComponents());

            // insert the items into the target storage
            long accepted = to.insert(variant, maxAmount, transaction);

            // extract the items from the source storage
            // and check if the amount extracted is equal to the amount inserted
            if (from.extract(variant.getObject(), variant.getComponents(), accepted, transaction) == accepted) {
                // commit the transaction if the move was successful
                transaction.commit();
                return accepted;
            }
        }

        return 0;
    }

    /**
     * Moves the fluid(s) contained in the slot into the other storage.
     *
     * @param from the source storage
     * @param to the destination storage
     * @param maxAmount the maximum number of fluids to move
     * @param context the transaction context to use (if {@code null} a new transaction will be created and committed)
     * @return the amount of resources moved
     */
    public static long move(@Nullable FluidResourceSlot from, @Nullable Storage<FluidVariant> to, long maxAmount, @Nullable TransactionContext context) {
        if (from == null || to == null || from.isEmpty() || maxAmount == 0) return 0;
        StoragePreconditions.notNegative(maxAmount);

        // limit movement to the amount that can be extracted
        maxAmount = from.tryExtract(maxAmount);
        if (maxAmount == 0) return 0;

        try (Transaction transaction = Transaction.openNested(context)) {
            assert from.getResource() != null;
            FluidVariant variant = FluidVariant.of(from.getResource(), from.getComponents());

            // insert the items into the target storage
            long accepted = to.insert(variant, maxAmount, transaction);

            // extract the items from the source storage
            // and check if the amount extracted is equal to the amount inserted
            if (from.extract(variant.getObject(), variant.getComponents(), accepted, transaction) == accepted) {
                // commit the transaction if the move was successful
                transaction.commit();
                return accepted;
            }
        }

        return 0;
    }

    /**
     * Moves all resources (matching the slot filter) from the storage into the slot.
     *
     * @param from the source storage
     * @param to the destination slot
     * @param maxAmount the maximum number of resources to move
     * @param context the transaction context to use (if {@code null} a new transaction will be created and committed)
     * @return the amount of resources moved
     */
    public static <Resource, Variant extends TransferVariant<Resource>> long move(@Nullable Storage<Variant> from, @Nullable ResourceSlot<Resource> to, long maxAmount, @Nullable TransactionContext context) {
        if (from == null || to == null || !from.supportsExtraction() || maxAmount == 0) return 0;
        StoragePreconditions.notNegative(maxAmount);

        Variant variant = findCompatibleVariant(to.getFilter(), from, to);
        if (variant == null) return 0;

        // limit movement to the amount that can be inserted
        maxAmount = Math.min(maxAmount, to.tryInsert(variant.getObject(), variant.getComponents(), maxAmount));
        if (maxAmount == 0) return 0;

        try (Transaction transaction = Transaction.openNested(context)) {
            // extract the items into the source storage
            long extracted = from.extract(variant, maxAmount, transaction);

            // insert the items from the target storage
            // and check if the amount inserted is equal to the amount inserted
            if (to.insert(variant.getObject(), variant.getComponents(), extracted, transaction) == extracted) {
                // commit the transaction if the move was successful
                transaction.commit();
                return extracted;
            }
        }

        return 0;
    }

    /**
     * Moves all items (matching the slot filter) from the storage into the slot.
     *
     * @param from the source storage
     * @param to the destination slot
     * @param maxAmount the maximum number of items to move
     * @param context the transaction context to use (if {@code null} a new transaction will be created and committed)
     * @return whether any resources were moved
     */
    @Contract("_, null, _, _, _ -> false; _, !null, null, _, _ -> false")
    public static <Resource, Variant extends TransferVariant<Resource>> boolean moveAll(@NotNull ResourceFilter<Resource> filter, @Nullable Storage<Variant> from, @Nullable StorageAccess<Resource> to, long maxAmount, @Nullable TransactionContext context) {
        if (from == null || to == null || maxAmount == 0 || !from.supportsExtraction()) return false;
        boolean changed = false;
        StoragePreconditions.notNegative(maxAmount);

        for (StorageView<Variant> view : from) {
            Variant variant;
            long maxExtracted = 0;
            try (Transaction test = Transaction.openNested(context)) {
                variant = view.getResource();
                if (filter.test(variant.getObject(), variant.getComponents())) {
                    maxExtracted = from.extract(variant, maxAmount, test);
                }
            }

            if (maxExtracted == 0 || variant.isBlank()) continue;

            try (Transaction moveTransaction = Transaction.openNested(context)) {
                long accepted = to.insert(variant.getObject(), variant.getComponents(), maxExtracted, moveTransaction);

                if (view.extract(variant, accepted, moveTransaction) == accepted) {
                    moveTransaction.commit();
                    changed = true;
                }
            }
        }
        return changed;
    }

    @Contract("_, null, _, _, _ -> false; _, !null, null, _, _ -> false")
    public static <Resource, Variant extends TransferVariant<Resource>> boolean moveAll(@NotNull ResourceFilter<Resource> filter, @Nullable Storage<Variant> from, @Nullable Storage<Variant> to, long maxAmount, @Nullable TransactionContext context) {
        if (from == null || to == null || maxAmount == 0 || !from.supportsExtraction() || !to.supportsInsertion())
            return false;
        boolean changed = false;
        StoragePreconditions.notNegative(maxAmount);

        for (StorageView<Variant> view : from) {
            Variant variant;
            long maxExtracted = 0;
            try (Transaction test = Transaction.openNested(context)) {
                variant = view.getResource();
                if (filter.test(variant.getObject(), variant.getComponents())) {
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
    public static <Resource, Variant extends TransferVariant<Resource>> boolean moveAll(@Nullable Storage<Variant> from, @Nullable Storage<Variant> to, long maxAmount, @Nullable TransactionContext context) {
        if (from == null || to == null || maxAmount == 0 || !from.supportsExtraction() || !to.supportsInsertion())
            return false;
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

    /**
     * Finds a variant in the storage that is compatible with the given filter.
     *
     * @param filter the filter to test the variants against
     * @param storage the storage to search in
     * @param <Resource> the type of resource
     * @param <Variant> the type of variant
     * @return a variant that is compatible with the filter, or {@code null} if none was found
     */
    public static <Resource, Variant extends TransferVariant<Resource>> @Nullable Variant findCompatibleVariant(@NotNull ResourceFilter<Resource> filter, @NotNull Storage<Variant> storage) {
        for (StorageView<Variant> view : storage.nonEmptyViews()) {
            Variant resource = view.getResource();
            if (filter.test(resource.getObject(), resource.getComponents())) return resource;
        }
        return null;
    }

    /**
     * Finds a variant in the storage that is compatible with the given slot.
     * If the slot is not empty, the variant must match the slot's resource and components.
     * Otherwise, the variant must be compatible with the slot's filter.
     *
     * @param filter the filter to test the variants against
     * @param storage the storage to search in
     * @param slot the slot to test the variants against
     * @param <Resource> the type of resource
     * @param <Variant> the type of variant
     * @return a variant that is compatible with the filter and slot, or {@code null} if none was found
     */
    public static <Resource, Variant extends TransferVariant<Resource>> @Nullable Variant findCompatibleVariant(@NotNull ResourceFilter<Resource> filter, @NotNull Storage<Variant> storage, ResourceSlot<Resource> slot) {
        if (!slot.isEmpty()) {
            for (StorageView<Variant> view : storage.nonEmptyViews()) {
                Variant resource = view.getResource();
                if (resource.getObject() == slot.getResource() && resource.getComponents().equals(slot.getComponents()))
                    return resource;
            }
        } else {
            for (StorageView<Variant> view : storage.nonEmptyViews()) {
                Variant resource = view.getResource();
                if (filter.test(resource.getObject(), resource.getComponents())) return resource;
            }
        }
        return null;
    }
}
