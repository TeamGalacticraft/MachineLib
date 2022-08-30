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

package dev.galacticraft.api.machine.storage;

import com.mojang.datafixers.util.Either;
import dev.galacticraft.api.machine.storage.io.*;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.Collection;

public interface ResourceStorage<T, V extends TransferVariant<T>, S> extends ConfiguredStorage<T, V>, Storage<V>, Clearable {
    /**
     * Returns the number of slots in the storage.
     * @return The number of slots in the storage.
     */
    int size();

    /**
     * Returns whether the storage contains no items.
     * @return Whether the storage contains no items.
     */
    boolean isEmpty();

    /**
     * Returns a resource stack with the amount and type of the resource in the storage.
     * @param slot The slot to get the stack from.
     * @return A resource stack with the amount and type of the resource in the storage.
     */
    @NotNull S getStack(int slot);

    /**
     * Returns the resource variant contained in the slot.
     * Should NEVER be modified.
     * @param slot The slot to get the variant from.
     * @return The resource variant contained in the slot.
     */
    @NotNull V getVariant(int slot);

    /**
     * The amount of resources in the given slot.
     * @param slot The slot to get the amount of resources from.
     * @return The amount of resources in the slot.
     */
    long getAmount(int slot);

    /**
     * Returns the type of resource contained in this storage.
     * @return The type of resource contained in this storage.
     */
    @NotNull ResourceType<T, V> getResource();

    /**
     * Returns whether the storage allows extraction from the given slot.
     * @param slot The slot to check.
     * @return Whether the storage allows extraction from the given slot.
     */
    boolean canExposedExtract(int slot);

    /**
     * Returns whether the storage allows insertion into the given slot.
     * @param slot The slot to check.
     * @return Whether the storage allows insertion into the given slot.
     */
    boolean canExposedInsert(int slot);

    /**
     * Simulates the extraction of all resources from the given slot.
     * @param slot The slot to extract from.
     * @return The extracted resources.
     */
    default @NotNull S simulateExtract(int slot) {
        return this.simulateExtract(slot, (TransactionContext) null);
    }

    /**
     * Simulates the extraction of the given amount of resources from the given slot.
     * @param slot The slot to extract from.
     * @param amount The amount of resources to extract.
     * @return The extracted resources.
     */
    default @NotNull S simulateExtract(int slot, long amount) {
        return this.simulateExtract(slot, amount, null);
    }

    /**
     * Simulates the extraction of all resources from the given slot if it matches the tag.
     * @param slot The slot to extract from.
     * @param tag The tag to match.
     * @return The extracted resources.
     */
    default @NotNull S simulateExtract(int slot, @NotNull TagKey<T> tag) {
        return this.simulateExtract(slot, tag, null);
    }

    /**
     * Simulates the extraction of the given amount of resources from the given slot if it matches the tag.
     * @param slot The slot to extract from.
     * @param tag The tag to match.
     * @param amount The amount of resources to extract.
     * @return The extracted resources.
     */
    default @NotNull S simulateExtract(int slot, @NotNull TagKey<T> tag, long amount) {
        return this.simulateExtract(slot, tag, amount, null);
    }

    /**
     * Simulates the extraction of all resources of the given type from the given slot.
     * @param slot The slot to extract from.
     * @param resource The type of resource to extract.
     * @return The amount of extracted resources.
     */
    default long simulateExtract(int slot, @NotNull T resource) {
        return this.simulateExtract(slot, resource, null);
    }

    /**
     * Simulates the extraction of all resources of the given type and amount from the given slot.
     * @param slot The slot to extract from.
     * @param resource The type of resource to extract.
     * @param amount The amount of resources to extract.
     * @return The amount of extracted resources.
     */
    default long simulateExtract(int slot, @NotNull T resource, long amount) {
        return this.simulateExtract(slot, resource, amount, null);
    }

    /**
     * Simulates the replacement of the resource in the given slot with the given resource and amount.
     * Does not guarantee that the resources will be replaced. Be sure to check the returned stack!
     * @param slot The slot to replace.
     * @param variant The variant of the resource to replace with.
     * @param amount The amount of resources to replace with.
     * @return The extracted resources.
     */
    default @NotNull S simulateReplace(int slot, @NotNull V variant, long amount) {
        return this.simulateReplace(slot, variant, amount, null);
    }

    /**
     * Simulates the insertion of the given amount of resources into the given slot.
     * @param slot The slot to insert into.
     * @param variant The variant of the resource to insert.
     * @param amount The amount of resources to insert.
     * @return The amount of inserted resources.
     */
    default long simulateInsert(int slot, @NotNull V variant, long amount) {
        return this.simulateInsert(slot, variant, amount, null);
    }

    /**
     * Simulates the extraction of the given amount of resources of the given type from the given slot.
     * @param slot The slot to extract from.
     * @param variant The variant of the resource to extract.
     * @return The amount of extracted resources.
     */
    default long simulateExtract(int slot, @NotNull V variant) {
        return this.simulateExtract(slot, variant, null);
    }

    /**
     * Simulates the extraction of the given amount of resources of the given type from the given slot.
     * @param slot The slot to extract from.
     * @param variant The variant of the resource to extract.
     * @param amount The amount of resources to extract.
     * @return The amount of extracted resources.
     */
    default long simulateExtract(int slot, @NotNull V variant, long amount) {
        return this.simulateExtract(slot, variant, amount, null);
    }

    /**
     * Simulates the extraction of all resources from the given slot.
     * @param slot The slot to extract from.
     * @return The extracted resources.
     */
    default @NotNull S simulateExtract(int slot, @Nullable TransactionContext context) {
        try (Transaction transaction = Transaction.openNested(context)) {
            return this.extract(slot, transaction);
        }
    }

    /**
     * Simulates the extraction of the given amount of resources from the given slot.
     * @param slot The slot to extract from.
     * @param amount The amount of resources to extract.
     * @return The extracted resources.
     */
    default @NotNull S simulateExtract(int slot, long amount, @Nullable TransactionContext context) {
        try (Transaction transaction = Transaction.openNested(context)) {
            return this.extract(slot, amount, transaction);
        }
    }

    /**
     * Simulates the extraction of all resources from the given slot if it matches the tag.
     * @param slot The slot to extract from.
     * @param tag The tag to match.
     * @return The extracted resources.
     */
    default @NotNull S simulateExtract(int slot, @NotNull TagKey<T> tag, @Nullable TransactionContext context) {
        try (Transaction transaction = Transaction.openNested(context)) {
            return this.extract(slot, tag, transaction);
        }
    }

    /**
     * Simulates the extraction of the given amount of resources from the given slot if it matches the tag.
     * @param slot The slot to extract from.
     * @param tag The tag to match.
     * @param amount The amount of resources to extract.
     * @return The extracted resources.
     */
    default @NotNull S simulateExtract(int slot, @NotNull TagKey<T> tag, long amount, @Nullable TransactionContext context) {
        try (Transaction transaction = Transaction.openNested(context)) {
            return this.extract(slot, tag, amount, transaction);
        }
    }

    /**
     * Simulates the extraction of all resources of the given type from the given slot.
     * @param slot The slot to extract from.
     * @param resource The type of resource to extract.
     * @return The amount of extracted resources.
     */
    default long simulateExtract(int slot, @NotNull T resource, @Nullable TransactionContext context) {
        try (Transaction transaction = Transaction.openNested(context)) {
            return this.extract(slot, resource, transaction);
        }
    }

    /**
     * Simulates the extraction of all resources of the given type and amount from the given slot.
     * @param slot The slot to extract from.
     * @param resource The type of resource to extract.
     * @param amount The amount of resources to extract.
     * @return The amount of extracted resources.
     */
    default long simulateExtract(int slot, @NotNull T resource, long amount, @Nullable TransactionContext context) {
        try (Transaction transaction = Transaction.openNested(context)) {
            return this.extract(slot, resource, amount, transaction);
        }
    }

    /**
     * Simulates the replacement of the resource in the given slot with the given resource and amount.
     * Does not guarantee that the resources will be replaced. Be sure to check the returned stack!
     * @param slot The slot to replace.
     * @param variant The variant of the resource to replace with.
     * @param amount The amount of resources to replace with.
     * @return The extracted resources.
     */
    default @NotNull S simulateReplace(int slot, @NotNull V variant, long amount, @Nullable TransactionContext context) {
        try (Transaction transaction = Transaction.openNested(context)) {
            return this.replace(slot, variant, amount, transaction);
        }
    }

    /**
     * Simulates the insertion of the given amount of resources into the given slot.
     * @param slot The slot to insert into.
     * @param variant The variant of the resource to insert.
     * @param amount The amount of resources to insert.
     * @return The amount of inserted resources.
     */
    default long simulateInsert(int slot, @NotNull V variant, long amount, @Nullable TransactionContext context) {
        try (Transaction transaction = Transaction.openNested(context)) {
            return this.insert(slot, variant, amount, transaction);
        }
    }

    /**
     * Simulates the extraction of the given amount of resources of the given type from the given slot.
     * @param slot The slot to extract from.
     * @param variant The variant of the resource to extract.
     * @return The amount of extracted resources.
     */
    default long simulateExtract(int slot, @NotNull V variant, @Nullable TransactionContext context) {
        try (Transaction transaction = Transaction.openNested(context)) {
            return this.extract(slot, variant, transaction);
        }
    }

    /**
     * Simulates the extraction of the given amount of resources of the given type from the given slot.
     * @param slot The slot to extract from.
     * @param variant The variant of the resource to extract.
     * @param amount The amount of resources to extract.
     * @return The amount of extracted resources.
     */
    default long simulateExtract(int slot, @NotNull V variant, long amount, @Nullable TransactionContext context) {
        try (Transaction transaction = Transaction.openNested(context)) {
            return this.extract(slot, variant, amount, transaction);
        }
    }

    /**
     * Extracts all resources from the given slot.
     * @param slot The slot to extract from.
     * @return The extracted resources.
     */
    default @NotNull S extract(int slot) {
        return this.extract(slot, (TransactionContext) null);
    }

    /**
     * Extracts the given amount of resources from the given slot.
     * @param slot The slot to extract from.
     * @param amount The amount of resources to extract.
     * @return The extracted resources.
     */
    default @NotNull S extract(int slot, long amount) {
        return this.extract(slot, amount, null);
    }

    /**
     * Extracts all resources from the given slot if it matches the tag.
     * @param slot The slot to extract from.
     * @param tag The tag to match.
     * @return The extracted resources.
     */
    default @NotNull S extract(int slot, @NotNull TagKey<T> tag) {
        return this.extract(slot, tag, null);
    }

    /**
     * Extracts the given amount of resources from the given slot if it matches the tag.
     * @param slot The slot to extract from.
     * @param tag The tag to match.
     * @param amount The amount of resources to extract.
     * @return The extracted resources.
     */
    default @NotNull S extract(int slot, @NotNull TagKey<T> tag, long amount) {
        return this.extract(slot, tag, amount, null);
    }

    /**
     * Extracts all resources of the given type from the given slot.
     * @param slot The slot to extract from.
     * @param resource The type of resource to extract.
     * @return The amount of extracted resources.
     */
    default long extract(int slot, @NotNull T resource) {
        return this.extract(slot, resource, null);
    }

    /**
     * Extracts all resources of the given type and amount from the given slot.
     * @param slot The slot to extract from.
     * @param resource The type of resource to extract.
     * @param amount The amount of resources to extract.
     * @return The amount of extracted resources.
     */
    default long extract(int slot, @NotNull T resource, long amount) {
        return this.extract(slot, resource, amount, null);
    }

    /**
     * Replaces the resource in the given slot with the given resource and amount.
     * Does not guarantee that the resources will be replaced. Be sure to check the returned stack!
     * @param slot The slot to replace.
     * @param variant The variant of the resource to replace with.
     * @param amount The amount of resources to replace with.
     * @return The extracted resources.
     */
    default @NotNull S replace(int slot, @NotNull V variant, long amount) {
        return this.replace(slot, variant, amount, null);
    }

    /**
     * Inserts the given amount of resources into the given slot.
     * @param slot The slot to insert into.
     * @param variant The variant of the resource to insert.
     * @param amount The amount of resources to insert.
     * @return The amount of inserted resources.
     */
    default long insert(int slot, @NotNull V variant, long amount) {
        return this.insert(slot, variant, amount, null);
    }

    /**
     * Extracts the given amount of resources of the given type from the given slot.
     * @param slot The slot to extract from.
     * @param variant The variant of the resource to extract.
     * @return The amount of extracted resources.
     */
    default long extract(int slot, @NotNull V variant) {
        return this.extract(slot, variant, null);
    }

    /**
     * Extracts the given amount of resources of the given type from the given slot.
     * @param slot The slot to extract from.
     * @param variant The variant of the resource to extract.
     * @param amount The amount of resources to extract.
     * @return The amount of extracted resources.
     */
    default long extract(int slot, @NotNull V variant, long amount) {
        return this.extract(slot, variant, amount, null);
    }

    /**
     * Extracts all resources from the given slot.
     * @param slot The slot to extract from.
     * @param context The transaction context.
     * @return The extracted resources.
     */
    default @NotNull S extract(int slot, @Nullable TransactionContext context) {
        return this.extract(slot, Long.MAX_VALUE, context);
    }

    /**
     * Extracts the all resources from the given slot if they match the tag.
     * @param slot The slot to extract from.
     * @param tag The tag to match.
     * @param context The transaction context.
     * @return The extracted resources.
     */
    default @NotNull S extract(int slot, @NotNull TagKey<T> tag, @Nullable TransactionContext context) {
        return this.extract(slot, tag, Long.MAX_VALUE, context);
    }

    /**
     * Extracts all resources of the given (raw) type from the given slot.
     * @param slot The slot to extract from.
     * @param resource The type of resource to extract.
     * @param context The transaction context.
     * @return The amount of extracted resources.
     */
    default long extract(int slot, @NotNull T resource, @Nullable TransactionContext context) {
        return this.extract(slot, resource, Long.MAX_VALUE, context);
    }

    /**
     * Extracts all resources from the given slot of the given type.
     * @param slot The slot to extract from.
     * @param variant The variant of the resource to extract.
     * @param context The transaction context.
     * @return The amount of extracted resources.
     */
    default long extract(int slot, @NotNull V variant, @Nullable TransactionContext context) {
        return this.extract(slot, variant, Long.MAX_VALUE, context);
    }

    /**
     * Replaces the resources in the given slot with the given resources.
     * Does not guarantee that the resources will be replaced. Be sure to check the returned stack!
     * @param slot The slot to replace.
     * @param variant The variant of the resource to replace with.
     * @param amount The amount of resources to replace with.
     * @param context The transaction context.
     * @return The leftover resources.
     */
    @NotNull S replace(int slot, @NotNull V variant, long amount, @Nullable TransactionContext context);

    /**
     * Inserts the given amount of resources into the given slot.
     * @param slot The slot to insert into.
     * @param variant The variant of the resource to insert.
     * @param amount The amount of resources to insert.
     * @param context The transaction context.
     * @return The amount of inserted resources.
     */
    long insert(int slot, @NotNull V variant, long amount, @Nullable TransactionContext context);

    /**
     * Extracts the given amount of resources from the given slot.
     * @param slot The slot to extract from.
     * @param amount The amount of resources to extract.
     * @param context The transaction context.
     * @return The extracted resources.
     */
    @NotNull S extract(int slot, long amount, @Nullable TransactionContext context);

    /**
     * Extracts the given amount of resources from the given slot if they match the tag.
     * @param slot The slot to extract from.
     * @param tag The tag to match.
     * @param amount The amount of resources to extract.
     * @param context The transaction context.
     * @return The extracted resources.
     */
    @NotNull S extract(int slot, @NotNull TagKey<T> tag, long amount, @Nullable TransactionContext context);

    /**
     * Extracts the given amount of resources of the given type from the given slot.
     * @param slot The slot to extract from.
     * @param resource The type of resource to extract.
     * @param amount The amount of resources to extract.
     * @param context The transaction context.
     * @return The amount of extracted resources.
     */
    long extract(int slot, @NotNull T resource, long amount, @Nullable TransactionContext context);

    /**
     * Extracts the given amount of resources from the given slot of the given type.
     * @param slot The slot to extract from.
     * @param variant The variant of the resource to extract.
     * @param amount The amount of resources to extract.
     * @param context The transaction context.
     * @return The amount of extracted resources.
     */
    long extract(int slot, @NotNull V variant, long amount, @Nullable TransactionContext context);

    /**
     * Returns the maximum amount of resources that can be stored in the given slot.
     * This is context specific; for example, a slot with a capacity of 64 can only store 16 snowballs.
     * @param slot The slot to check.
     * @return The maximum amount of resources that can be stored in the given slot.
     */
    long getMaxCount(int slot);

    /**
     * Returns the modification count of this inventory.
     * Do NOT call during a transaction
     * @return the modification count of this inventory.
     */
    int getModCount();

    /**
     * Returns the modification count of this inventory.
     * The modification count may go down if a transaction fails.
     * @return the modification count of this inventory.
     */
    int getModCountUnsafe();

    /**
     * Returns the modification count of a particular slot in this inventory.
     * Do NOT call during a transaction
     * @param slot The slot to check.
     * @return the modification count of a particular slot in this inventory.
     */
    int getSlotModCount(int slot);

    /**
     * Returns whether any more resources can be inserted into the given slot.
     * Note that a slot can be both empty and full at the same time.
     * @param slot The slot to check.
     * @return Whether any more resources can be inserted into the given slot.
     */
    boolean isFull(int slot);

    /**
     * Returns whether the given slot contains any resources.
     * Note that a slot can be both empty and full at the same time.
     * @param slot The slot to check.
     * @return Whether the given slot contains any resources.
     */
    boolean isEmpty(int slot);

    /**
     * Returns an internal storage representing the given slot.
     * @param slot The index of the slot.
     * @return An internal storage representing the given slot.
     */
    @NotNull StorageSlot<T, V> getSlot(int slot);

    /**
     * Returns whether the player can access this inventory.
     * @param player The player to check.
     * @return Whether the player can access this inventory.
     */
    boolean canAccess(@NotNull Player player);

    /**
     * Returns whether the given slot can contain the given resource.
     * @param slot The slot to check.
     * @param variant The variant to check.
     * @return Whether the given slot can contain the given resource.
     */
    boolean canAccept(int slot, @NotNull V variant);

    /**
     * Returns the number of the given resource in this inventory.
     * @param resource The resource to check.
     *                 Note: This is not a variant. Items with different NBT will also be included.
     * @return The number of the given resource in this inventory.
     * @deprecated Use {@link #count(V)} instead.
     */
    @Deprecated
    long count(@NotNull T resource);

    /**
     * Returns the number of the given resource in this inventory.
     * @param resource The resource to check.
     * @return The number of the given resource in this inventory.
     */
    long count(@NotNull V resource);

    /**
     * Returns whether any of the given resources are present in this inventory.
     * @param resources The resources to check.
     * @return Whether any of the given resources are present in this inventory.
     */
    boolean containsAny(@NotNull Collection<T> resources);

    /**
     * Serializes this storage to nbt.
     * @return The serialized nbt.
     */
    @NotNull Tag writeNbt();

    /**
     * Deserializes a storage from nbt.
     * @param nbt The nbt to deserialize.
     */
    void readNbt(@NotNull Tag nbt);

    /**
     * Clears the inventory.
     */
    @Override
    void clearContent();

    /**
     * Returns a read-only view of this storage.
     * @return A read-only view of this storage.
     */
    ExposedStorage<T, V> view();

    @Override
    default long getVersion() {
        return this.getModCount();
    }

    /**
     * Sets the items in the given slot without using a transaction and without modifying the {@link #getModCount() mod count}.
     * @param slot The slot to set.
     * @param variant The variant to set.
     * @param amount The amount of items to set.
     */
    @TestOnly
    default void setSlot(int slot, V variant, long amount) {
        this.setSlot(slot, variant, amount, false);
    }

    /**
     * Sets the items in the given slot without using a transaction.
     * @param slot The slot to set.
     * @param variant The variant to set.
     * @param amount The amount of items to set.
     * @param markDirty Whether to mark the inventory as changed after setting the item.
     */
    @TestOnly
    void setSlot(int slot, V variant, long amount, boolean markDirty);

    @ApiStatus.Internal
    default Storage<V> getExposedStorage(@Nullable Either<Integer, SlotType<?, ?>> either, @NotNull ResourceFlow flow) {
        if (either != null) {
            if (either.right().isPresent()) {
                assert either.right().get().getType().willAcceptResource(this.getResource());
                return ExposedStorage.ofType(this, (SlotType<T, V>) either.right().get(), flow.canFlowIn(ResourceFlow.INPUT), flow.canFlowIn(ResourceFlow.OUTPUT));
            } else {
                //noinspection OptionalGetWithoutIsPresent - we can assert that left is present if right is not.
                return ExposedStorage.ofSlot(this, either.left().get(), flow.canFlowIn(ResourceFlow.INPUT), flow.canFlowIn(ResourceFlow.OUTPUT));
            }
        } else {
            return ExposedStorage.of(this, flow.canFlowIn(ResourceFlow.INPUT), flow.canFlowIn(ResourceFlow.OUTPUT));
        }
    }

    long getCapacity(int slot);
}
