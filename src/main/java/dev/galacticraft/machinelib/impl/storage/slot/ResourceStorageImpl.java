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
import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import dev.galacticraft.machinelib.api.storage.ResourceFilter;
import dev.galacticraft.machinelib.api.storage.ResourceStorage;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.impl.menu.sync.ResourceStorageSyncHandler;
import dev.galacticraft.machinelib.impl.storage.AbstractSlotProvider;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public abstract class ResourceStorageImpl<Resource, Slot extends ResourceSlot<Resource>> extends AbstractSlotProvider<Resource, Slot> implements ResourceStorage<Resource, Slot> {
    private long modifications = 0;
    private TransactionContext cachedTransaction = null;
    private Runnable listener;

    public ResourceStorageImpl(@NotNull Slot @NotNull [] slots) {
        super(slots);
        for (Slot slot : slots) {
            slot._setParent(this);
        }
    }

    @Override
    public void setListener(Runnable listener) {
        this.listener = listener;
    }

    @Override
    public int size() {
        return this.slots.length;
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
    public @NotNull ResourceFilter<Resource> getStrictFilter(int slot) {
        return this.slots[slot].getStrictFilter();
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
    public boolean canInsert(int slot, @NotNull Resource resource) {
        return this.slots[slot].canInsert(resource);
    }

    @Override
    public boolean canInsert(int slot, @NotNull Resource resource, @Nullable CompoundTag tag) {
        return this.slots[slot].canInsert(resource, tag);
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
    public void markModified() {
        this.modifications++;
        if (this.listener != null) this.listener.run();
    }

    @Override
    public void markModified(@Nullable TransactionContext context) {
        if (context != null) {
            this.modifications++;
            context.addCloseCallback((context1, result) -> {
                if (result.wasAborted()) {
                    this.modifications--;
                } else {
                    if (this.listener != null) {
                        TransactionContext outer = context1.nestingDepth() != 0 ? context1.getOpenTransaction(0) : context1;
                        if (this.cachedTransaction != outer) {
                            this.cachedTransaction = outer;
                            context.addOuterCloseCallback((result1) -> {
                                if (result1.wasCommitted()) this.listener.run();
                            });
                        }
                    }
                }
            });
        } else {
            this.markModified();
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

    @Override
    public @Nullable MenuSyncHandler createSyncHandler() {
        return new ResourceStorageSyncHandler<>(this);
    }
}
