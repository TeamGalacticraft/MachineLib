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
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.io.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.exposed.ExposedEnergyStorage;
import dev.galacticraft.machinelib.impl.menu.sync.MachineEnergyStorageSyncHandler;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.nbt.LongTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

@ApiStatus.Internal
public final class MachineEnergyStorageImpl extends SnapshotParticipant<Long> implements MachineEnergyStorage {
    public final long capacity;
    private final long maxInput;
    private final long maxOutput;
    private final boolean insert;
    private final boolean extract;

    public long amount = 0;
    private Runnable listener;

    public MachineEnergyStorageImpl(long capacity, long maxInput, long maxOutput, boolean insert, boolean extract) {
        this.capacity = capacity;
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
        this.insert = insert;
        this.extract = extract;
    }

    @Override
    protected Long createSnapshot() {
        return this.amount;
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        this.amount = snapshot;
    }

    @Override
    public boolean supportsInsertion() {
        return this.maxInput > 0;
    }

    @Override
    public boolean canExtract(long amount) {
        return this.amount >= amount && amount <= this.maxOutput;
    }

    @Override
    public boolean canInsert(long amount) {
        return this.amount <= amount && amount <= this.maxInput;
    }

    @Override
    public long tryExtract(long amount) {
        return Math.min(this.maxOutput, Math.min(this.amount, amount));
    }

    @Override
    public long tryInsert(long amount) {
        return Math.min(this.maxInput, Math.min(this.amount + amount, this.capacity) - this.amount);
    }

    @Override
    public long extract(long amount) {
        long extracted = this.tryExtract(amount);

        if (extracted > 0) {
            this.amount -= extracted;
            this.markModified();
            return extracted;
        }
        return 0;
    }

    @Override
    public long insert(long amount) {
        long inserted = this.tryInsert(amount);

        if (inserted > 0) {
            this.amount += inserted;
            this.markModified();
            return inserted;
        }
        return 0;
    }

    @Override
    public boolean extractExact(long amount) {
        if (this.canExtract(amount)) {
            this.amount -= amount;
            this.markModified();
            return true;
        }
        return false;
    }

    @Override
    public boolean insertExact(long amount) {
        if (this.canInsert(amount)) {
            this.amount += amount;
            this.markModified();
            return true;
        }
        return false;
    }

    @Override
    public long insert(long amount, @NotNull TransactionContext transaction) {
        long inserted = this.tryInsert(amount);

        if (inserted > 0) {
            this.updateSnapshots(transaction);
            this.amount += inserted;
            return inserted;
        }
        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return this.maxOutput > 0;
    }

    @Override
    public long extract(long amount, @NotNull TransactionContext transaction) {
        long extracted = this.tryExtract(amount);

        if (extracted > 0) {
            this.updateSnapshots(transaction);
            this.amount -= extracted;
            return extracted;
        }
        return 0;
    }

    @Override
    public long getAmount() {
        return this.amount;
    }

    @Override
    public long getCapacity() {
        return this.capacity;
    }

    @Override
    public boolean isFull() {
        return this.amount == this.capacity;
    }

    @Override
    public boolean isEmpty() {
        return this.amount == 0;
    }

    @Override
    public void setEnergy(long amount, @Nullable TransactionContext context) {
        if (context != null) this.updateSnapshots(context);
        this.setEnergy(amount);
    }

    @Override
    public void setEnergy(long amount) {
        this.amount = amount;
    }

    @Override
    public @Nullable EnergyStorage getExposedStorage(@NotNull ResourceFlow flow) {
        switch (flow) {
            case INPUT -> {
                if (this.insert) {
                    return ExposedEnergyStorage.create(this, true, false);
                }
                return null;
            }
            case OUTPUT -> {
                if (this.extract) {
                    return ExposedEnergyStorage.create(this, true, false);
                }
                return null;
            }
            case BOTH -> {
                if (this.insert) {
                    if (this.extract) {
                        return this;
                    } else {
                        return ExposedEnergyStorage.create(this, true, false);
                    }
                } else if (this.extract) {
                    return ExposedEnergyStorage.create(this, false, true);
                }
                return null;
            }
        }
        return null;
    }

    @Override
    public boolean canExposedInsert() {
        return this.insert;
    }

    @Override
    public boolean canExposedExtract() {
        return this.extract;
    }

    @Override
    public void setListener(Runnable listener) {
        this.listener = listener;
    }

    @Override
    public @NotNull LongTag createTag() {
        return LongTag.valueOf(this.amount);
    }

    @Override
    public void readTag(@NotNull LongTag tag) {
        this.amount = tag.getAsLong();
    }

    @Override
    public void writePacket(@NotNull FriendlyByteBuf buf) {
        buf.writeLong(this.amount);
    }

    @Override
    public void readPacket(@NotNull FriendlyByteBuf buf) {
        this.amount = buf.readLong();
    }

    @Override
    protected void onFinalCommit() {
        super.onFinalCommit();
        this.markModified();
    }

    @Override
    public @NotNull MenuSyncHandler createSyncHandler() {
        return new MachineEnergyStorageSyncHandler(this);
    }

    @Override
    public long getModifications() {
        return this.amount;
    }

    private void markModified() {
        if (this.listener != null) this.listener.run();
    }
}
