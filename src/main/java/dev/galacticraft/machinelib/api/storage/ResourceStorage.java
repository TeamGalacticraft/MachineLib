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

package dev.galacticraft.machinelib.api.storage;

import dev.galacticraft.machinelib.api.storage.exposed.ExposedSlot;
import dev.galacticraft.machinelib.api.storage.exposed.ExposedStorage;
import dev.galacticraft.machinelib.api.storage.io.ConfiguredStorage;
import dev.galacticraft.machinelib.api.storage.io.ResourceFlow;
import dev.galacticraft.machinelib.api.storage.io.ResourceType;
import dev.galacticraft.machinelib.api.storage.io.StorageSelection;
import dev.galacticraft.machinelib.api.storage.slot.StorageSlot;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.*;

import java.util.Collection;
import java.util.function.Predicate;

public interface ResourceStorage<T, V extends TransferVariant<T>, S> extends ConfiguredStorage, Storage<V>, Clearable {
    /**
     * Returns the number of slots in the storage.
     * @return The number of slots in the storage.
     */
    @Contract(pure = true)
    int size();

    /**
     * Returns whether the storage contains no items.
     * @return Whether the storage contains no items.
     */
    @Contract(pure = true)
    boolean isEmpty();

    /**
     * Returns a resource stack with the amount and type of the resource in the storage.
     * @param slot The slot to get the stack from.
     * @return A resource stack with the amount and type of the resource in the storage.
     */
    default @NotNull S getStack(int slot) {
        return this.getSlot(slot).copyStack();
    }

    /**
     * Returns the resource variant contained in the slot.
     * @param slot The slot to get the variant from.
     * @return The resource variant contained in the slot.
     */
    @Contract(pure = true)
    default @NotNull V getVariant(int slot) {
        return this.getSlot(slot).getResource();
    }

    /**
     * The amount of resources in the given slot.
     * @param slot The slot to get the amount of resources from.
     * @return The amount of resources in the slot.
     */
    @Contract(pure = true)
    default long getAmount(int slot) {
        return this.getSlot(slot).getAmount();
    }

    /**
     * Returns the type of resource contained in this storage.
     * @return The type of resource contained in this storage.
     */
    @Contract(pure = true)
    @NotNull ResourceType getResource();

    /**
     * Returns whether the storage allows extraction from the given slot.
     * @param slot The slot to check.
     * @return Whether the storage allows extraction from the given slot.
     */
    @Contract(pure = true)
    boolean canExposedExtract(int slot);

    /**
     * Returns whether the storage allows insertion into the given slot.
     * @param slot The slot to check.
     * @return Whether the storage allows insertion into the given slot.
     */
    @Contract(pure = true)
    boolean canExposedInsert(int slot);

    /**
     * Returns whether the storage allows insertion into the given slot.
     * @param slot The slot to check.
     * @return Whether the storage allows insertion into the given slot.
     */
    @Contract(pure = true)
    boolean canPlayerInsert(int slot);

    @Override
    default long insert(@NotNull V resource, long amount, @NotNull TransactionContext context) {
        StoragePreconditions.notBlankNotNegative(resource, amount);
        long inserted = 0;
        for (int i = 0; i < this.size(); i++) {
            if (this.canAccept(i, resource)) {
                inserted += this.insert(i, resource, amount - inserted, context);
                if (inserted == amount) {
                    break;
                }
            }
        }

        return inserted;
    }

    default long insert(@NotNull V resource, long maxAmount) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        long inserted = 0;
        for (int i = 0; i < this.size(); i++) {
            if (this.canAccept(i, resource)) {
                inserted += this.insert(i, resource, maxAmount - inserted);
                if (inserted == maxAmount) {
                    break;
                }
            }
        }

        return inserted;
    }

    default long simulateInsert(@NotNull V resource, long maxAmount) {
        return this.simulateInsert(resource, maxAmount, null);
    }

    /**
     * Inserts the given amount of resources into the given slot.
     * @param slot The slot to insert into.
     * @param variant The variant of the resource to insert.
     * @param amount The amount of resources to insert.
     * @param context The transaction context.
     * @return The amount of inserted resources.
     */
    default long insert(int slot, @NotNull V variant, long amount, @NotNull TransactionContext context) {
        if (!this.canAccept(slot, variant)) return 0;
        return this.getSlot(slot).insert(variant, amount, context);
    }

    default long insert(int slot, @NotNull V variant, long amount) {
        if (!this.canAccept(slot, variant)) return 0;
        return this.getSlot(slot).insert(variant, amount);
    }

    default long simulateInsert(int slot, @NotNull V variant, long amount, @Nullable TransactionContext context) {
        try (Transaction transaction = Transaction.openNested(context)) {
            return this.insert(slot, variant, amount, transaction);
        }
    }

    default long simulateInsert(int slot, @NotNull V variant, long amount) {
        return this.simulateInsert(slot, variant, amount, null);
    }

    /**
     * Inserts the given amount of resources into the given slot.
     * @param slot The slot to insert into.
     * @param type The type of the resource to insert.
     * @param amount The amount of resources to insert.
     * @param context The transaction context.
     * @return The amount of inserted resources.
     */
    default long insert(int slot, @NotNull T type, long amount, @NotNull TransactionContext context) {
        return this.insert(slot, this.createVariant(type), amount, context);
    }

    default long insert(int slot, @NotNull T type, long amount) {
        return this.insert(slot, this.createVariant(type), amount);
    }

    default long simulateInsert(int slot, @NotNull T type, long amount, @Nullable TransactionContext context) {
        try (Transaction transaction = Transaction.openNested(context)) {
            return this.insert(slot, type, amount, transaction);
        }
    }

    default long simulateInsert(int slot, @NotNull T type, long amount) {
        return this.simulateInsert(slot, type, amount, null);
    }

    @Override
    default long extract(@NotNull V resource, long amount, @NotNull TransactionContext context) {
        StoragePreconditions.notBlankNotNegative(resource, amount);
        long extracted = 0;
        for (int i = 0; i < this.size(); i++) {
            extracted += this.extract(i, resource, amount - extracted, context);
            if (extracted == amount) {
                break;
            }
        }

        return extracted;
    }

    default long simulateExtract(@NotNull V resource, long amount) {
        return this.simulateExtract(resource, amount, null);
    }

    /**
     * Extracts the given amount of resources from the given slot of the given type.
     * @param slot The slot to extract from.
     * @param variant The variant of the resource to extract.
     * @param amount The amount of resources to extract.
     * @param context The transaction context.
     * @return The amount of extracted resources.
     */
    default long extract(int slot, @NotNull V variant, long amount, @NotNull TransactionContext context) {
        return this.getSlot(slot).extract(variant, amount, context);
    }

    default long extract(int slot, @NotNull V variant, long amount) {
        return this.getSlot(slot).extract(variant, amount);
    }

    default long simulateExtract(int slot, @NotNull V variant, long amount, @Nullable TransactionContext context) {
        return this.getSlot(slot).simulateExtract(variant, amount, context);
    }

    default long simulateExtract(int slot, @NotNull V variant, long amount) {
        return this.getSlot(slot).simulateExtract(variant, amount);
    }

    /**
     * Extracts the given amount of resources from the given slot of the given type.
     * @param slot The slot to extract from.
     * @param type The type of the resource to extract.
     * @param amount The amount of resources to extract.
     * @param context The transaction context.
     * @return The amount of extracted resources.
     */
    default long extract(int slot, @NotNull T type, long amount, @NotNull TransactionContext context) {
        return this.getSlot(slot).extractType(type, amount, context);
    }

    default long extract(int slot, @NotNull T type, long amount) {
        return this.getSlot(slot).extractType(type, amount);
    }

    default long simulateExtract(int slot, @NotNull T type, long amount, @Nullable TransactionContext context) {
        return this.getSlot(slot).simulateExtractType(type, amount, context);
    }

    default long simulateExtract(int slot, @NotNull T type, long amount) {
        return this.getSlot(slot).simulateExtractType(type, amount);
    }
    
    /**
     * Extracts the given amount of resources from the given slot.
     * @param slot The slot to extract from.
     * @param amount The amount of resources to extract.
     * @param context The transaction context.
     * @return The extracted resources.
     */
    default @NotNull S extract(int slot, long amount, @NotNull TransactionContext context) {
        return this.getSlot(slot).extract(amount, context);
    }

    default @NotNull S extract(int slot, long amount) {
        return this.getSlot(slot).extract(amount);
    }

    default @NotNull S simulateExtract(int slot, long amount, @NotNull TransactionContext context) {
        return this.getSlot(slot).simulateExtract(amount, context);
    }

    default @NotNull S simulateExtract(int slot, long amount) {
        return this.getSlot(slot).simulateExtract(amount);
    }

    /**
     * Returns the maximum amount of resources that can be stored in the given slot.
     * This is context specific; for example, a slot with a capacity of 64 can only store 16 snowballs.
     * @param slot The slot to check.
     * @return The maximum amount of resources that can be stored in the given slot.
     */
    default long getMaxCount(int slot) {
        return this.getSlot(slot).getCapacity();
    }

    /**
     * Returns the modification count of this inventory.
     * Do NOT call during a transaction
     *
     * @return the modification count of this inventory.
     */
    long getModCount();

    /**
     * Returns the modification count of this inventory.
     * The modification count may go down if a transaction fails.
     *
     * @return the modification count of this inventory.
     */
    long getModCountUnsafe();

    /**
     * Returns whether any more resources can be inserted into the given slot.
     * Note that a slot can be both empty and full at the same time.
     * @param slot The slot to check.
     * @return Whether any more resources can be inserted into the given slot.
     */
    default boolean isFull(int slot) {
        return this.getSlot(slot).isFull();
    }

    /**
     * Returns whether the given slot contains any resources.
     * Note that a slot can be both empty and full at the same time.
     * @param slot The slot to check.
     * @return Whether the given slot contains any resources.
     */
    default boolean isEmpty(int slot) {
        return this.getSlot(slot).isEmpty();
    }

    /**
     * Returns an internal storage representing the given slot.
     * @param slot The index of the slot.
     * @return An internal storage representing the given slot.
     */
    @NotNull StorageSlot<T, V, S> getSlot(int slot);

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
     * Returns whether the given slot can contain the given resource.
     * @param slot The slot to check.
     * @return Whether the given slot can contain the given resource.
     */
    Predicate<V> getFilter(int slot);

    /**
     * Returns the number of the given resource in this inventory.
     * @param resource The resource to check.
     *                 Note: This is not a variant. Items with different NBT will also be included.
     * @return The number of the given resource in this inventory.
     */
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

    @Override
    default long getVersion() {
        return this.getModCount();
    }

    @ApiStatus.Internal
    @NotNull V createVariant(@NotNull T type);

    /**
     * Sets the items in the given slot without using a transaction.
     * @param slot The slot to set.
     * @param variant The variant to set.
     * @param amount The amount of items to set.
     */
    @TestOnly
    void setSlot(int slot, V variant, long amount);

    @ApiStatus.Internal
    default Storage<V> getExposedStorage(@Nullable StorageSelection selection, @NotNull ResourceFlow flow) {
        if (selection != null) {
            if (selection.isGroup()) {
                return ExposedStorage.ofType(this, selection.getGroup(), flow.canFlowIn(ResourceFlow.INPUT), flow.canFlowIn(ResourceFlow.OUTPUT));
            } else {
                return ExposedSlot.ofSlot(this, selection.getSlot(), flow.canFlowIn(ResourceFlow.INPUT), flow.canFlowIn(ResourceFlow.OUTPUT));
            }
        } else {
            return ExposedStorage.of(this, flow.canFlowIn(ResourceFlow.INPUT), flow.canFlowIn(ResourceFlow.OUTPUT));
        }
    }

    long getCapacity(int slot);
}
