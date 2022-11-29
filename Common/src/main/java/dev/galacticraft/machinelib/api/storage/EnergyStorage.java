package dev.galacticraft.machinelib.api.storage;

public interface EnergyStorage {
    boolean supportsInsertion();

    long insert(long amount, boolean simulate);

    boolean supportsExtraction();

    long extract(long amount, boolean simulate);

    long getAmount();

    long getCapacity();
}
