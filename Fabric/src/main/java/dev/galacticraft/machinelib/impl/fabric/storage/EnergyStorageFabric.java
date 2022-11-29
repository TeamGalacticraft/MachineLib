package dev.galacticraft.machinelib.impl.fabric.storage;

import dev.galacticraft.machinelib.api.storage.EnergyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

public class EnergyStorageFabric implements EnergyStorage {
    private final team.reborn.energy.api.EnergyStorage storage;

    public EnergyStorageFabric(team.reborn.energy.api.EnergyStorage storage) {
        this.storage = storage;
    }

    @Override
    public boolean supportsInsertion() {
        return this.storage.supportsInsertion();
    }

    @Override
    public long insert(long amount, boolean simulate) {
        long inserted;
        try (Transaction transaction = Transaction.openOuter()) {
            inserted = this.storage.insert(amount, transaction);
            if (!simulate) transaction.commit();
        }
        return inserted;
    }

    @Override
    public boolean supportsExtraction() {
        return this.storage.supportsExtraction();
    }

    @Override
    public long extract(long amount, boolean simulate) {
        long extracted;
        try (Transaction transaction = Transaction.openOuter()) {
            extracted = this.storage.extract(amount, transaction);
            if (!simulate) transaction.commit();
        }
        return extracted;
    }

    @Override
    public long getAmount() {
        return this.storage.getAmount();
    }

    @Override
    public long getCapacity() {
        return this.storage.getCapacity();
    }
}
