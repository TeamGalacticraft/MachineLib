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

import dev.galacticraft.api.block.entity.MachineBlockEntity;
import dev.galacticraft.api.machine.storage.MachineItemStorage;
import dev.galacticraft.api.machine.storage.StorageSlot;
import dev.galacticraft.api.machine.storage.io.ExposedStorage;
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.machine.storage.io.SlotGroup;
import dev.galacticraft.api.machine.storage.io.StorageSelection;
import dev.galacticraft.api.screen.MachineScreenHandler;
import dev.galacticraft.api.screen.StorageSyncHandler;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

public enum EmptyMachineItemStorage implements MachineItemStorage, ExposedStorage<Item, ItemVariant> {
    INSTANCE;

    private static final Container EMPTY_INVENTORY = new SimpleContainer(0);
    private static final SlotGroup[] NO_SLOTS = new SlotGroup[0];

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
    public boolean canExposedExtract(int slot) {
        return false;
    }

    @Override
    public boolean canExposedInsert(int slot) {
        return false;
    }

    @Override
    public boolean canPlayerInsert(int slot) {
        return false;
    }

    @Override
    public @NotNull ItemStack extract(int slot, long amount, @Nullable TransactionContext context) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack extract(int slot, @NotNull TagKey<Item> tag, long amount, @Nullable TransactionContext context) {
        return ItemStack.EMPTY;
    }

    @Override
    public long extract(int slot, @NotNull Item resource, long amount, @Nullable TransactionContext context) {
        return 0;
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
    public long getModCount() {
        return 0;
    }

    @Override
    public long getModCountUnsafe() {
        return 0;
    }

    @Override
    public long getSlotModCount(int slot) {
        return 0;
    }

    @Override
    public long getSlotModCountUnsafe(int slot) {
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
    public @NotNull StorageSlot<Item, ItemVariant, ItemStack> getSlot(int slot) {
        throw new IndexOutOfBoundsException("No slots!");
    }

    @Override
    public @NotNull Predicate<ItemVariant> getFilter(int slot) {
        return v -> false;
    }

    @Override
    public boolean canAccess(@NotNull Player player) {
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
    public @NotNull Tag writeNbt() {
        return ByteTag.ZERO;
    }

    @Override
    public void readNbt(@NotNull Tag nbt) {
    }

    @Override
    public void clearContent() {
    }

    @Override
    public ExposedStorage<Item, ItemVariant> view() {
        return this;
    }

    @Override
    public void setSlotUnsafe(int slot, ItemVariant variant, long amount, boolean markDirty) {
    }

    @Override
    public Storage<ItemVariant> getExposedStorage(@Nullable StorageSelection either, @NotNull ResourceFlow flow) {
        return this;
    }

    @Override
    public @NotNull SlotGroup @NotNull [] getGroups() {
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
    public long insert(@NotNull ItemVariant resource, long maxAmount, @NotNull TransactionContext transaction) {
        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public long extract(@NotNull ItemVariant resource, long maxAmount, @NotNull TransactionContext transaction) {
        return 0;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return ObjectIterators.emptyIterator();
    }

    @Override
    public <M extends MachineBlockEntity> void addSlots(@NotNull MachineScreenHandler<M> handler) {
    }

    @Contract(pure = true)
    @Override
    public @NotNull Container playerInventory() {
        return EMPTY_INVENTORY;
    }

    @Contract(pure = true)
    @Override
    public @NotNull Container subInv(int start, int size) {
        if (start > 0 || size > 0) throw new IndexOutOfBoundsException("Index out of bounds");
        return EMPTY_INVENTORY;
    }
}
