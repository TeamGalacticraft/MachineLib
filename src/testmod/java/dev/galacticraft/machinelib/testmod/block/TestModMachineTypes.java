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
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.testmod.block.entity.GeneratorBlockEntity;
import dev.galacticraft.machinelib.testmod.block.entity.MelterBlockEntity;
import dev.galacticraft.machinelib.testmod.block.entity.MixerBlockEntity;
import dev.galacticraft.machinelib.testmod.block.entity.TestModBlockEntityTypes;
import dev.galacticraft.machinelib.testmod.menu.TestModMenuTypes;
import dev.galacticraft.machinelib.testmod.slot.TestModSlotGroupTypes;
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
            () -> MachineEnergyStorage.of(30000, 300, 300, false, true),
            MachineItemStorage.builder()
                    .single(TestModSlotGroupTypes.CHARGE, ItemResourceSlot.builder()
                            .pos(8, 62)
                            .filter(ResourceFilters.CAN_INSERT_ENERGY)
                            .strictFilter(ResourceFilters.CAN_INSERT_ENERGY_STRICT)
                            .capacity(32)
                            ::build
                    )
                    .single(TestModSlotGroupTypes.SOLID_FUEL, ItemResourceSlot.builder()
                            .pos(80, 49)
                            .filter((item, tag) -> FuelRegistry.INSTANCE.get(item) > 0)
                            ::build
                    )
                    ::build,
            MachineFluidStorage::empty
    );

    public static final MachineType<MixerBlockEntity, MachineMenu<MixerBlockEntity>> MIXER = MachineType.create(
            TestModBlocks.MIXER,
            TestModBlockEntityTypes.MIXER,
            TestModMenuTypes.MIXER,
            ImmutableList.of(MachineStatuses.IDLE), //todo
            () -> MachineEnergyStorage.of(30000, 300, 300, true, false),
            MachineItemStorage.builder()
                    .single(TestModSlotGroupTypes.CHARGE, ItemResourceSlot.builder()
                            .pos(8, 8)
                            .filter(ResourceFilters.CAN_EXTRACT_ENERGY)
                            .strictFilter(ResourceFilters.CAN_EXTRACT_ENERGY_STRICT)
                            .capacity(32)
                            ::build
                    )
                    .group(TestModSlotGroupTypes.TANK_IO, SlotGroup.item()
                            .add(ItemResourceSlot.builder()
                                    .pos(48, 8)
                                    .strictFilter(ResourceFilters.canExtractFluidStrict(Fluids.WATER))
                                    ::build
                            )
                            .add(ItemResourceSlot.builder()
                                    .pos(70, 8)
                                    .strictFilter(ResourceFilters.canExtractFluidStrict(Fluids.LAVA))
                                    ::build
                            )
                            ::build
                    )
                    .single(TestModSlotGroupTypes.OBSIDIAN, ItemResourceSlot.builder()
                            .pos(147, 43)
                            .filter(ResourceFilters.ofResource(Items.OBSIDIAN))
                            ::build
                    )
                    ::build,
            MachineFluidStorage.builder().group(TestModSlotGroupTypes.INPUTS, SlotGroup.fluid()
                    .add(FluidResourceSlot.builder()
                            .pos(48, 30)
                            .capacity(FluidConstants.BUCKET * 16)
                            .filter(ResourceFilters.ofResource(Fluids.WATER))
                            ::build
                    )
                    .add(FluidResourceSlot.builder()
                            .pos(70, 30)
                            .capacity(FluidConstants.BUCKET * 16)
                            .filter(ResourceFilters.ofResource(Fluids.LAVA))
                            ::build
                    )
                    ::build
            )::build
    );

    public static final MachineType<MelterBlockEntity, MachineMenu<MelterBlockEntity>> MELTER = MachineType.create(
            TestModBlocks.MELTER,
            TestModBlockEntityTypes.MELTER,
            TestModMenuTypes.MELTER,
            ImmutableList.of(MachineStatuses.ACTIVE, MachineStatuses.NOT_ENOUGH_ENERGY, MachineStatuses.OUTPUT_FULL, MachineStatuses.IDLE),
            () -> MachineEnergyStorage.of(30000, 300, 300, true, false),
            MachineItemStorage.builder()
                    .single(TestModSlotGroupTypes.CHARGE, ItemResourceSlot.builder()
                            .pos(8, 8)
                            .filter(ResourceFilters.CAN_EXTRACT_ENERGY)
                            .strictFilter(ResourceFilters.CAN_EXTRACT_ENERGY_STRICT)
                            .capacity(32)
                            ::build
                    )
                    .single(TestModSlotGroupTypes.INPUTS, ItemResourceSlot.builder()
                            .pos(59, 42)
                            .filter(ResourceFilters.ofResource(Items.COBBLESTONE))
                            ::build
                    )
                    .single(TestModSlotGroupTypes.TANK_IO, ItemResourceSlot.builder()
                            .pos(152, 62)
                            .strictFilter(ResourceFilters.canInsertFluidStrict(Fluids.LAVA))
                            ::build
                    )
                    ::build,
            MachineFluidStorage.builder().single(TestModSlotGroupTypes.LAVA, FluidResourceSlot.builder()
                    .pos(152, 8)
                    .capacity(FluidConstants.BUCKET * 16)
                    .filter(ResourceFilters.ofResource(Fluids.LAVA))
                    ::build
            )::build
    );

    public static void initialize() {
    }
}
