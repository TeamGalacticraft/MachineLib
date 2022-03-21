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

package dev.galacticraft.api.machine.storage.io;

import dev.galacticraft.api.machine.storage.ResourceStorage;
import dev.galacticraft.impl.machine.storage.io.ExposedInventory;
import dev.galacticraft.impl.machine.storage.io.ExposedSlot;
import dev.galacticraft.impl.machine.storage.io.ExposedSlots;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public interface ExposedStorage<T, V extends TransferVariant<T>> extends Storage<V> {
    V getResource(int slot);

    long getAmount(int slot);

    long getCapacity(int slot);

    Storage<V> getSlot(int slot);

    Predicate<V> getFilter(int index);

    @Contract("_, _, _ -> new")
    static <T, V extends TransferVariant<T>> @NotNull ExposedStorage<T, V> of(ResourceStorage<T, V, ?> storage, boolean insert, boolean extract) {
        return new ExposedInventory<>(storage, insert, extract);
    }

    @Contract("_, _, _, _ -> new")
    static <T, V extends TransferVariant<T>> @NotNull ExposedStorage<T, V> ofType(ResourceStorage<T, V, ?> storage, SlotType<T, V> type, boolean insert, boolean extract) {
        return new ExposedSlots<>(storage, type, insert, extract);
    }

    @Contract("_, _, _, _ -> new")
    static <T, V extends TransferVariant<T>> @NotNull ExposedStorage<T, V> ofSlot(ResourceStorage<T, V, ?> storage, int slot, boolean insert, boolean extract) {
        return new ExposedSlot<>(storage, slot, insert, extract);
    }
}
