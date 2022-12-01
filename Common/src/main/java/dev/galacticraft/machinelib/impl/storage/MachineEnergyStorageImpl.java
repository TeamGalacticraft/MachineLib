package dev.galacticraft.machinelib.impl.storage;

import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;

public class MachineEnergyStorageImpl implements MachineEnergyStorage {
    private final long capacity;
    private final long maxInsert;
    private final long maxExtract;
    private long amount;

    public MachineEnergyStorageImpl(long capacity, long maxInsert, long maxExtract) {
        this.capacity = capacity;
        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
    }

    @Override
    public boolean supportsInsertion() {
        return true;
    }

    @Override
    public long insert(long amount, boolean simulate) {
        amount = Math.min(Math.min(this.maxInsert, amount), this.capacity - this.amount);
        this.amount += amount;
        return amount;
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }

    @Override
    public long extract(long amount, boolean simulate) {
        amount = Math.min(this.amount, Math.min(this.maxExtract, amount));
        this.amount -= amount;
        return amount;
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
    public void setAmount(long amount) {
        this.amount = Math.min(this.capacity, amount);
    }
}
