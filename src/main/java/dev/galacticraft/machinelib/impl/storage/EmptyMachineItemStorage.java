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

package dev.galacticraft.machinelib.impl.storage;

import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.ResourceFilter;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;

public class EmptyMachineItemStorage implements MachineItemStorage {
    public static final @NotNull EmptyMachineItemStorage INSTANCE = new EmptyMachineItemStorage();

    private EmptyMachineItemStorage() {
    }

    @Override
    public @Nullable MenuSyncHandler createSyncHandler() {
        return null;
    }

    @Override
    public long getModifications() {
        return -1;
    }

    @Override
    public void markModified(@Nullable TransactionContext context) {

    }

    @Override
    public void markModified() {

    }

    @Override
    public void setListener(Runnable listener) {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int getContainerSize() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public ItemStack getItem(int i) {
        throw new IndexOutOfBoundsException(i);
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        throw new IndexOutOfBoundsException(i);
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        throw new IndexOutOfBoundsException(i);
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        throw new IndexOutOfBoundsException(i);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public boolean isFull() {
        return true;
    }

    @Override
    public ItemResourceSlot[] getSlots() {
        return new ItemResourceSlot[0];
    }

    @Override
    public boolean canInsert(@NotNull Item item) {
        return false;
    }

    @Override
    public boolean canInsert(@NotNull Item item, @Nullable CompoundTag tag) {
        return false;
    }

    @Override
    public boolean canInsert(@NotNull Item item, long amount) {
        return false;
    }

    @Override
    public boolean canInsert(@NotNull Item item, @Nullable CompoundTag tag, long amount) {
        return false;
    }

    @Override
    public long tryInsert(@NotNull Item item, long amount) {
        return 0;
    }

    @Override
    public long tryInsert(@NotNull Item item, @Nullable CompoundTag tag, long amount) {
        return 0;
    }

    @Override
    public long insert(@NotNull Item item, long amount) {
        return 0;
    }

    @Override
    public long insert(@NotNull Item item, @Nullable CompoundTag tag, long amount) {
        return 0;
    }

    @Override
    public long insertMatching(@NotNull Item item, long amount) {
        return 0;
    }

    @Override
    public long insertMatching(@NotNull Item item, @Nullable CompoundTag tag, long amount) {
        return 0;
    }

    @Override
    public boolean containsAny(@NotNull Item item) {
        return false;
    }

    @Override
    public boolean containsAny(@NotNull Item item, @Nullable CompoundTag tag) {
        return false;
    }

    @Override
    public boolean canExtract(@NotNull Item item, long amount) {
        return false;
    }

    @Override
    public boolean canExtract(@NotNull Item item, @Nullable CompoundTag tag, long amount) {
        return false;
    }

    @Override
    public long tryExtract(@NotNull Item item, long amount) {
        return 0;
    }

    @Override
    public long tryExtract(@NotNull Item item, @Nullable CompoundTag tag, long amount) {
        return 0;
    }

    @Override
    public boolean extractOne(@NotNull Item item) {
        return false;
    }

    @Override
    public boolean extractOne(@NotNull Item item, @Nullable CompoundTag tag) {
        return false;
    }

    @Override
    public long extract(@NotNull Item item, long amount) {
        return 0;
    }

    @Override
    public long extract(@NotNull Item item, @Nullable CompoundTag tag, long amount) {
        return 0;
    }

    @Override
    public @NotNull ItemResourceSlot getSlot(int slot) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public @Nullable ResourceFilter<Item> getFilter(int slot) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public @NotNull ResourceFilter<Item> getStrictFilter(int slot) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public @Nullable Item getResource(int slot) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public long getAmount(int slot) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public @Nullable CompoundTag getTag(int slot) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public @Nullable CompoundTag copyTag(int slot) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public long getCapacity(int slot) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public long getCapacityFor(int slot, @NotNull Item item) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public long getRealCapacity(int slot) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public boolean isEmpty(int slot) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public boolean isFull(int slot) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public boolean canInsert(int slot, @NotNull Item item) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public boolean canInsert(int slot, @NotNull Item item, @Nullable CompoundTag tag) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public boolean canInsert(int slot, @NotNull Item item, long amount) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public boolean canInsert(int slot, @NotNull Item item, @Nullable CompoundTag tag, long amount) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public long tryInsert(int slot, @NotNull Item item, long amount) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public long tryInsert(int slot, @NotNull Item item, @Nullable CompoundTag tag, long amount) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public long insert(int slot, @NotNull Item item, long amount) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public long insert(int slot, @NotNull Item item, @Nullable CompoundTag tag, long amount) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public boolean containsAny(int slot, @NotNull Item item) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public boolean containsAny(int slot, @NotNull Item item, @Nullable CompoundTag tag) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public boolean canExtract(int slot, long amount) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public boolean canExtract(int slot, @NotNull Item item, long amount) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public boolean canExtract(int slot, @NotNull Item item, @Nullable CompoundTag tag, long amount) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public long tryExtract(int slot, long amount) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public long tryExtract(int slot, @Nullable Item item, long amount) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public long tryExtract(int slot, @Nullable Item item, @Nullable CompoundTag tag, long amount) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public boolean extractOne(int slot) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public boolean extractOne(int slot, @Nullable Item item) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public boolean extractOne(int slot, @Nullable Item item, @Nullable CompoundTag tag) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public long extract(int slot, long amount) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public long extract(int slot, @Nullable Item item, long amount) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public long extract(int slot, @Nullable Item item, @Nullable CompoundTag tag, long amount) {
        throw new IndexOutOfBoundsException(slot);
    }

    @Override
    public long insert(@NotNull Item item, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context) {
        return 0;
    }

    @Override
    public long extract(@Nullable Item item, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context) {
        return 0;
    }

    @Override
    public void readTag(@NotNull ListTag tag) {

    }

    @Override
    public void readPacket(@NotNull FriendlyByteBuf buf) {

    }

    @Override
    public @NotNull ListTag createTag() {
        return new ListTag();
    }

    @Override
    public void writePacket(@NotNull FriendlyByteBuf buf) {

    }

    @NotNull
    @Override
    public Iterator<ItemResourceSlot> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public void clearContent() {

    }
}
