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

//package dev.galacticraft.machinelib.impl.storage.slot;
//
//import com.google.common.collect.Iterators;
//import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
//import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
//import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
//import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.nbt.Tag;
//import net.minecraft.network.FriendlyByteBuf;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.Iterator;
//
//public class SingletonResourceSlotGroupImpl<Resource, Stack, Slot extends ResourceSlot<Resource, Stack>> implements ResourceSlot<Resource, Stack>, SlotGroup<Resource, Stack, Slot> {
//    private final @NotNull Slot slot;
//    private final @NotNull SlotGroupType type;
//
//    public SingletonResourceSlotGroupImpl(@NotNull SlotGroupType type, @NotNull Slot slot) {
//        this.slot = slot;
//        this.type = type;
//    }
//
//    @Override
//    public long getModifications() {
//        return this.slot.getModifications();
//    }
//
//    @Override
//    public Tag createTag() {
//        return this.slot.createTag();
//    }
//
//    @Override
//    public void readTag(Tag tag) {
//        this.slot.readTag(tag);
//    }
//
//    @Override
//    public void writePacket(@NotNull FriendlyByteBuf buf) {
//        this.slot.writePacket(buf);
//    }
//
//    @Override
//    public void readPacket(@NotNull FriendlyByteBuf buf) {
//        this.slot.readPacket(buf);
//    }
//
//    @Override
//    public @NotNull ResourceFilter<Resource> getFilter() {
//        return this.slot.getFilter();
//    }
//
//    @Override
//    public @Nullable Resource getResource() {
//        return this.slot.getResource();
//    }
//
//    @Override
//    public long getAmount() {
//        return this.slot.getAmount();
//    }
//
//    @Override
//    public @Nullable CompoundTag getTag() {
//        return this.slot.getTag();
//    }
//
//    @Override
//    public @Nullable CompoundTag copyTag() {
//        return this.slot.copyTag();
//    }
//
//    @Override
//    public long getCapacity() {
//        return this.slot.getCapacity();
//    }
//
//    @Override
//    public long getRealCapacity() {
//        return this.slot.getRealCapacity();
//    }
//
//    @Override
//    public @NotNull Stack createStack() {
//        return this.slot.createStack();
//    }
//
//    @Override
//    public @NotNull Stack copyStack() {
//        return this.slot.copyStack();
//    }
//
//    @Override
//    public boolean insertOne(@NotNull Resource resource) {
//        return this.slot.insertOne(resource);
//    }
//
//    @Override
//    public boolean insertOne(@NotNull Resource resource, @Nullable TransactionContext context) {
//        return this.slot.insertOne(resource, context);
//    }
//
//    @Override
//    public boolean insertOne(@NotNull Resource resource, @Nullable CompoundTag tag) {
//        return this.slot.insertOne(resource, tag);
//    }
//
//    @Override
//    public boolean insertOne(@NotNull Resource resource, @Nullable CompoundTag tag, @Nullable TransactionContext context) {
//        return this.slot.insertOne(resource, tag, context);
//    }
//
//    @Override
//    public boolean extractOne(@Nullable TransactionContext context) {
//        return this.slot.extractOne(context);
//    }
//
//    @Override
//    public boolean extractOne(@Nullable Resource resource) {
//        return this.slot.extractOne(resource);
//    }
//
//    @Override
//    public boolean extractOne(@Nullable Resource resource, @Nullable TransactionContext context) {
//        return this.slot.extractOne(resource, context);
//    }
//
//    @Override
//    public boolean extractOne(@Nullable Resource resource, @Nullable CompoundTag tag) {
//        return this.slot.extractOne(resource, tag);
//    }
//
//    @Override
//    public boolean extractOne(@Nullable Resource resource, @Nullable CompoundTag tag, @Nullable TransactionContext context) {
//        return this.slot.extractOne(resource, tag, context);
//    }
//
//    @Override
//    public long extract(long amount) {
//        return this.slot.extract(amount);
//    }
//
//    @Override
//    public long extract(long amount, @Nullable TransactionContext context) {
//        return this.slot.extract(amount, context);
//    }
//
//    @Override
//    public boolean contains(@NotNull Resource resource) {
//        return this.slot.contains(resource);
//    }
//
//    @Override
//    public boolean contains(@NotNull Resource resource, @Nullable CompoundTag tag) {
//        return this.slot.contains(resource, tag);
//    }
//
//    @Override
//    public @NotNull SlotGroupType getType() {
//        return this.type;
//    }
//
//    @Override
//    public int size() {
//        return 1;
//    }
//
//    @Override
//    public boolean isEmpty() {
//        return this.slot.isEmpty();
//    }
//
//    @Override
//    public boolean isFull() {
//        return this.slot.isFull();
//    }
//
//    @Override
//    public long insert(@NotNull Resource resource, long amount) {
//        return this.slot.insert(resource, amount);
//    }
//
//    @Override
//    public long insert(@NotNull Resource resource, long amount, @Nullable TransactionContext context) {
//        return this.slot.insert(resource, amount, context);
//    }
//
//    @Override
//    public long insert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
//        return this.slot.insert(resource, tag, amount);
//    }
//
//    @Override
//    public long insert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context) {
//        return this.slot.insert(resource, tag, amount, context);
//    }
//
//    @Override
//    public long extract(@Nullable Resource resource, long amount) {
//        return this.slot.extract(resource, amount);
//    }
//
//    @Override
//    public boolean extractOne() {
//        return this.slot.extractOne();
//    }
//
//    @Override
//    public long extract(@Nullable Resource resource, long amount, @Nullable TransactionContext context) {
//        return this.slot.extract(resource, amount, context);
//    }
//
//    @Override
//    public long extract(@Nullable Resource resource, @Nullable CompoundTag tag, long amount) {
//        return this.slot.extract(resource, tag, amount);
//    }
//
//    @Override
//    public long extract(@Nullable Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context) {
//        return this.slot.extract(resource, tag, amount, context);
//    }
//
//    @Override
//    public @NotNull Slot getSlot(int slot) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot;
//    }
//
//    @Override
//    public @Nullable ResourceFilter<Resource> getFilter(int slot) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.getFilter();
//    }
//
//    @Override
//    public @Nullable Resource getResource(int slot) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.getResource();
//    }
//
//    @Override
//    public long getAmount(int slot) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.getAmount();
//    }
//
//    @Override
//    public @Nullable CompoundTag getTag(int slot) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.getTag();
//    }
//
//    @Override
//    public @Nullable CompoundTag copyTag(int slot) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.copyTag();
//    }
//
//    @Override
//    public long getCapacity(int slot) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.getCapacity();
//    }
//
//    @Override
//    public long getRealCapacity(int slot) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.getRealCapacity();
//    }
//
//    @Override
//    public @NotNull Stack createStack(int slot) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.createStack();
//    }
//
//    @Override
//    public @NotNull Stack copyStack(int slot) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.copyStack();
//    }
//
//    @Override
//    public boolean insertOne(int slot, @NotNull Resource resource) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.insertOne(resource);
//    }
//
//    @Override
//    public boolean insertOne(int slot, @NotNull Resource resource, @Nullable TransactionContext context) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.insertOne(resource, context);
//    }
//
//    @Override
//    public boolean insertOne(int slot, @NotNull Resource resource, @Nullable CompoundTag tag) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.insertOne(resource, tag);
//    }
//
//    @Override
//    public boolean insertOne(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, @Nullable TransactionContext context) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.insertOne(resource, tag, context);
//    }
//
//    @Override
//    public long insert(int slot, @NotNull Resource resource, long amount) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.insert(resource, amount);
//    }
//
//    @Override
//    public long insert(int slot, @NotNull Resource resource, long amount, @Nullable TransactionContext context) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.insert(resource, amount, context);
//    }
//
//    @Override
//    public long insert(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.insert(resource, tag, amount);
//    }
//
//    @Override
//    public long insert(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.insert(resource, tag, amount, context);
//    }
//
//    @Override
//    public boolean extractOne(int slot) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.extractOne();
//    }
//
//    @Override
//    public boolean extractOne(int slot, @Nullable TransactionContext context) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.extractOne(context);
//    }
//
//    @Override
//    public boolean extractOne(int slot, @Nullable Resource resource) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.extractOne(resource);
//    }
//
//    @Override
//    public boolean extractOne(int slot, @Nullable Resource resource, @Nullable TransactionContext context) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.extractOne(resource, context);
//    }
//
//    @Override
//    public boolean extractOne(int slot, @Nullable Resource resource, @Nullable CompoundTag tag) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.extractOne(resource, tag);
//    }
//
//    @Override
//    public boolean extractOne(int slot, @Nullable Resource resource, @Nullable CompoundTag tag, @Nullable TransactionContext context) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.extractOne(resource, tag, context);
//    }
//
//    @Override
//    public long extract(int slot, long amount) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.extract(amount);
//    }
//
//    @Override
//    public long extract(int slot, long amount, @Nullable TransactionContext context) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.extract(amount, context);
//    }
//
//    @Override
//    public long extract(int slot, @Nullable Resource resource, long amount) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.extract(resource, amount);
//    }
//
//    @Override
//    public long extract(int slot, @Nullable Resource resource, long amount, @Nullable TransactionContext context) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.extract(resource, amount, context);
//    }
//
//    @Override
//    public long extract(int slot, @Nullable Resource resource, @Nullable CompoundTag tag, long amount) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.extract(resource, tag, amount);
//    }
//
//    @Override
//    public long extract(int slot, @Nullable Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.extract(resource, tag, amount, context);
//    }
//
//    @Override
//    public boolean contains(int slot, @NotNull Resource resource) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.contains(resource);
//    }
//
//    @Override
//    public boolean contains(int slot, @NotNull Resource resource, @Nullable CompoundTag tag) {
//        if (slot != 0) throw new IndexOutOfBoundsException(slot);
//        return this.slot.contains(resource, tag);
//    }
//
//    @NotNull
//    @Override
//    public Iterator<Slot> iterator() {
//        return Iterators.singletonIterator(this.slot);
//    }
//
//    @Override
//    public ResourceSlot<Resource, Stack>[] getSlots() {
//        return new ResourceSlot[] { this.slot };
//    }
//}
