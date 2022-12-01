package dev.galacticraft.machinelib.api.component;

import dev.galacticraft.machinelib.api.storage.EnergyStorage;
import dev.galacticraft.machinelib.api.storage.FluidStorage;
import dev.galacticraft.machinelib.api.storage.ItemStorage;
import dev.galacticraft.machinelib.impl.platform.Services;

public interface ComponentTypes {
    static ComponentType<EnergyStorage> energyStorage() {
        return Services.COMPONENT_TYPES.getEnergyStorage();
    }

    static ComponentType<ItemStorage> itemStorage() {
        return Services.COMPONENT_TYPES.getItemStorage();
    }

    static ComponentType<FluidStorage> fluidStorage() {
        return Services.COMPONENT_TYPES.getFluidStorage();
    }

    ComponentType<EnergyStorage> getEnergyStorage();
    ComponentType<ItemStorage> getItemStorage();
    ComponentType<FluidStorage> getFluidStorage();
}
