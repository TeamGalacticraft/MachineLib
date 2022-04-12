/*
 * Copyright (c) 2021-${year} ${company}
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

package dev.galacticraft.api.machine.storage.io;

import dev.galacticraft.api.machine.storage.ResourceStorage;
import dev.galacticraft.impl.machine.storage.io.ExposedInventory;
import dev.galacticraft.impl.machine.storage.io.ExposedSlot;
import dev.galacticraft.impl.machine.storage.io.ExposedSlots;
import dev.galacticraft.impl.machine.storage.io.PlayerExposedInventory;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * A storage that exposes the contents of a {@link ResourceStorage} but keeps modification restricted to certain slots or operations.
 * @param <T> The inner type of the storage.
 * @param <V> The {@link TransferVariant} to expose.
 */
public interface ExposedStorage<T, V extends TransferVariant<T>> extends Storage<V> {
    /**
     * Returns the resource in the given slot.
     * @param slot The slot to get the resource from.
     * @return The resource in the slot.
     */
    V getResource(int slot);

    /**
     * Returns the amount of resources in the given slot.
     * @param slot The slot to get the amount of resources from.
     * @return The amount of resources in the slot.
     */
    long getAmount(int slot);

    /**
     * Returns the capacity of the given slot.
     * @param slot The slot to get the capacity of.
     * @return The capacity of the slot.
     */
    long getCapacity(int slot);

    /**
     * Returns a new storage that only exposed the given slot.
     * @param slot The slot to expose.
     * @return The exposed storage.
     */
    Storage<V> getSlot(int slot);

    /**
     * Returns the filter for the given slot.
     * @param slot The slot to get the filter for.
     * @return The filter for the slot.
     */
    Predicate<V> getFilter(int slot);

    /**
     * Creates a new storage that restricts insertion or extraction.
     * @param storage The storage to expose.
     * @param insert Whether to allow insertion.
     * @param extract Whether to allow extraction.
     * @param <T> The inner type of the storage.
     * @param <V> The {@link TransferVariant} to expose.
     * @return The exposed storage.
     */
    @Contract("_, _, _ -> new")
    static <T, V extends TransferVariant<T>> @NotNull ExposedStorage<T, V> of(ResourceStorage<T, V, ?> storage, boolean insert, boolean extract) {
        return new ExposedInventory<>(storage, insert, extract);
    }

    /**
     * Creates a new storage that restricts insertion or extraction.
     * This has more lenience towards extracting to allow players to take out resource they inserted.
     *
     * @param storage The storage to expose.
     * @param insert Whether to allow insertion.
     * @param extract Whether to allow extraction.
     * @param <T> The inner type of the storage.
     * @param <V> The {@link TransferVariant} to expose.
     * @return The exposed storage.
     */
    @Contract("_, _, _ -> new")
    static <T, V extends TransferVariant<T>> @NotNull ExposedStorage<T, V> ofPlayer(ResourceStorage<T, V, ?> storage, boolean insert, boolean extract) {
        return new PlayerExposedInventory<>(storage, insert, extract);
    }

    /**
     * Creates a new storage that restricts insertion or extraction to a specific type of slot.
     * @param storage The storage to expose.
     * @param type The type of slot to expose.
     * @param insert Whether to allow insertion.
     * @param extract Whether to allow extraction.
     * @param <T> The inner type of the storage.
     * @param <V> The {@link TransferVariant} to expose.
     * @return The exposed storage.
     */
    @Contract("_, _, _, _ -> new")
    static <T, V extends TransferVariant<T>> @NotNull ExposedStorage<T, V> ofType(ResourceStorage<T, V, ?> storage, SlotType<T, V> type, boolean insert, boolean extract) {
        return new ExposedSlots<>(storage, type, insert, extract);
    }

    /**
     * Creates a new storage that restricts insertion or extraction to a specific slot.
     * @param storage The storage to expose.
     * @param slot The slot to expose.
     * @param insert Whether to allow insertion.
     * @param extract Whether to allow extraction.
     * @param <T> The inner type of the storage.
     * @param <V> The {@link TransferVariant} to expose.
     * @return The exposed storage.
     */
    @Contract("_, _, _, _ -> new")
    static <T, V extends TransferVariant<T>> @NotNull ExposedStorage<T, V> ofSlot(ResourceStorage<T, V, ?> storage, int slot, boolean insert, boolean extract) {
        return new ExposedSlot<>(storage, slot, insert, extract);
    }
}
