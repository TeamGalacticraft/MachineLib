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

package dev.galacticraft.machinelib.impl.storage.slot;

import com.google.common.collect.Iterators;
import dev.galacticraft.machinelib.api.storage.MutableModifiable;
import dev.galacticraft.machinelib.api.storage.ResourceFilter;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
import dev.galacticraft.machinelib.impl.Utils;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class SlotGroupImpl<Resource, Stack, Slot extends ResourceSlot<Resource, Stack>> implements SlotGroup<Resource, Stack, Slot> {
    private final @NotNull SlotGroupType type;
    private final @NotNull Slot @NotNull [] slots;
    private MutableModifiable parent;
    private long modifications = 0;

    public SlotGroupImpl(@NotNull SlotGroupType type, @NotNull Slot @NotNull [] slots) {
        this.type = type;
        this.slots = slots;
        for (Slot slot : this.slots) {
            slot._setGroup(this);
        }
    }

    @Override
    public void _setParent(@NotNull MutableModifiable modifiable) {
        this.parent = modifiable;
    }

    @NotNull
    @Override
    public SlotGroupType getType() {
        return this.type;
    }

    @Override
    public int size() {
        return this.slots.length;
    }

    @Override
    public boolean isEmpty() {
        for (Slot slot : this.slots) {
            if (!slot.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public boolean isFull() {
        for (Slot slot : this.slots) {
            if (!slot.isFull()) return false;
        }
        return true;
    }

    @Override
    public @NotNull ResourceFilter<Resource> getStrictFilter(int slot) {
        return this.slots[slot].getStrictFilter();
    }

    @Override
    public boolean canInsertOne(@NotNull Resource resource) {
        for (Slot slot : this.slots) {
            if (slot.canInsertOne(resource)) return true;
        }
        return false;
    }

    @Override
    public boolean canInsertOne(@NotNull Resource resource, @Nullable CompoundTag tag) {
        for (Slot slot : this.slots) {
            if (slot.canInsertOne(resource, tag)) return true;
        }
        return false;
    }

    @Override
    public boolean canInsert(@NotNull Resource resource, long amount) {
        long total = 0;
        for (Slot slot : this.slots) {
            if (slot.canInsert(resource, amount)) {
                total += slot.getAmount();
                if (total >= amount) return true;
            }
        }
        return total >= amount;
    }

    @Override
    public boolean canInsert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        long total = 0;
        for (Slot slot : this.slots) {
            if (slot.canInsert(resource, tag, amount)) {
                total += slot.getAmount();
                if (total >= amount) return true;
            }
        }
        return total >= amount;
    }

    @Override
    public boolean insertOne(@NotNull Resource resource, @Nullable TransactionContext context) {
        for (Slot slot : this.slots) {
            if (slot.insertOne(resource, context)) return true;
        }
        return false;
    }

    @Override
    public boolean insertOne(@NotNull Resource resource, @Nullable CompoundTag tag, @Nullable TransactionContext context) {
        for (Slot slot : this.slots) {
            if (slot.insertOne(resource, tag, context)) return true;
        }
        return false;
    }

    @Override
    public long insert(@NotNull Resource resource, long amount, @Nullable TransactionContext context) {
        long requested = amount;
        for (Slot slot : this.slots) {
            if (amount == 0) return requested;
            if (slot.getResource() == resource || slot.isEmpty()) {
                amount -= slot.insert(resource, amount, context);
            }
        }
        return requested - amount;
    }

    @Override
    public long insert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context) {
        long requested = amount;
        for (Slot slot : this.slots) {
            if (amount == 0) return requested;
            if ((slot.getResource() == resource && Utils.tagsEqual(slot.getTag(), tag)) || slot.isEmpty()) {
                amount -= slot.insert(resource, amount, context);
            }
        }
        return requested - amount;
    }

    @Override
    public long extract(@Nullable Resource resource, long amount, @Nullable TransactionContext context) {
        long requested = amount;
        for (Slot slot : this.slots) {
            if (amount == 0) return requested;
            if (resource == null || slot.contains(resource)) {
                amount -= slot.extract(resource, amount, context);
            }
        }
        return requested - amount;
    }

    @Override
    public long extract(@Nullable Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context) {
        long requested = amount;
        for (Slot slot : this.slots) {
            if (amount == 0) return requested;
            if (resource == null || slot.contains(resource, tag)) {
                amount -= slot.extract(resource, amount, context);
            }
        }
        return requested - amount;
    }

    @Override
    public boolean containsAny(@NotNull Resource resource) {
        for (Slot slot : this.slots) {
            if (slot.contains(resource)) return true;
        }
        return false;
    }

    @Override
    public boolean containsAny(@NotNull Resource resource, @Nullable CompoundTag tag) {
        for (Slot slot : this.slots) {
            if (slot.contains(resource)) return true;
        }
        return false;
    }

    @Override
    public boolean contains(@NotNull Resource resource, long amount) {
        long total = 0;
        for (Slot slot : this.slots) {
            if (slot.contains(resource)) {
                total += slot.getAmount();
                if (total >= amount) return true;
            }
        }
        return total >= amount;
    }

    @Override
    public boolean contains(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        long total = 0;
        for (Slot slot : this.slots) {
            if (slot.contains(resource, tag)) {
                total += slot.getAmount();
                if (total >= amount) return true;
            }
        }
        return total >= amount;
    }


    @Override
    public @NotNull Slot getSlot(int slot) {
        return this.slots[slot];
    }

    @Override
    public @Nullable ResourceFilter<Resource> getFilter(int slot) {
        return this.slots[slot].getFilter();
    }

    @Override
    public @Nullable Resource getResource(int slot) {
        return this.slots[slot].getResource();
    }

    @Override
    public long getAmount(int slot) {
        return this.slots[slot].getAmount();
    }

    @Override
    public @Nullable CompoundTag getTag(int slot) {
        return this.slots[slot].getTag();
    }

    @Override
    public @Nullable CompoundTag copyTag(int slot) {
        return this.slots[slot].copyTag();
    }

    @Override
    public long getCapacity(int slot) {
        return this.slots[slot].getCapacity();
    }

    @Override
    public long getRealCapacity(int slot) {
        return this.slots[slot].getRealCapacity();
    }

    @Override
    public @NotNull Stack createStack(int slot) {
        return this.slots[slot].createStack();
    }

    @Override
    public @NotNull Stack copyStack(int slot) {
        return this.slots[slot].copyStack();
    }

    @Override
    public boolean canInsertOne(int slot, @NotNull Resource resource) {
        return this.slots[slot].canInsertOne(resource);
    }

    @Override
    public boolean canInsertOne(int slot, @NotNull Resource resource, @Nullable CompoundTag tag) {
        return this.slots[slot].canInsertOne(resource, tag);
    }

    @Override
    public boolean canInsert(int slot, @NotNull Resource resource, long amount) {
        return this.slots[slot].canInsert(resource, amount);
    }

    @Override
    public boolean canInsert(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        return this.slots[slot].canInsert(resource, tag, amount);
    }

    @Override
    public boolean insertOne(int slot, @NotNull Resource resource, @Nullable TransactionContext context) {
        return this.slots[slot].insertOne(resource, context);
    }

    @Override
    public boolean insertOne(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, @Nullable TransactionContext context) {
        return this.slots[slot].insertOne(resource, tag, context);
    }

    @Override
    public long insert(int slot, @NotNull Resource resource, long amount, @Nullable TransactionContext context) {
        return this.slots[slot].insert(resource, amount, context);
    }

    @Override
    public long insert(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context) {
        return this.slots[slot].insert(resource, tag, amount, context);
    }

    @Override
    public boolean extractOne(int slot, @Nullable TransactionContext context) {
        return this.slots[slot].extractOne(context);
    }

    @Override
    public boolean extractOne(int slot, @Nullable Resource resource, @Nullable TransactionContext context) {
        return this.slots[slot].extractOne(resource, context);
    }

    @Override
    public boolean extractOne(int slot, @Nullable Resource resource, @Nullable CompoundTag tag, @Nullable TransactionContext context) {
        return this.slots[slot].extractOne(resource, tag, context);
    }

    @Override
    public long extract(int slot, long amount, @Nullable TransactionContext context) {
        return this.slots[slot].extract(amount, context);
    }

    @Override
    public long extract(int slot, @Nullable Resource resource, long amount, @Nullable TransactionContext context) {
        return this.slots[slot].extract(resource, amount, context);
    }

    @Override
    public long extract(int slot, @Nullable Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context) {
        return this.slots[slot].extract(resource, tag, amount, context);
    }

    @Override
    public boolean contains(int slot, @NotNull Resource resource) {
        return this.slots[slot].contains(resource);
    }

    @Override
    public boolean contains(int slot, @NotNull Resource resource, @Nullable CompoundTag tag) {
        return this.slots[slot].contains(resource, tag);
    }

    @Override
    public boolean contains(int slot, @NotNull Resource resource, long amount) {
        return this.slots[slot].contains(resource, amount);
    }

    @Override
    public boolean contains(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        return this.slots[slot].contains(resource, tag, amount);
    }

    @NotNull
    @Override
    public Iterator<Slot> iterator() {
        return Iterators.forArray(this.slots);
    }

    @Override
    public long getModifications() {
        return this.modifications;
    }

    @Override
    public void revertModification() {
        if (this.parent == null) throw new AssertionError();
        this.parent.revertModification();
        this.modifications--;
    }

    @Override
    public void markModified() {
        if (this.parent == null) throw new AssertionError();
        this.parent.markModified();
        this.modifications++;
    }

    @Override
    public void markModified(@Nullable TransactionContext context) {
        this.parent.markModified(context);
        this.modifications++;

        if (context != null) {
            context.addCloseCallback((context1, result) -> {
                if (result.wasAborted()) {
                    this.modifications--;
                }
            });
        }
    }

    @Override
    public Slot[] getSlots() {
        return this.slots;
    }

    @Override
    public @NotNull ListTag createTag() {
        ListTag tag = new ListTag();
        for (Slot slot : this.slots) {
            tag.add(slot.createTag());
        }
        return tag;
    }

    @Override
    public void readTag(@NotNull ListTag tag) {
        for (int i = 0; i < tag.size(); i++) {
            this.slots[i].readTag(tag.getCompound(i));
        }
    }

    @Override
    public void writePacket(@NotNull FriendlyByteBuf buf) {
        for (Slot slot : this.slots) {
            slot.writePacket(buf);
        }
    }

    @Override
    public void readPacket(@NotNull FriendlyByteBuf buf) {
        for (Slot slot : this.slots) {
            slot.readPacket(buf);
        }
    }
}
