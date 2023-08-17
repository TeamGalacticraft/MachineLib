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

package dev.galacticraft.machinelib.impl.compat.transfer;

import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

@ApiStatus.Internal
public record UnmodifiableStorageView<T>(StorageView<T> parent) implements StorageView<T> {
    @Contract(value = "_, true -> param1; _, false -> new")
    public static <T> StorageView<T> maybeCreate(StorageView<T> parent, boolean extract) {
        if (extract) return parent; // if the storage is able to extract, then there is no need to limit it
        return new UnmodifiableStorageView<>(parent);
    }

    @Override
    public long extract(T resource, long maxAmount, TransactionContext transaction) {
        return 0L;
    }

    @Override
    public boolean isResourceBlank() {
        return this.parent.isResourceBlank();
    }

    @Override
    public T getResource() {
        return this.parent.getResource();
    }

    @Override
    public long getAmount() {
        return this.parent.getAmount();
    }

    @Override
    public long getCapacity() {
        return this.parent.getCapacity();
    }
}
