package dev.galacticraft.machinelib.api.storage;

import dev.galacticraft.machinelib.impl.storage.EmptyMachineEnergyStorage;
import dev.galacticraft.machinelib.impl.storage.MachineEnergyStorageImpl;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface MachineEnergyStorage extends EnergyStorage {
    void setAmount(long amount);

    @Contract(pure = true)
    static MachineEnergyStorage empty() {
        return EmptyMachineEnergyStorage.INSTANCE;
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    static @NotNull MachineEnergyStorage create(long maxCapacity, long maxInsert, long maxExtract) {
        if (maxCapacity == 0) return empty();
        if (maxCapacity < 1) throw new IllegalArgumentException("maxCapacity must be positive!");
        if (maxInsert < 1) throw new IllegalArgumentException("maxInsert must be greater than zero!");
        if (maxExtract < 1) throw new IllegalArgumentException("maxExtract must be greater than zero!");
        return new MachineEnergyStorageImpl(maxCapacity, maxInsert, maxExtract);
    }

    @Contract(value = "_ -> new", pure = true)
    static @NotNull MachineEnergyStorage create(long maxCapacity) {
        return create(maxCapacity, Long.MAX_VALUE, Long.MAX_VALUE);
    }
}
