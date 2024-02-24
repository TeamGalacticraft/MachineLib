/*
 * Copyright (c) 2021-2024 Team Galacticraft
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
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public abstract class ResourceStorageImpl<Resource, Slot extends ResourceSlot<Resource>> extends BaseSlottedStorage<Resource, Slot> implements ResourceStorage<Resource, Slot>, TransactionContext.CloseCallback {
    private long modifications = 1;
    private final LongList transactions = new LongArrayList();
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
    public @NotNull ResourceFilter<Resource> getStrictFilter(int slot) {
        return this.slots[slot].getFilter();
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
    public void markModified(@Nullable TransactionContext transaction) {
        if (transaction != null) {
            while (this.transactions.size() <= transaction.nestingDepth()) {
                this.transactions.add(-1L);
            }

            if (this.transactions.getLong(transaction.nestingDepth()) == -1L) {
                this.transactions.set(transaction.nestingDepth(), this.modifications);
                transaction.addCloseCallback(this);
            }

            this.modifications++;
        } else {
            this.markModified();
        }
    }

    @Override
    public void onClose(TransactionContext transaction, TransactionContext.Result result) {
        if (result.wasAborted()) {
            this.modifications = this.transactions.removeLong(transaction.nestingDepth());
        } else if (transaction.nestingDepth() > 0) {
            long snap = this.transactions.removeLong(transaction.nestingDepth());
            if (this.transactions.getLong(transaction.nestingDepth() - 1) == -1) {
                this.transactions.set(transaction.nestingDepth() - 1, snap);
                transaction.getOpenTransaction(transaction.nestingDepth() - 1).addCloseCallback(this);
            }
        } else {
            this.transactions.clear();
            transaction.addOuterCloseCallback((res) -> {
                assert res.wasCommitted();
                if (this.listener != null) this.listener.run();
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

    @Override
    public @Nullable MenuSyncHandler createSyncHandler() {
        return new ResourceStorageSyncHandler<>(this);
    }
}
