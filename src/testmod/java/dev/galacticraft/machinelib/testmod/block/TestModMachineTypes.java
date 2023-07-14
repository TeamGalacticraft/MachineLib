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

package dev.galacticraft.machinelib.testmod.block;

import com.google.common.collect.ImmutableList;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.io.InputType;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.testmod.block.entity.GeneratorBlockEntity;
import dev.galacticraft.machinelib.testmod.block.entity.MelterBlockEntity;
import dev.galacticraft.machinelib.testmod.block.entity.MixerBlockEntity;
import dev.galacticraft.machinelib.testmod.block.entity.TestModBlockEntityTypes;
import dev.galacticraft.machinelib.testmod.menu.TestModMenuTypes;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

public class TestModMachineTypes {
    public static final MachineType<GeneratorBlockEntity, MachineMenu<GeneratorBlockEntity>> GENERATOR = MachineType.create(
            TestModBlocks.GENERATOR,
            TestModBlockEntityTypes.GENERATOR,
            TestModMenuTypes.GENERATOR,
            ImmutableList.of(MachineStatuses.ACTIVE, MachineStatuses.IDLE, MachineStatuses.OUTPUT_FULL),
            () -> MachineEnergyStorage.create(30000, 300, 300, false, true),
            MachineItemStorage.of(
                    ItemResourceSlot.builder(InputType.TRANSFER)
                            .pos(8, 62)
                            .filter(ResourceFilters.CAN_INSERT_ENERGY)
                            .strictFilter(ResourceFilters.CAN_INSERT_ENERGY_STRICT)
                            .capacity(32),
                    ItemResourceSlot.builder(InputType.INPUT)
                            .pos(80, 49)
                            .filter((item, tag) -> FuelRegistry.INSTANCE.get(item) > 0)
            ),
            MachineFluidStorage::empty
    );

    public static final MachineType<MixerBlockEntity, MachineMenu<MixerBlockEntity>> MIXER = MachineType.create(
            TestModBlocks.MIXER,
            TestModBlockEntityTypes.MIXER,
            TestModMenuTypes.MIXER,
            ImmutableList.of(MachineStatuses.IDLE), //todo
            () -> MachineEnergyStorage.create(30000, 300, 300, true, false),
            MachineItemStorage.of(
                    ItemResourceSlot.builder(InputType.TRANSFER)
                            .pos(8, 8)
                            .filter(ResourceFilters.CAN_EXTRACT_ENERGY)
                            .strictFilter(ResourceFilters.CAN_EXTRACT_ENERGY_STRICT)
                            .capacity(32),
                    ItemResourceSlot.builder(InputType.TRANSFER)
                            .pos(48, 8)
                            .strictFilter(ResourceFilters.canExtractFluidStrict(Fluids.WATER)),
                    ItemResourceSlot.builder(InputType.TRANSFER)
                            .pos(70, 8)
                            .strictFilter(ResourceFilters.canExtractFluidStrict(Fluids.LAVA)),
                    ItemResourceSlot.builder(InputType.OUTPUT)
                            .pos(147, 43)
                            .filter(ResourceFilters.ofResource(Items.OBSIDIAN))
            ),
            MachineFluidStorage.of(
                    FluidResourceSlot.builder(InputType.INPUT)
                            .pos(48, 30)
                            .capacity(FluidConstants.BUCKET * 16)
                            .filter(ResourceFilters.ofResource(Fluids.WATER)),
                    FluidResourceSlot.builder(InputType.INPUT)
                            .pos(70, 30)
                            .capacity(FluidConstants.BUCKET * 16)
                            .filter(ResourceFilters.ofResource(Fluids.LAVA))
            )
    );

    public static final MachineType<MelterBlockEntity, MachineMenu<MelterBlockEntity>> MELTER = MachineType.create(
            TestModBlocks.MELTER,
            TestModBlockEntityTypes.MELTER,
            TestModMenuTypes.MELTER,
            ImmutableList.of(MachineStatuses.ACTIVE, MachineStatuses.NOT_ENOUGH_ENERGY, MachineStatuses.OUTPUT_FULL, MachineStatuses.IDLE),
            () -> MachineEnergyStorage.create(30000, 300, 300, true, false),
            MachineItemStorage.of(
                    ItemResourceSlot.builder(InputType.TRANSFER)
                            .pos(8, 8)
                            .filter(ResourceFilters.CAN_EXTRACT_ENERGY)
                            .strictFilter(ResourceFilters.CAN_EXTRACT_ENERGY_STRICT)
                            .capacity(32),
                    ItemResourceSlot.builder(InputType.INPUT)
                            .pos(59, 42)
                            .filter(ResourceFilters.ofResource(Items.COBBLESTONE)),
                    ItemResourceSlot.builder(InputType.TRANSFER)
                            .pos(152, 62)
                            .strictFilter(ResourceFilters.canInsertFluidStrict(Fluids.LAVA))
            ),
            MachineFluidStorage.of(FluidResourceSlot.builder(InputType.OUTPUT)
                    .pos(152, 8)
                    .capacity(FluidConstants.BUCKET * 16)
                    .filter(ResourceFilters.ofResource(Fluids.LAVA))
            )
    );

    public static void initialize() {
    }
}
