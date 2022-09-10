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
import dev.galacticraft.api.machine.storage.io.SlotGroup;
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
import java.util.function.Predicate;

public class MachineItemStorageImpl implements MachineItemStorage {
    private final int size;
    private final @NotNull ItemSlotDisplay[] displays;
    private final @NotNull ItemSlot[] inventory;
    private final @NotNull SlotGroup[] types;
    private final @NotNull Predicate<ItemVariant> @NotNull [] filters;
    private final boolean @NotNull [] playerInsertion;

    private final ModCount modCount = ModCount.root();
    private final @NotNull ExposedStorage<Item, ItemVariant> view = ExposedStorage.view(this);
    private final Container playerInventory;

    public MachineItemStorageImpl(int size, SlotGroup[] types, Predicate<ItemVariant> @NotNull [] filters, boolean @NotNull [] playerInsertion, int[] counts, ItemSlotDisplay[] displays) {
        this.size = size;
        this.displays = displays;
        this.inventory = new ItemSlot[this.size];
        this.filters = filters;
        this.playerInsertion = playerInsertion;
        this.types = types;

        for (int i = 0; i < this.inventory.length; i++) {
            this.inventory[i] = new ItemSlot(counts[i], this.modCount);
        }

        this.playerInventory = new PlayerExposedVanillaInventory(this);
    }

    @Override
    public int size() {
        return this.size;
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
    public boolean canExposedExtract(int slot) {
        return this.types[slot].isAutomatable();
    }

    @Override
    public boolean canExposedInsert(int slot) {
        return this.canPlayerInsert(slot) && this.types[slot].isAutomatable();
    }

    @Override
    public boolean canPlayerInsert(int slot) {
        return this.playerInsertion[slot];
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return Iterators.forArray(this.inventory); // we do not need to iterate over the inner slots' iterator as there's only one slot.
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
    public boolean canAccess(@NotNull Player player) {
        return true;
    }

    @Override
    public boolean canAccept(int slot, @NotNull ItemVariant variant) {
        return this.filters[slot].test(variant);
    }

    @Override
    public Predicate<ItemVariant> getFilter(int slot) {
        return this.filters[slot];
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
        if (nbt instanceof ListTag list) {
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
    public @NotNull SlotGroup @NotNull [] getGroups() {
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
}
