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
import dev.galacticraft.machinelib.testmod.block.entity.SimpleMachineBlockEntity;
import dev.galacticraft.machinelib.testmod.block.entity.TestModBlockEntityTypes;
import dev.galacticraft.machinelib.testmod.menu.TestModMenuTypes;
import dev.galacticraft.machinelib.testmod.slot.TestModSlotGroupTypes;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

public class TestModMachineTypes {
    public static final MachineType<SimpleMachineBlockEntity, MachineMenu<SimpleMachineBlockEntity>> SIMPLE_MACHINE = MachineType.create(
            TestModBlocks.SIMPLE_MACHINE_BLOCK,
            TestModBlockEntityTypes.SIMPLE_MACHINE,
            TestModMenuTypes.SIMPLE_MACHINE,
            ImmutableList.of(MachineStatuses.ACTIVE, MachineStatuses.NOT_ENOUGH_ENERGY, MachineStatuses.INVALID_RECIPE, MachineStatuses.OUTPUT_FULL),
            () -> MachineEnergyStorage.of(30000, 300, 300, true, false),
            MachineItemStorage.builder()
                    .group(TestModSlotGroupTypes.CHARGE, SlotGroup.item()
                            .add(ItemResourceSlot.builder()
                                    .pos(0, 0)
                                    .filter(ResourceFilters.CAN_EXTRACT_ENERGY)
                                    .strictFilter(ResourceFilters.CAN_EXTRACT_ENERGY_STRICT)
                                    .capacity(32)
                                    ::build
                            )::build
                    )
                    .group(TestModSlotGroupTypes.DIRT, SlotGroup.item()
                            .add(ItemResourceSlot.builder()
                                    .pos(16, 0)
                                    .filter(ResourceFilters.ofResource(Items.DIRT))
                                    ::build
                            )::build
                    )
                    .group(TestModSlotGroupTypes.DIAMONDS, SlotGroup.item()
                            .add(ItemResourceSlot.builder()
                                    .pos(32, 0)
                                    .filter(ResourceFilters.ofResource(Items.DIAMOND))
                                    ::build
                            )::build
                    )
                    .single(TestModSlotGroupTypes.TANK_IN, ItemResourceSlot.builder()
                            .pos(64, 64)
                            .filter(ResourceFilters.canExtractFluidStrict(Fluids.WATER))
                            ::build
                    )::build,
            MachineFluidStorage.builder().single(TestModSlotGroupTypes.WATER,
                    FluidResourceSlot.builder()
                            .pos(96, 16)
                            .capacity(FluidConstants.BUCKET * 10)
                            ::build
            )::build
    );

    public static void initialize() {
    }
}
