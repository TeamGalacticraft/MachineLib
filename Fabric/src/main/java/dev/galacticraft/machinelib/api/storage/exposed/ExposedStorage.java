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

package dev.galacticraft.machinelib.api.storage.exposed;

import com.google.common.base.Preconditions;
import dev.galacticraft.machinelib.api.storage.ResourceStorage;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.impl.storage.exposed.ExposedSlotGroup;
import dev.galacticraft.machinelib.impl.storage.exposed.ExposedStorageView;
import dev.galacticraft.machinelib.impl.storage.exposed.PlayerExposedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * A storage that exposes the contents of a {@link ResourceStorage} but keeps modification restricted to certain slots or operations.
 *
 * @param <T> The inner type of the storage.
 * @param <V> The {@link TransferVariant} to expose.
 */
public interface ExposedStorage<T, V extends TransferVariant<T>> extends Storage<V> {
    /**
     * Returns the resource in the given slot.
     *
     * @param slot The slot to get the resource from.
     * @return The resource in the slot.
     */
    @NotNull V getResource(int slot);

    /**
     * Returns the amount of resources in the given slot.
     *
     * @param slot The slot to get the amount of resources from.
     * @return The amount of resources in the slot.
     */
    long getAmount(int slot);

    /**
     * Returns the capacity of the given slot.
     *
     * @param slot The slot to get the capacity of.
     * @return The capacity of the slot.
     */
    long getCapacity(int slot);

    /**
     * Returns the filter for the given slot.
     *
     * @param slot The slot to get the filter for.
     * @return The filter for the slot.
     */
    @NotNull Predicate<V> getFilter(int slot);

    /**
     * Creates a new storage that restricts insertion or extraction.
     *
     * @param storage The storage to expose.
     * @param insert Whether to allow insertion.
     * @param extract Whether to allow extraction.
     * @param <T> The inner type of the storage.
     * @param <V> The {@link TransferVariant} to expose.
     * @return The exposed storage.
     */
    @Contract("_, _, _ -> new")
    static <T, V extends TransferVariant<T>> @NotNull ExposedStorage<T, V> of(@NotNull ResourceStorage<T, V, ?> storage, boolean insert, boolean extract) {
        Preconditions.checkNotNull(storage);
        return new dev.galacticraft.machinelib.impl.storage.exposed.ExposedStorage<>(storage, insert, extract);
    }

    /**
     * Returns a view of an exposed storage.
     *
     * @param storage the storage
     * @return a view of the given storage
     * @param <T> The inner type of the storage.
     * @param <V> The {@link TransferVariant} to expose.
     */
    @Deprecated(forRemoval = true)
    @Contract("_ -> new")
    static <T, V extends TransferVariant<T>> @NotNull ExposedStorage<T, V> view(@NotNull ResourceStorage<T, V, ?> storage) {
        Preconditions.checkNotNull(storage);
        return new ExposedStorageView<>(storage);
    }

    /**
     * Creates a new storage that restricts insertion or extraction.
     * This has more lenience towards extracting to allow players to take out resource they inserted.
     *
     * @param storage The storage to expose.
     * @param insert Whether to allow insertion.
     * @param <T> The inner type of the storage.
     * @param <V> The {@link TransferVariant} to expose.
     * @return The exposed storage.
     */
    @Contract("_, _ -> new")
    static <T, V extends TransferVariant<T>> @NotNull ExposedStorage<T, V> ofPlayer(@NotNull ResourceStorage<T, V, ?> storage, boolean insert) {
        Preconditions.checkNotNull(storage);
        return new PlayerExposedStorage<>(storage, insert);
    }

    /**
     * Creates a new storage that restricts insertion or extraction to a specific type of slot.
     *
     * @param storage The storage to expose.
     * @param type The type of slot to expose.
     * @param insert Whether to allow insertion.
     * @param extract Whether to allow extraction.
     * @param <T> The inner type of the storage.
     * @param <V> The {@link TransferVariant} to expose.
     * @return The exposed storage.
     */
    @Contract("_, _, _, _ -> new")
    static <T, V extends TransferVariant<T>> @NotNull ExposedStorage<T, V> ofType(@NotNull ResourceStorage<T, V, ?> storage, @NotNull SlotGroup type, boolean insert, boolean extract) {
        Preconditions.checkNotNull(storage);
        Preconditions.checkNotNull(type);
        return new ExposedSlotGroup<>(storage, type, insert, extract);
    }
}
