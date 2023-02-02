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
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class SlotGroupImpl<Resource, Stack, Slot extends ResourceSlot<Resource, Stack>> implements SlotGroup<Resource, Stack, Slot> {
    private final @NotNull Slot @NotNull [] slots;
    private MutableModifiable parent;
    private long modifications = 0;

    public SlotGroupImpl(@NotNull Slot @NotNull [] slots) {
        this.slots = slots;
        for (Slot slot : this.slots) {
            slot._setParent(this);
        }
    }

    @Override
    public void _setParent(@NotNull MutableModifiable modifiable) {
        this.parent = modifiable;
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
    public long tryInsert(@NotNull Resource resource, long amount) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            inserted += slot.tryInsert(resource, amount - inserted);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public long tryInsert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            inserted += slot.tryInsert(resource, tag, amount - inserted);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public boolean insertOne(@NotNull Resource resource) {
        for (Slot slot : this.slots) {
            if (slot.canInsertOne(resource)) return true;
        }
        return false;
    }

    @Override
    public boolean insertOne(@NotNull Resource resource, @Nullable CompoundTag tag) {
        for (Slot slot : this.slots) {
            if (slot.insertOne(resource, tag)) return true;
        }
        return false;
    }

    @Override
    public long insert(@NotNull Resource resource, long amount) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            inserted += slot.insert(resource, amount - inserted);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public long insert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            inserted += slot.insert(resource, tag, amount - inserted);
            if (inserted == amount) break;
        }
        return inserted;
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
            if (slot.contains(resource, tag)) return true;
        }
        return false;
    }

    @Override
    public boolean canExtract(@NotNull Resource resource, long amount) {
        for (Slot slot : this.slots) {
            if (slot.canExtract(resource, amount)) return true;
        }
        return false;
    }

    @Override
    public boolean canExtract(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        for (Slot slot : this.slots) {
            if (slot.canExtract(resource, tag, amount)) return true;
        }
        return false;
    }

    @Override
    public long tryExtract(@NotNull Resource resource, long amount) {
        long extracted = 0;
        for (Slot slot : this.slots) {
            extracted += slot.tryExtract(resource, amount - extracted);
            if (extracted == amount) break;
        }
        return extracted;
    }

    @Override
    public boolean extractOne(@NotNull Resource resource) {
        for (Slot slot : this.slots) {
            if (slot.extractOne(resource)) return true;
        }
        return false;
    }

    @Override
    public boolean extractOne(@NotNull Resource resource, @Nullable CompoundTag tag) {
        for (Slot slot : this.slots) {
            if (slot.extractOne(resource, tag)) return true;
        }
        return false;
    }

    @Override
    public long tryExtract(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        long extracted = 0;
        for (Slot slot : this.slots) {
            extracted += slot.tryExtract(resource, tag, amount - extracted);
            if (extracted == amount) break;
        }
        return extracted;
    }

    @Override
    public long extract(@NotNull Resource resource, long amount) {
        long extracted = 0;
        for (Slot slot : this.slots) {
            extracted += slot.extract(resource, amount - extracted);
            if (extracted == amount) break;
        }
        return extracted;
    }

    @Override
    public long extract(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        long extracted = 0;
        for (Slot slot : this.slots) {
            extracted += slot.extract(resource, tag, amount - extracted);
            if (extracted == amount) break;
        }
        return extracted;
    }

    @Override
    public long insert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            inserted += slot.insert(resource, tag, amount - inserted);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public long extract(@Nullable Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context) {
        long extracted = 0;
        for (Slot slot : this.slots) {
            extracted += slot.extract(resource, tag, amount - extracted, context);
            if (extracted == amount) break;
        }
        return extracted;
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
    public long getCapacityFor(int slot, @NotNull Resource resource) {
        return this.slots[slot].getCapacityFor(resource);
    }

    @Override
    public long getRealCapacity(int slot) {
        return this.slots[slot].getRealCapacity();
    }

    @Override
    public boolean isEmpty(int slot) {
        return this.slots[slot].isEmpty();
    }

    @Override
    public boolean isFull(int slot) {
        return this.slots[slot].isFull();
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
    public long tryInsert(int slot, @NotNull Resource resource, long amount) {
        return this.slots[slot].tryInsert(resource, amount);
    }

    @Override
    public long tryInsert(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        return this.slots[slot].tryInsert(resource, tag, amount);
    }

    @Override
    public boolean insertOne(int slot, @NotNull Resource resource) {
        return this.slots[slot].insertOne(resource);
    }

    @Override
    public boolean insertOne(int slot, @NotNull Resource resource, @Nullable CompoundTag tag) {
        return this.slots[slot].insertOne(resource, tag);
    }

    @Override
    public long insert(int slot, @NotNull Resource resource, long amount) {
        return this.slots[slot].insert(resource, amount);
    }

    @Override
    public long insert(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        return this.slots[slot].insert(resource, tag, amount);
    }

    @Override
    public boolean containsAny(int slot, @NotNull Resource resource) {
        return this.slots[slot].contains(resource);
    }

    @Override
    public boolean containsAny(int slot, @NotNull Resource resource, @Nullable CompoundTag tag) {
        return this.slots[slot].contains(resource, tag);
    }

    @Override
    public boolean canExtract(int slot, long amount) {
        return this.slots[slot].canExtract(amount);
    }

    @Override
    public boolean canExtract(int slot, @NotNull Resource resource, long amount) {
        return this.slots[slot].canExtract(resource, amount);
    }

    @Override
    public boolean canExtract(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        return this.slots[slot].canExtract(resource, tag, amount);
    }

    @Override
    public long tryExtract(int slot, long amount) {
        return this.slots[slot].tryExtract(amount);
    }

    @Override
    public long tryExtract(int slot, @Nullable Resource resource, long amount) {
        return this.slots[slot].tryExtract(resource, amount);
    }

    @Override
    public long tryExtract(int slot, @Nullable Resource resource, @Nullable CompoundTag tag, long amount) {
        return this.slots[slot].tryExtract(resource, tag, amount);
    }

    @Override
    public boolean extractOne(int slot) {
        return this.slots[slot].extractOne();
    }

    @Override
    public boolean extractOne(int slot, @Nullable Resource resource) {
        return this.slots[slot].extractOne(resource);
    }

    @Override
    public boolean extractOne(int slot, @Nullable Resource resource, @Nullable CompoundTag tag) {
        return this.slots[slot].extractOne(resource, tag);
    }

    @Override
    public long extract(int slot, long amount) {
        return this.slots[slot].extract(amount);
    }

    @Override
    public long extract(int slot, @Nullable Resource resource, long amount) {
        return this.slots[slot].extract(resource, amount);
    }

    @Override
    public long extract(int slot, @Nullable Resource resource, @Nullable CompoundTag tag, long amount) {
        return this.slots[slot].extract(resource, tag, amount);
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
        if (this.parent != null) this.parent.revertModification();
        this.modifications--;
    }

    @Override
    public void markModified() {
        if (this.parent != null) this.parent.markModified();
        this.modifications++;
    }

    @Override
    public void markModified(@Nullable TransactionContext context) {
        if (this.parent != null) this.parent.markModified(context);
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
