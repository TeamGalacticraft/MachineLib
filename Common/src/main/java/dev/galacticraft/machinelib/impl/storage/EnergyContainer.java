package dev.galacticraft.machinelib.impl.storage;

public interface EnergyContainer {
    default boolean supportsInsertion() {
        return true;
    }

    long insert(long maxAmount, boolean simulate);

    default boolean supportsExtraction() {
        return true;
    }

    long extract(long maxAmount, boolean simulate);

    long getAmount();

    long getCapacity();
}
