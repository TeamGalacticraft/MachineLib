/*
 * Copyright (c) 2021-2022 Team Galacticraft
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
import dev.galacticraft.api.machine.storage.io.ExposedEnergyStorage;
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.screen.StorageSyncHandler;
import dev.galacticraft.impl.machine.ModCount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtLong;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;
import team.reborn.energy.api.EnergyStorage;

public class MachineEnergyStorageImpl extends SnapshotParticipant<Long> implements MachineEnergyStorage {
    public final long capacity;
    private final long maxInput;
    private final long maxOutput;
    private final ModCount modCount = new ModCount();
    private final ExposedEnergyStorage view = new ExposedEnergyStorage(this, false, false);

    public long amount = 0;

    public MachineEnergyStorageImpl(long capacity, long maxInput, long maxOutput) {
        this.capacity = capacity;
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
    }

    @Override
    public @NotNull StorageSyncHandler createSyncHandler() {
        return new StorageSyncHandler() {
            private int modCount = -1;

            @Override
            public boolean needsSyncing() {
                return MachineEnergyStorageImpl.this.modCount.getModCount() != this.modCount;
            }

            @Override
            public void sync(PacketByteBuf buf) {
                this.modCount = MachineEnergyStorageImpl.this.modCount.getModCount();
                buf.writeLong(MachineEnergyStorageImpl.this.amount);
            }

            @Override
            public void read(PacketByteBuf buf) {
                MachineEnergyStorageImpl.this.amount = buf.readLong();
            }
        };
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
    public long extract(long maxAmount, TransactionContext transaction) {
        long extracted = Math.min(this.amount, Math.min(maxAmount, this.maxOutput));

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
        assert !Transaction.isOpen();
        this.modCount.incrementUnsafe();
        this.amount = amount;
    }

    @Override
    public @NotNull EnergyStorage getExposedStorage(@NotNull ResourceFlow flow) {
        return new ExposedEnergyStorage(this, flow.canFlowIn(ResourceFlow.INPUT), flow.canFlowIn(ResourceFlow.OUTPUT));
    }

    @Override
    public @NotNull EnergyStorage view() {
        return this.view;
    }

    @Override
    public @NotNull NbtElement writeNbt() {
        return NbtLong.of(this.amount);
    }

    @Override
    public void readNbt(@NotNull NbtElement nbt) {
        if (nbt.getType() == NbtElement.LONG_TYPE) {
            this.amount = ((NbtLong)nbt).longValue();
        }
    }
}
