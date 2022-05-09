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

package dev.galacticraft.impl.machine.storage.empty;

import com.mojang.datafixers.util.Either;
import dev.galacticraft.api.block.entity.MachineBlockEntity;
import dev.galacticraft.api.machine.storage.MachineItemStorage;
import dev.galacticraft.api.machine.storage.io.ExposedStorage;
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.machine.storage.io.ResourceType;
import dev.galacticraft.api.machine.storage.io.SlotType;
import dev.galacticraft.api.screen.MachineScreenHandler;
import dev.galacticraft.api.screen.StorageSyncHandler;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtElement;
import net.minecraft.tag.Tag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

public enum EmptyMachineItemStorage implements MachineItemStorage, ExposedStorage<Item, ItemVariant> {
    INSTANCE;

    private static final Inventory EMPTY_INVENTORY = new SimpleInventory(0);
    private static final SlotType<Item, ItemVariant>[] NO_SLOTS = new SlotType[0];

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public @NotNull ItemStack getStack(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemVariant getVariant(int slot) {
        return ItemVariant.blank();
    }

    @Override
    public @NotNull ItemVariant getResource(int slot) {
        return ItemVariant.blank();
    }

    @Override
    public long getAmount(int slot) {
        return 0;
    }

    @Override
    public long getCapacity(int slot) {
        return 0;
    }

    @Override
    public @NotNull ResourceType<Item, ItemVariant> getResource() {
        return ResourceType.ITEM;
    }

    @Override
    public boolean canExposedExtract(int slot) {
        return false;
    }

    @Override
    public boolean canExposedInsert(int slot) {
        return false;
    }

    @Override
    public @NotNull ItemStack extract(int slot, long amount, @Nullable TransactionContext context) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack extract(int slot, @NotNull Tag<Item> tag, long amount, @Nullable TransactionContext context) {
        return ItemStack.EMPTY;
    }

    @Override
    public long extract(int slot, @NotNull Item resource, long amount, @Nullable TransactionContext context) {
        return 0;
    }

    @Override
    public @NotNull ItemStack replace(int slot, @NotNull ItemVariant variant, long amount, @Nullable TransactionContext context) {
        return variant.toStack(Math.toIntExact(amount));
    }

    @Override
    public long insert(int slot, @NotNull ItemVariant variant, long amount, @Nullable TransactionContext context) {
        return 0;
    }

    @Override
    public long extract(int slot, @NotNull ItemVariant variant, long amount, @Nullable TransactionContext context) {
        return 0;
    }

    @Override
    public long getMaxCount(int slot) {
        return 0;
    }

    @Override
    public int getModCount() {
        return 0;
    }

    @Override
    public int getModCountUnsafe() {
        return 0;
    }

    @Override
    public int getSlotModCount(int slot) {
        return 0;
    }

    @Override
    public boolean isFull(int slot) {
        return true;
    }

    @Override
    public boolean isEmpty(int slot) {
        return true;
    }

    @Override
    public @NotNull SingleVariantStorage<ItemVariant> getSlot(int slot) {
        throw new IndexOutOfBoundsException("No slots!");
    }

    @Override
    public @NotNull Predicate<ItemVariant> getFilter(int slot) {
        return v -> false;
    }

    @Override
    public boolean canAccess(@NotNull PlayerEntity player) {
        return false;
    }

    @Override
    public boolean canAccept(int slot, @NotNull ItemVariant variant) {
        return false;
    }

    @Override
    public long count(@NotNull Item resource) {
        return 0;
    }

    @Override
    public long count(@NotNull ItemVariant resource) {
        return 0;
    }

    @Override
    public boolean containsAny(@NotNull Collection<Item> resources) {
        return false;
    }

    @Override
    public @NotNull NbtElement writeNbt() {
        return NbtByte.ZERO;
    }

    @Override
    public void readNbt(@NotNull NbtElement nbt) {
    }

    @Override
    public void clear() {
    }

    @Override
    public ExposedStorage<Item, ItemVariant> view() {
        return this;
    }

    @Override
    public void setSlot(int slot, ItemVariant variant, long amount, boolean markDirty) {
    }

    @Override
    public Storage<ItemVariant> getExposedStorage(@Nullable Either<Integer, SlotType<?, ?>> either, @NotNull ResourceFlow flow) {
        return this;
    }

    @Override
    public @NotNull SlotType<Item, ItemVariant> @NotNull [] getTypes() {
        return NO_SLOTS;
    }

    @Override
    public @NotNull StorageSyncHandler createSyncHandler() {
        return StorageSyncHandler.DEFAULT;
    }

    @Override
    public boolean supportsInsertion() {
        return false;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator(TransactionContext transaction) {
        return ObjectIterators.emptyIterator();
    }

    @Override
    public <M extends MachineBlockEntity> void addSlots(@NotNull MachineScreenHandler<M> handler) {
    }

    @Contract(pure = true)
    @Override
    public @NotNull Inventory playerInventory() {
        return EMPTY_INVENTORY;
    }

    @Contract(pure = true)
    @Override
    public @NotNull Inventory subInv(int start, int size) {
        if (start > 0 || size > 0) throw new IndexOutOfBoundsException("Index out of bounds");
        return EMPTY_INVENTORY;
    }
}
