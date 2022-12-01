package dev.galacticraft.machinelib.impl.fabric.storage;

import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import team.reborn.energy.api.EnergyStorage;

public class FabricExposedEnergyStorage extends SnapshotParticipant<Long> implements EnergyStorage {
    private final MachineEnergyStorage storage;
    private final long maxInsertion;
    private final long maxExtraction;

    public FabricExposedEnergyStorage(MachineEnergyStorage storage, long maxInsertion, long maxExtraction) {
        this.storage = storage;
        this.maxInsertion = maxInsertion;
        this.maxExtraction = maxExtraction;
    }

    @Override
    public boolean supportsInsertion() {
        return this.storage.supportsInsertion() && this.maxInsertion > 0;
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        maxAmount = Math.min(maxAmount, this.maxInsertion);
        this.createSnapshot();
        return this.storage.insert(maxAmount, false);
    }

    @Override
    public boolean supportsExtraction() {
        return this.storage.supportsExtraction() && this.maxExtraction > 0;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        maxAmount = Math.min(maxAmount, this.maxExtraction);
        this.createSnapshot();
        return this.storage.extract(maxAmount, false);
    }

    @Override
    public long getAmount() {
        return this.storage.getAmount();
    }

    @Override
    public long getCapacity() {
        return this.storage.getCapacity();
    }

    @Override
    protected Long createSnapshot() {
        return this.storage.getAmount();
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        this.storage.setAmount(snapshot);
    }
}
