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

package dev.galacticraft.impl.machine.storage;

import com.google.common.collect.Iterators;
import dev.galacticraft.api.block.entity.MachineBlockEntity;
import dev.galacticraft.api.machine.storage.MachineItemStorage;
import dev.galacticraft.api.machine.storage.display.ItemSlotDisplay;
import dev.galacticraft.api.machine.storage.io.ExposedStorage;
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.machine.storage.io.ResourceType;
import dev.galacticraft.api.machine.storage.io.SlotType;
import dev.galacticraft.api.screen.MachineScreenHandler;
import dev.galacticraft.api.screen.StorageSyncHandler;
import dev.galacticraft.impl.compat.ReadOnlySubInv;
import dev.galacticraft.impl.machine.Constant;
import dev.galacticraft.impl.machine.ModCount;
import dev.galacticraft.impl.machine.storage.slot.ItemSlot;
import dev.galacticraft.impl.machine.storage.slot.ResourceSlot;
import dev.galacticraft.impl.machine.storage.slot.VanillaWrappedItemSlot;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.tag.Tag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

//todo: more integer sanity checks?
public class MachineItemStorageImpl implements MachineItemStorage {
    private final int size;
    private final @NotNull ItemSlotDisplay[] displays;
    private final @NotNull ItemSlot[] inventory;
    private final @NotNull SlotType<Item, ItemVariant>[] types;
    private final boolean @NotNull [] extraction;
    private final boolean @NotNull [] insertion;

    private final ModCount modCount = new ModCount();
    private final @NotNull ExposedStorage<Item, ItemVariant> view = ExposedStorage.of(this, false, false);
    private final Inventory playerInventory;

    public MachineItemStorageImpl(int size, SlotType<Item, ItemVariant>[] types, long[] counts, ItemSlotDisplay[] displays) {
        this.size = size;
        this.displays = displays;
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

        this.playerInventory = new PlayerExposedVanillaInventory(this);
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
    public int getModCountUnsafe() {
        return this.modCount.getModCountUnsafe();
    }

    @Override
    public int getSlotModCount(int slot) {
        return this.getSlot(slot).getModCount();
    }

    @Override
    public boolean isFull(int slot) {
        return this.getSlot(slot).getAmount() == this.getSlot(slot).getCapacity();
    }

    @Override
    public boolean isEmpty(int slot) {
        return this.getSlot(slot).getAmount() == 0;
    }

    @Override
    public ResourceSlot<Item, ItemVariant, ItemStack> getSlot(int slot) {
        return this.inventory[slot];
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
        return this.getSlot(slot).copyStack();
    }

    @Override
    public @NotNull ItemVariant getVariant(int slot) {
        return this.getSlot(slot).getResource();
    }

    @Override
    public long getAmount(int slot) {
        return this.getSlot(slot).getAmount();
    }

    @Override
    public @NotNull ResourceType<Item, ItemVariant> getResource() {
        return ResourceType.ITEM;
    }

    @Override
    public boolean canExposedExtract(int slot) {
        return this.extraction[slot];
    }

    @Override
    public boolean canExposedInsert(int slot) {
        return this.insertion[slot];
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, @NotNull TransactionContext context) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        long inserted = 0;
        for (int i = 0; i < this.size(); i++) {
            if (this.canAccept(i, resource)) {
                inserted += this.insert(i, resource, maxAmount - inserted, context);
                if (inserted == maxAmount) {
                    break;
                }
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

        return this.extractVariant(this.getSlot(slot), amount, context);
    }

    @Override
    public @NotNull ItemStack extract(int slot, @NotNull Tag<Item> tag, long amount, @Nullable TransactionContext context) {
        StoragePreconditions.notNegative(amount);

        ResourceSlot<Item, ItemVariant, ItemStack> invSlot = this.getSlot(slot);
        if (tag.values().contains(invSlot.getResource().getItem())) {
            return this.extractVariant(invSlot, amount, context);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public @NotNull long extract(int slot, @NotNull Item item, long amount, @Nullable TransactionContext context) {
        StoragePreconditions.notNegative(amount);
        return this.extract(slot, ItemVariant.of(item), amount, context);
    }

    @NotNull
    private ItemStack extractVariant(@NotNull ResourceSlot<Item, ItemVariant, ItemStack> invSlot, long amount, @Nullable TransactionContext context) {
        long extracted = Math.min(invSlot.getAmount(), amount);
        if (extracted > 0) {
            try (Transaction transaction = Transaction.openNested(context)) {
                ItemStack stack = invSlot.copyStack();
                stack.setCount(Math.toIntExact(extracted));
                invSlot.extract(extracted, transaction);
                this.modCount.increment(transaction);
                transaction.commit();
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack replace(int slot, @NotNull ItemVariant variant, long amount, @Nullable TransactionContext context) {
        StoragePreconditions.notBlankNotNegative(variant, amount);
        try (Transaction transaction = Transaction.openNested(context)) {
            ResourceSlot<Item, ItemVariant, ItemStack> invSlot = this.getSlot(slot);
            ItemStack currentStack = invSlot.copyStack();
            invSlot.setStack(variant, amount, context);
            this.modCount.increment(transaction);
            transaction.commit();
            return currentStack;
        }
    }

    @Override
    public long insert(int slot, @NotNull ItemVariant variant, long amount, @Nullable TransactionContext context) {
        StoragePreconditions.notBlankNotNegative(variant, amount);
        if (!this.canAccept(slot, variant) || amount == 0) return 0;
        ResourceSlot<Item, ItemVariant, ItemStack> invSlot = this.getSlot(slot);
        if (invSlot.isResourceBlank()) {
            amount = Math.min(amount, invSlot.getCapacity(variant));
            try (Transaction transaction = Transaction.openNested(context)) {
                invSlot.setStack(variant, amount, transaction);
                this.modCount.increment(transaction);
                transaction.commit();
                return amount;
            }
        } else if (variant.equals(invSlot.getResource())) {
            try (Transaction transaction = Transaction.openNested(context)) {
                long inserted = Math.min(amount, invSlot.getCapacity(variant) - invSlot.getAmount());
                if (inserted > 0) {
                    invSlot.setAmount(invSlot.getAmount() + inserted, transaction);
                    this.modCount.increment(transaction);
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
        StoragePreconditions.notBlankNotNegative(variant, amount);
        ResourceSlot<Item, ItemVariant, ItemStack> invSlot = this.getSlot(slot);
        if (invSlot.getResource().equals(variant)) {
            long extracted = Math.min(invSlot.getAmount(), amount);
            if (extracted > 0) {
                try (Transaction transaction = Transaction.openNested(context)) {
                    invSlot.extract(extracted, transaction);
                    this.modCount.increment(transaction);
                    transaction.commit();
                    return extracted;
                }
            }
        }
        return 0;
    }

    @Override
    public long getMaxCount(int slot) {
        return this.getSlot(slot).getCapacity();
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
    public long count(@NotNull Item item) {
        long count = 0;
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
    public boolean containsAny(@NotNull Tag<Item> tag) {
        List<Item> values = tag.values();
        for (ItemSlot itemSlot : this.inventory) {
            if (values.contains(itemSlot.copyStack().getItem())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <M extends MachineBlockEntity> void addSlots(MachineScreenHandler<M> handler) {
        for (int i = 0; i < this.displays.length; i++) {
            handler.addSlot(new VanillaWrappedItemSlot(this, i, this.displays[i]));
        }
    }

    @Override
    public Inventory playerInventory() {
        return this.playerInventory;
    }

    @Override
    public Inventory subInv(int start, int size) {
        return new ReadOnlySubInv(this, start, size);
    }

    @Override
    public @NotNull NbtElement writeNbt() {
        NbtList list = new NbtList();
        for (ItemSlot itemSlot : this.inventory) {
            NbtCompound compound = itemSlot.getResource().toNbt();
            compound.putLong(Constant.Nbt.AMOUNT, itemSlot.getAmount());
            list.add(compound);
        }
        return list;
    }

    @Override
    public void readNbt(@NotNull NbtElement nbt) {
        if (nbt.getType() == NbtElement.LIST_TYPE) {
            NbtList list = ((NbtList) nbt);
            for (int i = 0; i < list.size(); i++) {
                NbtCompound compound = list.getCompound(i);
                this.getSlot(i).setStackUnsafe(ItemVariant.fromNbt(compound), compound.getLong(Constant.Nbt.AMOUNT), true);
            }
        }
    }

    @Override
    public void clear() {
        assert !Transaction.isOpen();
        for (ItemSlot itemSlot : this.inventory) {
            itemSlot.setStackUnsafe(ItemVariant.blank(), 0, true);
        }
        this.modCount.incrementUnsafe();
    }

    @Override
    public ExposedStorage<Item, ItemVariant> view() {
        return this.view;
    }

    @Override
    public void setSlot(int slot, ItemVariant variant, long amount, boolean markDirty) {
        assert !Transaction.isOpen();
        this.getSlot(slot).setStackUnsafe(variant, amount, markDirty);
        if (markDirty) this.modCount.incrementUnsafe();
    }

    @Override
    public long getCapacity(int slot) {
        return this.getSlot(slot).getCapacity();
    }

    @Override
    public SlotType<Item, ItemVariant> @NotNull [] getTypes() {
        return this.types;
    }

    @Override
    public @NotNull StorageSyncHandler createSyncHandler() {
        return new StorageSyncHandler() {
            private int modCount = -1;

            @Override
            public boolean needsSyncing() {
                return MachineItemStorageImpl.this.getModCount() != this.modCount;
            }

            @Override
            public void sync(PacketByteBuf buf) {
                this.modCount = MachineItemStorageImpl.this.modCount.getModCount();
                for (ItemSlot slot : MachineItemStorageImpl.this.inventory) {
                    slot.getResource().toPacket(buf);
                    buf.writeLong(slot.getAmount());
                }
            }

            @Override
            public void read(PacketByteBuf buf) {
                for (ItemSlot slot : MachineItemStorageImpl.this.inventory) {
                    slot.setStackUnsafe(ItemVariant.fromPacket(buf), buf.readLong(), false);
                }
            }
        };
    }

    @ApiStatus.Internal
    public void incrementModCountUnsafe() {
        this.modCount.incrementUnsafe();
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
        private final Iterator<ItemSlot> partIterator = Iterators.forArray(MachineItemStorageImpl.this.inventory);
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
}
