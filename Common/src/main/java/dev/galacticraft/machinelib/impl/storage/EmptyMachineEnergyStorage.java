package dev.galacticraft.machinelib.impl.storage;

import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;

public final class EmptyMachineEnergyStorage implements MachineEnergyStorage {
    public static final MachineEnergyStorage INSTANCE = new EmptyMachineEnergyStorage();

    private EmptyMachineEnergyStorage() {}

    @Override
    public boolean supportsInsertion() {
        return false;
    }

    @Override
    public long insert(long amount, boolean simulate) {
        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public long extract(long amount, boolean simulate) {
        return 0;
    }

    @Override
    public long getAmount() {
        return 0;
    }

    @Override
    public long getCapacity() {
        return 0;
    }

    @Override
    public void setAmount(long amount) {
    }
}
