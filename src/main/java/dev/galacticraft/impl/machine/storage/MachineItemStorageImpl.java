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
import dev.galacticraft.impl.MLConstant;
import dev.galacticraft.impl.compat.ReadOnlySubInv;
import dev.galacticraft.impl.machine.ModCount;
import dev.galacticraft.impl.machine.storage.slot.ItemSlot;
import dev.galacticraft.impl.machine.storage.slot.ResourceSlot;
import dev.galacticraft.impl.machine.storage.slot.VanillaWrappedItemSlot;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

//todo: more integer sanity checks?
public class MachineItemStorageImpl implements MachineItemStorage {
    private final int size;
    private final @NotNull ItemSlotDisplay[] displays;
    private final @NotNull ItemSlot[] inventory;
    private final @NotNull SlotType<Item, ItemVariant>[] types;
    private final boolean @NotNull [] extraction;
    private final boolean @NotNull [] insertion;

    private final ModCount modCount = ModCount.root();
    private final @NotNull ExposedStorage<Item, ItemVariant> view = ExposedStorage.of(this, false, false);
    private final Container playerInventory;

    public MachineItemStorageImpl(int size, SlotType<Item, ItemVariant>[] types, long[] counts, ItemSlotDisplay[] displays) {
        this.size = size;
        this.displays = displays;
        this.inventory = new ItemSlot[this.size];
        this.extraction = new boolean[this.size];
        this.insertion = new boolean[this.size];

        for (int i = 0; i < this.inventory.length; i++) {
            this.inventory[i] = new ItemSlot(counts[i], this.modCount);
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
    public long getModCount() {
        return this.modCount.getModCount();
    }

    @Override
    public long getModCountUnsafe() {
        return this.modCount.getModCountUnsafe();
    }

    @Override
    public long getSlotModCount(int slot) {
        return this.getSlot(slot).getModCount();
    }

    @Override
    public long getSlotModCountUnsafe(int slot) {
        return this.getSlot(slot).getModCountUnsafe();
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
    public @NotNull ResourceSlot<Item, ItemVariant, ItemStack> getSlot(int slot) {
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
    public Iterator<StorageView<ItemVariant>> iterator() {
        return new CombinedIterator();
    }

    @Override
    public @NotNull ItemStack extract(int slot, long amount, @Nullable TransactionContext context) {
        try (Transaction transaction = Transaction.openNested(context)) {
            ItemStack extract = this.getSlot(slot).extract(amount, transaction);
            transaction.commit();
            return extract;
        }
    }

    @Override
    public @NotNull ItemStack extract(int slot, @NotNull TagKey<Item> tag, long amount, @Nullable TransactionContext context) {
        StoragePreconditions.notNegative(amount);

        if (this.getSlot(slot).getResource().getItem().builtInRegistryHolder().is(tag)) {
            return this.extract(slot, amount, context);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public long extract(int slot, @NotNull Item item, long amount, @Nullable TransactionContext context) {
        StoragePreconditions.notNegative(amount);
        return this.extract(slot, ItemVariant.of(item), amount, context);
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
                transaction.commit();
                return amount;
            }
        } else if (variant.equals(invSlot.getResource())) {
            try (Transaction transaction = Transaction.openNested(context)) {
                long inserted = Math.min(amount, invSlot.getCapacity(variant) - invSlot.getAmount());
                if (inserted > 0) {
                    invSlot.setAmount(invSlot.getAmount() + inserted, transaction);
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
        try (Transaction transaction = Transaction.openNested(context)) {
            long extract = this.getSlot(slot).extract(variant, amount, transaction);
            transaction.commit();
            return extract;
        }
    }

    @Override
    public long getMaxCount(int slot) {
        return this.getSlot(slot).getCapacity();
    }

    @Override
    public boolean canAccess(@NotNull Player player) {
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
            if (itemSlot.getResource().getItem() == item) {
                count += itemSlot.getAmount();
            }
        }
        return count;
    }

    @Override
    public long count(@NotNull ItemVariant item) {
        long count = 0;
        for (ItemSlot itemSlot : this.inventory) {
            if (itemSlot.getResource().equals(item)) {
                count += itemSlot.getAmount();
            }
        }
        return count;
    }

    @Override
    public boolean containsAny(@NotNull Collection<Item> items) {
        for (ItemSlot itemSlot : this.inventory) {
            if (items.contains(itemSlot.getResource().getItem())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <M extends MachineBlockEntity> void addSlots(@NotNull MachineScreenHandler<M> handler) {
        for (int i = 0; i < this.displays.length; i++) {
            handler.addSlot(new VanillaWrappedItemSlot(this, i, this.displays[i]));
        }
    }

    @Override
    public @NotNull Container playerInventory() {
        return this.playerInventory;
    }

    @Override
    public @NotNull Container subInv(int start, int size) {
        return new ReadOnlySubInv(this, start, size);
    }

    @Override
    public @NotNull Tag writeNbt() {
        ListTag list = new ListTag();
        for (ItemSlot itemSlot : this.inventory) {
            CompoundTag compound = itemSlot.getResource().toNbt();
            compound.putLong(MLConstant.Nbt.AMOUNT, itemSlot.getAmount());
            list.add(compound);
        }
        return list;
    }

    @Override
    public void readNbt(@NotNull Tag nbt) {
        if (nbt.getId() == Tag.TAG_LIST) {
            ListTag list = ((ListTag) nbt);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag compound = list.getCompound(i);
                this.getSlot(i).setStackUnsafe(ItemVariant.fromNbt(compound), compound.getLong(MLConstant.Nbt.AMOUNT), true);
            }
        }
    }

    @Override
    public void clearContent() {
        assert !Transaction.isOpen();
        for (ItemSlot itemSlot : this.inventory) {
            itemSlot.setStackUnsafe(ItemVariant.blank(), 0, true);
        }
    }

    @Override
    public ExposedStorage<Item, ItemVariant> view() {
        return this.view;
    }

    @Override
    public void setSlotUnsafe(int slot, ItemVariant variant, long amount, boolean markDirty) {
        this.getSlot(slot).setStackUnsafe(variant, amount, markDirty);
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
            private long modCount = -1;

            @Override
            public boolean needsSyncing() {
                return MachineItemStorageImpl.this.getModCount() != this.modCount;
            }

            @Override
            public void sync(FriendlyByteBuf buf) {
                this.modCount = MachineItemStorageImpl.this.modCount.getModCount();
                for (ItemSlot slot : MachineItemStorageImpl.this.inventory) {
                    slot.getResource().toPacket(buf);
                    buf.writeVarLong(slot.getAmount());
                }
            }

            @Override
            public void read(FriendlyByteBuf buf) {
                for (ItemSlot slot : MachineItemStorageImpl.this.inventory) {
                    slot.setStackUnsafe(ItemVariant.fromPacket(buf), buf.readVarLong(), false);
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
    private class CombinedIterator implements Iterator<StorageView<ItemVariant>> {
        private final Iterator<ItemSlot> partIterator = Iterators.forArray(MachineItemStorageImpl.this.inventory);
        // Always holds the next StorageView<T>, except during next() while the iterator is being advanced.
        private Iterator<StorageView<ItemVariant>> currentPartIterator = null;

        private CombinedIterator() {
            advanceCurrentPartIterator();
        }

        @Override
        public boolean hasNext() {
            return currentPartIterator != null && currentPartIterator.hasNext();
        }

        @Override
        public StorageView<ItemVariant> next() {
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
                this.currentPartIterator = partIterator.next().iterator();

                if (this.currentPartIterator.hasNext()) {
                    break;
                }
            }
        }
    }
}
