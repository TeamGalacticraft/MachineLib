package dev.galacticraft.machinelib.impl.storage;

import dev.galacticraft.machinelib.api.storage.EnergyStorage;

public class EnergyStorageImpl implements EnergyStorage {
    private final long capacity;
    private long amount;

    public EnergyStorageImpl(long capacity) {
        this.capacity = capacity;
    }

    @Override
    public boolean supportsInsertion() {
        return true;
    }

    @Override
    public long insert(long amount, boolean simulate) {
        amount = Math.min(amount, this.capacity - this.amount);
        this.amount += amount;
        return amount;
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }

    @Override
    public long extract(long amount, boolean simulate) {
        amount = Math.min(this.amount, amount);
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
}
