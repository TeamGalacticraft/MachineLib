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

import com.google.common.collect.Iterators;
import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import dev.galacticraft.machinelib.api.storage.ResourceStorage;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.impl.menu.sync.ResourceStorageSyncHandler;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public abstract class ResourceStorageImpl<Resource, Slot extends ResourceSlot<Resource>> extends AbstractSlottedStorage<Resource, Slot> implements ResourceStorage<Resource, Slot> {
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
