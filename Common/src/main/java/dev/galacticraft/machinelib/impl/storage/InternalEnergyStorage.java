package dev.galacticraft.machinelib.impl.storage;

import dev.galacticraft.machinelib.api.storage.EnergyStorage;

public interface InternalEnergyStorage extends EnergyStorage {
    void setAmount(long amount);
}
