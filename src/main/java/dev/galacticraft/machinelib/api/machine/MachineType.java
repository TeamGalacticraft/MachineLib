package dev.galacticraft.machinelib.api.machine;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.impl.machine.MachineTypeImpl;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface MachineType<Machine extends MachineBlockEntity, Menu extends MachineMenu<Machine>> {
    @Contract(value = "_, _, _, _, _ -> new", pure = true)
    static <Machine extends MachineBlockEntity, Menu extends MachineMenu<Machine>> @NotNull MachineType<Machine, Menu> create(@NotNull Block block, @NotNull BlockEntityType<Machine> blockEntityType, @NotNull MenuType<Menu> menuType, @NotNull Supplier<MachineEnergyStorage> energySupplier, @NotNull Supplier<MachineItemStorage> itemSupplier) {
        return new MachineTypeImpl<>(block, blockEntityType, menuType, energySupplier, itemSupplier, MachineTypeImpl.DEFAULT_FLUID_STORAGE);
    }

    @Contract(value = "_, _, _, _, _, _ -> new", pure = true)
    static <Machine extends MachineBlockEntity, Menu extends MachineMenu<Machine>> @NotNull MachineType<Machine, Menu> create(@NotNull Block block, @NotNull BlockEntityType<Machine> blockEntityType, @NotNull MenuType<Menu> menuType, @NotNull Supplier<MachineEnergyStorage> energySupplier, @NotNull Supplier<MachineItemStorage> itemSupplier, @NotNull Supplier<MachineFluidStorage> fluidSupplier) {
        return new MachineTypeImpl<>(block, blockEntityType, menuType, energySupplier, itemSupplier, fluidSupplier);
    }

    @NotNull MachineEnergyStorage createEnergyStorage();
    @NotNull MachineItemStorage createItemStorage();
    @NotNull MachineFluidStorage createFluidStorage();

    @NotNull Block getBlock();
    @NotNull MenuType<Menu> getMenuType();
    @NotNull BlockEntityType<Machine> getBlockEntityType();
}
