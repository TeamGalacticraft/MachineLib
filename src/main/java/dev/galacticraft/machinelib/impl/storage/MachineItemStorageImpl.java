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

package dev.galacticraft.machinelib.impl.storage;

import com.google.common.collect.Iterators;
import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.screen.MachineScreenHandler;
import dev.galacticraft.machinelib.api.screen.StorageSyncHandler;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.api.transfer.cache.ModCount;
import dev.galacticraft.machinelib.api.util.GenericApiUtil;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.compat.ReadOnlySubInv;
import dev.galacticraft.machinelib.impl.storage.slot.ItemSlot;
import dev.galacticraft.machinelib.impl.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.impl.storage.slot.VanillaWrappedItemSlot;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

@ApiStatus.Internal
public final class MachineItemStorageImpl implements MachineItemStorage {
    private final int size;
    private final @NotNull ItemSlotDisplay[] displays;
    private final @NotNull ItemSlot[] inventory;
    private final @NotNull SlotGroup[] types;
    private final boolean @NotNull [] playerInsertion;

    private final ModCount modCount = ModCount.root();
    private final Container playerInventory;

    public MachineItemStorageImpl(int size, SlotGroup[] types, Predicate<ItemVariant> @NotNull [] filters, boolean @NotNull [] playerInsertion, int[] counts, ItemSlotDisplay[] displays) {
        this.size = size;
        this.displays = displays;
        this.inventory = new ItemSlot[this.size];
        this.playerInsertion = playerInsertion;
        this.types = types;

        for (int i = 0; i < this.inventory.length; i++) {
            this.inventory[i] = new ItemSlot(counts[i], filters[i], this.modCount);
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
    public @NotNull Iterator<StorageView<ItemVariant>> iterator() {
        return Iterators.forArray(this.inventory); // we do not need to iterate over the inner slots' iterator as there's only one slot.
    }

    @Override
    public boolean canAccess(@NotNull Player player) {
        return true;
    }

    @Override
    public boolean canAccept(int slot, @NotNull ItemVariant variant) {
        return this.getSlot(slot).canAccept(variant);
    }

    @Override
    public Predicate<ItemVariant> getFilter(int slot) {
        return this.getSlot(slot).getFilter();
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
            handler.addSlot(new VanillaWrappedItemSlot(this, i, this.displays[i], handler.player));
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
            compound.putLong(Constant.Nbt.AMOUNT, itemSlot.getAmount());
            list.add(compound);
        }
        return list;
    }

    @Override
    public void readNbt(@NotNull Tag nbt) {
        if (nbt instanceof ListTag list) {
            for (int i = 0; i < list.size(); i++) {
                CompoundTag compound = list.getCompound(i);
                this.getSlot(i).setStack(ItemVariant.fromNbt(compound), compound.getLong(Constant.Nbt.AMOUNT));
            }
        }
    }

    @Override
    public void clearContent() {
        GenericApiUtil.noTransaction();
        for (ItemSlot itemSlot : this.inventory) {
            itemSlot.setStack(ItemVariant.blank(), 0);
        }
    }

    @Override
    public void setSlot(int slot, ItemVariant variant, long amount) {
        this.getSlot(slot).setStack(variant, amount);
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
            public void sync(@NotNull FriendlyByteBuf buf) {
                this.modCount = MachineItemStorageImpl.this.modCount.getModCount();
                for (ItemSlot slot : MachineItemStorageImpl.this.inventory) {
                    slot.getResource().toPacket(buf);
                    buf.writeVarLong(slot.getAmount());
                }
            }

            @Override
            public void read(@NotNull FriendlyByteBuf buf) {
                for (ItemSlot slot : MachineItemStorageImpl.this.inventory) {
                    slot.setStack(ItemVariant.fromPacket(buf), buf.readVarLong());
                }
            }
        };
    }

    @ApiStatus.Internal
    public void incrementModCountUnsafe() {
        this.modCount.increment();
    }
}
