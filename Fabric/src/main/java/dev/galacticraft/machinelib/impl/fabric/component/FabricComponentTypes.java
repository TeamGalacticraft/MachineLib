package dev.galacticraft.machinelib.impl.fabric.component;

import dev.galacticraft.machinelib.api.component.ComponentType;
import dev.galacticraft.machinelib.api.component.ComponentTypes;
import dev.galacticraft.machinelib.api.storage.EnergyStorage;
import dev.galacticraft.machinelib.api.storage.FluidStorage;
import dev.galacticraft.machinelib.api.storage.ItemStorage;
import dev.galacticraft.machinelib.impl.fabric.storage.EnergyStorageFabric;
import dev.galacticraft.machinelib.impl.fabric.storage.FluidStorageFabric;
import dev.galacticraft.machinelib.impl.fabric.storage.ItemStorageFabric;

public class FabricComponentTypes implements ComponentTypes {
    private static final ComponentType<EnergyStorage> ENERGY_STORAGE = new FabricComponentType<>(
            team.reborn.energy.api.EnergyStorage.SIDED,
            team.reborn.energy.api.EnergyStorage.ITEM,
            EnergyStorageFabric::new);

    private static final ComponentType<ItemStorage> ITEM_STORAGE = new FabricComponentType<>(
            net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED,
            null,
            ItemStorageFabric::new);

    private static final ComponentType<FluidStorage> FLUID_STORAGE = new FabricComponentType<>(
            net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.SIDED,
            net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.ITEM,
            FluidStorageFabric::new);

    @Override
    public ComponentType<EnergyStorage> getEnergyStorage() {
        return ENERGY_STORAGE;
    }

    @Override
    public ComponentType<ItemStorage> getItemStorage() {
        return ITEM_STORAGE;
    }

    @Override
    public ComponentType<FluidStorage> getFluidStorage() {
        return FLUID_STORAGE;
    }
}
