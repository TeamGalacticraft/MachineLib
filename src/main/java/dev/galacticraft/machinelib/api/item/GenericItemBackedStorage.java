/*
 * Copyright (c) 2021-2026 Team Galacticraft
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

package dev.galacticraft.machinelib.api.item;

import com.google.common.collect.Iterators;
import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.filter.ResourceFilters;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;

public abstract class GenericItemBackedStorage<T, V extends TransferVariant<T>> implements Storage<V>, StorageView<V> {
    protected final ItemVariant item;
    protected final ContainerItemContext context;
    protected final ResourceFilter<T> filter;

    protected GenericItemBackedStorage(ItemStack stack, ContainerItemContext context, ResourceFilter<T> filter) {
        this.item = ItemVariant.of(stack);
        this.context = context;
        this.filter = filter;
    }

    protected GenericItemBackedStorage(ItemStack stack, ContainerItemContext context) {
        this(stack, context, ResourceFilters.any());
    }

    protected abstract long getRawMaxInput();
    protected abstract long getRawMaxOutput();
    protected abstract long getRawCapacity();
    protected abstract long getRawAmount();
    protected abstract boolean setRawContents(V variant, long amount, TransactionContext transaction);

    @Override
    public long insert(V resource, long maxAmount, TransactionContext transaction) {
        if (!this.item.equals(this.context.getItemVariant())) return 0;
        if (this.filter.test(resource.getObject(), resource.getComponents()) && (this.getResource().isBlank() || this.getResource().equals(resource))) {
            long stored = this.getRawAmount();
            maxAmount = Math.min(this.getRawCapacity() - stored, maxAmount / this.context.getAmount());
            maxAmount = Math.min(maxAmount, this.getRawMaxInput());
            if (this.setRawContents(resource, stored + maxAmount, transaction)) {
                return maxAmount * this.context.getAmount();
            }
        }
        return 0;
    }

    @Override
    public long extract(V resource, long maxAmount, TransactionContext transaction) {
        assert !resource.isBlank();
        if (!this.item.equals(this.context.getItemVariant())) return 0;
        if (this.getResource().equals(resource)) {
            long stored = this.getRawAmount();
            maxAmount = Math.min(stored, maxAmount / this.context.getAmount());
            maxAmount = Math.min(maxAmount, this.getRawMaxOutput());
            if (this.setRawContents(resource, stored - maxAmount, transaction)) {
                return maxAmount * this.context.getAmount();
            }
        }
        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        return this.getResource().isBlank();
    }

    @Override
    public long getAmount() {
        if (!this.item.equals(this.context.getItemVariant())) return 0;
        return this.getRawAmount() * this.context.getAmount();
    }

    @Override
    public long getCapacity() {
        if (!this.item.equals(this.context.getItemVariant())) return 0;
        return this.getRawCapacity() * this.context.getAmount();
    }

    @Override
    public @NotNull Iterator<StorageView<V>> iterator() {
        if (!this.item.equals(this.context.getItemVariant())) return Collections.emptyIterator();
        return Iterators.singletonIterator(this);
    }
}
