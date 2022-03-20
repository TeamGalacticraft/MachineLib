/*
 * Copyright (c) 2019-2022 Team Galacticraft
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

package dev.galacticraft.impl.machine.storage;

import dev.galacticraft.api.machine.storage.MachineEnergyStorage;
import dev.galacticraft.api.machine.storage.io.ExposedCapacitor;
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.screen.StorageSyncHandler;
import dev.galacticraft.impl.machine.Constant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;

public class MachineEnergyStorageImpl extends SnapshotParticipant<Long> implements MachineEnergyStorage {
    public final long capacity;
    private final long maxInput;
    private final long maxOutput;
    public long amount = 0;
    private ExposedCapacitor view = new ExposedCapacitor(this, false, false);

    public MachineEnergyStorageImpl(long capacity, long maxInput, long maxOutput) {
        this.capacity = capacity;
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
    }

    @Override
    public @NotNull StorageSyncHandler createSyncHandler() {
        return new StorageSyncHandler() {
            @Override
            public boolean needsSyncing() {
                return false;
            }

            @Override
            public void sync(PacketByteBuf buf) {

            }

            @Override
            public void read(PacketByteBuf buf) {

            }
        }; //todo
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
    public long insert(long maxAmount, TransactionContext transaction) {
        long inserted = Math.min(Math.min(maxAmount, maxInput), capacity - amount);

        if (inserted > 0) {
            this.updateSnapshots(transaction);
            amount += inserted;
            return inserted;
        }

        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return this.maxOutput > 0;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        long extracted = Math.min(this.amount, Math.min(maxAmount, this.maxOutput));

        if (extracted > 0) {
            updateSnapshots(transaction);
            amount -= extracted;
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
    public void setEnergy(long amount, TransactionContext context) {
        this.updateSnapshots(context);
        this.amount = amount;
    }

    @Override
    public void setEnergyUnsafe(long amount) {
        assert !Transaction.isOpen();
        this.amount = amount;
    }

    @Override
    public @NotNull ExposedCapacitor getExposedStorage(@NotNull ResourceFlow flow) {
        return new ExposedCapacitor(this, flow.canFlowIn(ResourceFlow.INPUT), flow.canFlowIn(ResourceFlow.OUTPUT));
    }

    @Override
    public @NotNull ExposedCapacitor view() {
        return this.view;
    }

    @Override
    public void writeNbt(@NotNull NbtCompound nbt) {
        nbt.putLong(Constant.Nbt.AMOUNT, this.amount);
    }

    @Override
    public void readNbt(@NotNull NbtCompound nbt) {
        this.amount = nbt.getLong(Constant.Nbt.AMOUNT);
    }
}
