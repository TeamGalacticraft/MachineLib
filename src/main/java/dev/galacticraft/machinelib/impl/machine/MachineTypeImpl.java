package dev.galacticraft.machinelib.impl.machine;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class MachineTypeImpl<Machine extends MachineBlockEntity, Menu extends MachineMenu<Machine>> implements MachineType<Machine, Menu> {
    public static final Supplier<MachineFluidStorage> DEFAULT_FLUID_STORAGE = MachineFluidStorage::empty;
    public static final Supplier<MachineItemStorage> DEFAULT_ITEM_STORAGE = MachineItemStorage::empty;
    public static final Supplier<MachineEnergyStorage> DEFAULT_ENERGY_STORAGE = MachineEnergyStorage::empty;

    private final @NotNull Block block;
    private final @NotNull BlockEntityType<Machine> blockEntityType;
    private final @NotNull MenuType<Menu> menuType;
    private final @NotNull Supplier<MachineEnergyStorage> energySupplier;
    private final @NotNull Supplier<MachineItemStorage> itemSupplier;
    private final @NotNull Supplier<MachineFluidStorage> fluidSupplier;

    public MachineTypeImpl(@NotNull Block block, @NotNull BlockEntityType<Machine> blockEntityType, @NotNull MenuType<Menu> menuType, @NotNull Supplier<MachineEnergyStorage> energySupplier, @NotNull Supplier<MachineItemStorage> itemSupplier, @NotNull Supplier<MachineFluidStorage> fluidSupplier) {
        this.block = block;
        this.blockEntityType = blockEntityType;
        this.menuType = menuType;
        this.energySupplier = energySupplier;
        this.itemSupplier = itemSupplier;
        this.fluidSupplier = fluidSupplier;
    }

    @Override
    public @NotNull MachineEnergyStorage createEnergyStorage() {
        return this.energySupplier.get();
    }

    @Override
    public @NotNull MachineItemStorage createItemStorage() {
        return this.itemSupplier.get();
    }

    @Override
    public @NotNull MachineFluidStorage createFluidStorage() {
        return this.fluidSupplier.get();
    }

    @Override
    public @NotNull Block getBlock() {
        return this.block;
    }

    @Override
    public @NotNull MenuType<Menu> getMenuType() {
        return this.menuType;
    }

    @Override
    public @NotNull BlockEntityType<Machine> getBlockEntityType() {
        return this.blockEntityType;
    }
}
