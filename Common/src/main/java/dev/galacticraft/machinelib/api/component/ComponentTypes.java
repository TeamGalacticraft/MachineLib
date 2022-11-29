package dev.galacticraft.machinelib.api.component;

import dev.galacticraft.machinelib.api.storage.EnergyStorage;
import dev.galacticraft.machinelib.api.storage.ItemStorage;
import dev.galacticraft.machinelib.impl.platform.Services;

public interface ComponentTypes {
    static ComponentType<EnergyStorage> energyStorage() {
        return Services.INSTANCE.getEnergyStorage();
    }

    static ComponentType<ItemStorage> itemStorage() {
        return Services.INSTANCE.getItemStorage();
    }

    ComponentType<EnergyStorage> getEnergyStorage();
    ComponentType<ItemStorage> getItemStorage();
}
