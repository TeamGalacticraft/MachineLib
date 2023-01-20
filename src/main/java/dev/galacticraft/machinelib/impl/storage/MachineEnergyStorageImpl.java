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

import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.io.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.cache.ModCount;
import dev.galacticraft.machinelib.api.transfer.exposed.ExposedEnergyStorage;
import dev.galacticraft.machinelib.api.util.GenericApiUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.nbt.LongTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import team.reborn.energy.api.EnergyStorage;

@ApiStatus.Internal
public final class MachineEnergyStorageImpl extends SnapshotParticipant<Long> implements MachineEnergyStorage {
    public final long capacity;
    private final long maxInput;
    private final long maxOutput;
    private final boolean insert;
    private final boolean extract;
    private final ModCount modCount = ModCount.root();

    public long amount = 0;

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
    public long insert(long amount) {
        long inserted = Math.min(Math.min(amount, maxInput), capacity - amount);

        if (inserted > 0) {
            this.modCount.increment();
            this.amount += inserted;
            return inserted;
        }

        return 0;
    }

    @Override
    public long insert(long amount, @NotNull TransactionContext transaction) {
        long inserted = Math.min(Math.min(amount, maxInput), capacity - this.amount);

        if (inserted > 0) {
            this.updateSnapshots(transaction);
            this.modCount.increment(transaction);
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
    public long extract(long amount) {
        long extracted = Math.min(this.amount, Math.min(amount, this.maxOutput));

        if (extracted > 0) {
            this.modCount.increment();
            this.amount -= extracted;
            return extracted;
        }

        return 0;
    }

    @Override
    public long extract(long amount, @NotNull TransactionContext transaction) {
        long extracted = Math.min(this.amount, Math.min(amount, this.maxOutput));

        if (extracted > 0) {
            this.updateSnapshots(transaction);
            this.modCount.increment(transaction);
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
    public void setEnergy(long amount, @NotNull TransactionContext context) {
        this.updateSnapshots(context);
        this.modCount.increment(context);
        this.amount = amount;
    }

    @Override
    public void setEnergyUnsafe(long amount) {
        GenericApiUtil.noTransaction();
        this.modCount.increment();
        this.amount = amount;
    }

    @Override
    public @NotNull EnergyStorage getExposedStorage(@NotNull ResourceFlow flow) {
        return ExposedEnergyStorage.create(this, this.insert && flow.canFlowIn(ResourceFlow.INPUT), this.extract && flow.canFlowIn(ResourceFlow.OUTPUT));
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
}
