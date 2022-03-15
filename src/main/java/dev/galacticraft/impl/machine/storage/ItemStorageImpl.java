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

package dev.galacticraft.impl.machine.storage;

import com.google.common.collect.Iterators;
import dev.galacticraft.api.machine.storage.ItemStorage;
import dev.galacticraft.api.machine.storage.ModCount;
import dev.galacticraft.api.machine.storage.slot.ResourceFlow;
import dev.galacticraft.api.machine.storage.slot.SlotType;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.Tag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemStorageImpl implements ItemStorage {
    private final int size;
    private final ItemSlot[] inventory;
    private final SlotType<Item, ItemVariant>[] types;
    private final boolean[] extraction;
    private final boolean[] insertion;

    private final ModCount modCount = new ModCount();

    public ItemStorageImpl(int size, SlotType<Item, ItemVariant>[] types, int[] counts) {
        this.size = size;
        this.inventory = new ItemSlot[this.size];
        this.extraction = new boolean[this.size];
        this.insertion = new boolean[this.size];

        for (int i = 0; i < this.inventory.length; i++) {
            this.inventory[i] = new ItemSlot(counts[i]);
            if (types[i].getFlow() == ResourceFlow.INPUT) {
                this.insertion[i] = true;
                this.extraction[i] = false;
            } else if (types[i].getFlow() == ResourceFlow.OUTPUT) {
                this.insertion[i] = false;
                this.extraction[i] = true;
            } else if (types[i].getFlow() == ResourceFlow.BOTH) {
                this.insertion[i] = true;
                this.extraction[i] = true;
            }
        }
        this.types = types;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int getModCount() {
        return this.modCount.getModCount();
    }

    @Override
    public boolean isEmpty() {
        for (ItemSlot itemSlot : this.inventory) {
            if (!itemSlot.isResourceBlank()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getStack(int slot) {
        return this.inventory[slot].copyStack();
    }

    @Override
    public boolean canExtract(int slot) {
        return this.extraction[slot];
    }

    @Override
    public boolean canInsert(int slot) {
        return this.insertion[slot];
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, @NotNull TransactionContext context) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        long inserted = 0;
        for (int i = 0; i < this.size(); i++) {
            inserted += this.insert(i, resource, maxAmount - inserted, context);
            if (inserted == maxAmount) {
                break;
            }
        }

        return inserted;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, @NotNull TransactionContext context) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        long extracted = 0;
        for (int i = 0; i < this.size(); i++) {
            extracted += this.extract(i, resource, maxAmount - extracted, context);
            if (extracted == maxAmount) {
                break;
            }
        }

        return extracted;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator(@NotNull TransactionContext context) {
        return new CombinedIterator(context);
    }

    @Override
    public @NotNull ItemStack extract(int slot, long amount, @Nullable TransactionContext context) {
        StoragePreconditions.notNegative(amount);

        return this.extractVariant(this.inventory[slot], amount, context);
    }

    @Override
    public @NotNull ItemStack extract(int slot, @NotNull Tag<Item> tag, long amount, @Nullable TransactionContext context) {
        StoragePreconditions.notNegative(amount);

        ItemSlot invSlot = this.inventory[slot];
        if (tag.values().contains(invSlot.variant.getItem())) {
            return this.extractVariant(invSlot, amount, context);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public @NotNull ItemStack extract(int slot, @NotNull Item item, long amount, @Nullable TransactionContext context) {
        StoragePreconditions.notNegative(amount);

        ItemSlot invSlot = this.inventory[slot];
        if (invSlot.variant.getItem() == item) {
            return this.extractVariant(invSlot, amount, context);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @NotNull
    private ItemStack extractVariant(@NotNull ItemSlot invSlot, long amount, @Nullable TransactionContext context) {
        int extracted = (int) Math.min(invSlot.amount, amount);
        if (extracted > 0) {
            try (Transaction transaction = Transaction.openNested(context)) {
                invSlot.updateSnapshots(transaction);
                invSlot.amount -= extracted;

                if (amount == 0) {
                    invSlot.variant = ItemVariant.blank();
                }
                modCount.increment(transaction);

                ItemStack copy = invSlot.variant.toStack(extracted);
                transaction.commit();
                return copy;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack replace(int slot, @NotNull ItemVariant variant, long amount, @Nullable TransactionContext context) {
        try (Transaction transaction = Transaction.openNested(context)) {
            ItemSlot invSlot = this.inventory[slot];
            invSlot.updateSnapshots(transaction);
            ItemStack currentStack = invSlot.copyStack();
            invSlot.variant = variant;
            invSlot.amount = amount;
            modCount.increment(transaction);
            transaction.commit();
            return currentStack;
        }
    }

    @Override
    public long insert(int slot, @NotNull ItemVariant variant, long amount, @Nullable TransactionContext context) {
        ItemSlot invSlot = this.inventory[slot];
        if (invSlot.isResourceBlank()) {
            amount = Math.min(amount, invSlot.getCapacity(variant));
            try (Transaction transaction = Transaction.openNested(context)) {
                invSlot.updateSnapshots(transaction);
                invSlot.variant = variant;
                invSlot.amount = amount;
                modCount.increment(transaction);
                transaction.commit();
                return amount;
            }
        } else if (variant.equals(invSlot.getResource())) {
            try (Transaction transaction = Transaction.openNested(context)) {
                long inserted = Math.min(amount, invSlot.getCapacity(variant) - invSlot.amount);
                if (inserted > 0) {
                    invSlot.updateSnapshots(transaction);
                    invSlot.amount += inserted;
                    modCount.increment(transaction);
                    transaction.commit();
                    return inserted;
                }
                return 0;
            }
        } else {
            return 0;
        }
    }

    @Override
    public long extract(int slot, @NotNull ItemVariant variant, long amount, @Nullable TransactionContext context) {
        ItemSlot invSlot = this.inventory[slot];
        if (invSlot.variant.equals(variant)) {
            int extracted = (int) Math.min(invSlot.amount, amount);
            if (extracted > 0) {
                try (Transaction transaction = Transaction.openNested(context)) {
                    invSlot.updateSnapshots(transaction);
                    invSlot.amount -= extracted;

                    if (amount == 0) {
                        invSlot.variant = ItemVariant.blank();
                    }
                    modCount.increment(transaction);
                    transaction.commit();
                    return extracted;
                }
            }
        }
        return 0;
    }

    @Override
    public int getMaxCount(int slot) {
        return (int) this.inventory[slot].getCapacity();
    }

    @Override
    public boolean canAccess(@NotNull PlayerEntity player) {
        return true;
    }

    @Override
    public boolean canAccept(int slot, @NotNull ItemVariant variant) {
        return this.types[slot].willAccept(variant);
    }

    @Override
    public int count(@NotNull Item item) {
        int count = 0;
        for (ItemSlot itemSlot : this.inventory) {
            ItemStack stack = itemSlot.copyStack();
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    @Override
    public boolean containsAny(@NotNull Set<Item> items) {
        for (ItemSlot itemSlot : this.inventory) {
            if (items.contains(itemSlot.copyStack().getItem())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAny(@NotNull Tag<Item> items) {
        List<Item> values = items.values();
        for (ItemSlot itemSlot : this.inventory) {
            if (values.contains(itemSlot.copyStack().getItem())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void clear() {
        for (ItemSlot itemSlot : this.inventory) {
            itemSlot.variant = ItemVariant.blank();
            itemSlot.amount = 0;
        }
        modCount.incrementUnsafe();
    }

    @Override
    public SlotType<Item, ItemVariant>[] getTypes() {
        return this.types;
    }

    /*
     * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    private class CombinedIterator implements Iterator<StorageView<ItemVariant>>, Transaction.CloseCallback {
        private boolean open = true;
        private final TransactionContext context;
        private final Iterator<ItemSlot> partIterator = Iterators.forArray(ItemStorageImpl.this.inventory);
        // Always holds the next StorageView<T>, except during next() while the iterator is being advanced.
        private Iterator<StorageView<ItemVariant>> currentPartIterator = null;

        private CombinedIterator(TransactionContext context) {
            this.context = context;
            advanceCurrentPartIterator();
            context.addCloseCallback(this);
        }

        @Override
        public boolean hasNext() {
            return open && currentPartIterator != null && currentPartIterator.hasNext();
        }

        @Override
        public StorageView<ItemVariant> next() {
            if (!open) {
                throw new NoSuchElementException("The transaction for this iterator was closed.");
            }

            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            StorageView<ItemVariant> returned = currentPartIterator.next();

            // Advance the current part iterator
            if (!currentPartIterator.hasNext()) {
                advanceCurrentPartIterator();
            }

            return returned;
        }

        private void advanceCurrentPartIterator() {
            while (partIterator.hasNext()) {
                this.currentPartIterator = partIterator.next().iterator(context);

                if (this.currentPartIterator.hasNext()) {
                    break;
                }
            }
        }

        @Override
        public void onClose(TransactionContext context, Transaction.Result result) {
            // As soon as the transaction is closed, this iterator is not valid anymore.
            open = false;
        }
    }

    public static class Builder {
        private int size = 0;
        private final List<SlotType<Item, ItemVariant>> types = new ArrayList<>();
        private final IntList counts = new IntArrayList();

        public Builder() {}

        @Contract(value = " -> new", pure = true)
        public static @NotNull Builder create() {
            return new Builder();
        }

        public @NotNull Builder addSlot(SlotType<Item, ItemVariant> type) {
            return this.addSlot(type, 64);
        }

        public @NotNull Builder addSlot(SlotType<Item, ItemVariant> type, int maxCount) {
            maxCount = Math.min(maxCount, 64);
            this.size++;
            this.types.add(type);
            this.counts.add(maxCount);
            return this;
        }

        @Contract(pure = true, value = " -> new")
        public @NotNull ItemStorageImpl build() {
            return new ItemStorageImpl(this.size, this.types.toArray(new SlotType[0]), this.counts.toIntArray());
        }
    }

    private static class ItemSlot extends SingleVariantStorage<ItemVariant> {
        private final int capacity;

        private ItemSlot(int capacity) {
            this.capacity = capacity;
        }

        @Override
        protected ItemVariant getBlankVariant() {
            return ItemVariant.blank();
        }

        @Override
        protected long getCapacity(@NotNull ItemVariant variant) {
            return Math.min(this.capacity, variant.getItem().getMaxCount());
        }

        public ItemStack copyStack() {
            return this.variant.toStack((int) this.amount);
        }
    }
}
