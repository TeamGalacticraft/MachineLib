/*
 * Copyright (c) 2021-2023 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
