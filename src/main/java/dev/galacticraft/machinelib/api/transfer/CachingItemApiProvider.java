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

package dev.galacticraft.machinelib.api.transfer;

import dev.galacticraft.machinelib.impl.transfer.CachingItemApiProviderImpl;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Retrieves an api from an item in a slot.
 *
 * @param <A> the api to search for
 */
public interface CachingItemApiProvider<A> {
    /**
     * Constructs a new caching api provider
     *
     * @param slot   the slot to check.
     * @param lookup the api lookup.
     * @param <A>    the api to search for.
     * @return a new caching api provider.
     */
    @Contract("_, _ -> new")
    static <A> @NotNull CachingItemApiProvider<A> create(@NotNull SingleSlotStorage<ItemVariant> slot, @NotNull ItemApiLookup<A, ContainerItemContext> lookup) {
        return new CachingItemApiProviderImpl<>(slot, lookup);
    }

    /**
     * Returns the api of the item in this slot.
     *
     * @return the api of the item in this slot.
     */
    @Nullable A getApi();
}
