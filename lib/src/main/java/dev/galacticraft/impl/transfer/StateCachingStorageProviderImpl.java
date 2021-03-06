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

package dev.galacticraft.impl.transfer;

import dev.galacticraft.api.transfer.StateCachingStorageProvider;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StateCachingStorageProviderImpl<T> implements StateCachingStorageProvider<T> {
    private final @NotNull SingleSlotStorage<ItemVariant> slot;
    private final @NotNull ContainerItemContext context;
    private final @NotNull ItemApiLookup<T, ContainerItemContext> lookup;
    private boolean hasStorage = false;
    private long modCount = -1;

    public StateCachingStorageProviderImpl(@NotNull SingleSlotStorage<ItemVariant> slot, @NotNull ItemApiLookup<T, ContainerItemContext> lookup) {
        this.slot = slot;
        this.context = ContainerItemContext.ofSingleSlot(slot);
        this.lookup = lookup;
    }

    @Nullable
    @Override
    public T getStorage() {
        assert !Transaction.isOpen();
        long version = this.slot.getVersion();
        if (this.modCount != version) {
            this.modCount = version;
            T storage = this.context.find(this.lookup);
            this.hasStorage = storage != null;
            return storage;
        }

        return this.hasStorage ? this.context.find(this.lookup) : null;
    }
}
