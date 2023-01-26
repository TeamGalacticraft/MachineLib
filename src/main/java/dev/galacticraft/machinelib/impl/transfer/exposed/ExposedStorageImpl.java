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

package dev.galacticraft.machinelib.impl.transfer.exposed;

import com.google.common.collect.Iterators;
import dev.galacticraft.machinelib.api.storage.Modifiable;
import dev.galacticraft.machinelib.api.transfer.exposed.ExposedSlot;
import dev.galacticraft.machinelib.api.transfer.exposed.ExposedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import java.util.Iterator;

public class ExposedStorageImpl<Resource, Variant extends TransferVariant<Resource>> implements ExposedStorage<Resource, Variant> {
    private final Modifiable modifiable;
    private final ExposedSlot<Resource, Variant>[] slots;

    public ExposedStorageImpl(Modifiable modifiable, ExposedSlot<Resource, Variant>[] slots) {
        this.modifiable = modifiable;
        this.slots = slots;
    }

    @Override
    public long insert(Variant variant, long maxAmount, TransactionContext transaction) {
        long requested = maxAmount;
        for (ExposedSlot<Resource, Variant> slot : slots) {
            if (maxAmount == 0) return requested;
            maxAmount -= slot.insert(variant, maxAmount, transaction);
        }
        return requested - maxAmount;
    }

    @Override
    public long extract(Variant variant, long maxAmount, TransactionContext transaction) {
        long requested = maxAmount;
        for (ExposedSlot<Resource, Variant> slot : slots) {
            if (maxAmount == 0) return requested;
            maxAmount -= slot.extract(variant, maxAmount, transaction);
        }
        return requested - maxAmount;
    }

    @Override
    public Iterator<StorageView<Variant>> iterator() {
        return Iterators.forArray(this.slots);
    }

    @Override
    public long getVersion() {
        return this.modifiable.getModifications();
    }
}
