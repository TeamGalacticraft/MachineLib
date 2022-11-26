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
import dev.galacticraft.machinelib.impl.storage.exposed.slot.ExposedStorageSlot;
import dev.galacticraft.machinelib.impl.storage.exposed.slot.PlayerExposedStorageSlot;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A single slot that restricts input and output.
 *
 * @param <T> The type of resource
 * @param <V> The resource variant type
 */
public interface ExposedSlot<T, V extends TransferVariant<T>> extends ExposedStorage<T, V>, SingleSlotStorage<V> {
    /**
     * Creates a new slot storage that can restrict insertion for a player.
     *
     * @param storage The storage to expose.
     * @param slot The slot to expose.
     * @param insert Whether to allow insertion.
     * @param <T> The inner type of the storage.
     * @param <V> The {@link TransferVariant} to expose.
     * @return The exposed storage.
     */
    @Contract("_, _, _ -> new")
    static <T, V extends TransferVariant<T>> @NotNull ExposedSlot<T, V> ofPlayerSlot(@NotNull ResourceStorage<T, V, ?> storage, int slot, boolean insert) {
        Preconditions.checkNotNull(storage);
        return new PlayerExposedStorageSlot<>(storage, slot, insert);
    }

    /**
     * Creates a new slot storage that restricts insertion or extraction.
     *
     * @param storage The storage to expose.
     * @param slot The slot to expose.
     * @param insert Whether to allow insertion.
     * @param extract Whether to allow extraction.
     * @param <T> The inner type of the storage.
     * @param <V> The {@link TransferVariant} to expose.
     * @return The exposed storage.
     */
    @Contract("_, _, _, _ -> new")
    static <T, V extends TransferVariant<T>> @NotNull ExposedSlot<T, V> ofSlot(@NotNull ResourceStorage<T, V, ?> storage, int slot, boolean insert, boolean extract) {
        Preconditions.checkNotNull(storage);
        return new ExposedStorageSlot<>(storage, slot, insert, extract);
    }
}
