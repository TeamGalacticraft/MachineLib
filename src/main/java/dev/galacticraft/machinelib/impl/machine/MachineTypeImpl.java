/*
 * Copyright (c) 2021-2024 Team Galacticraft
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
