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

import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupTypes;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.testmod.block.entity.SimpleMachineBlockEntity;
import dev.galacticraft.machinelib.testmod.block.entity.TestModBlockEntityTypes;
import dev.galacticraft.machinelib.testmod.menu.SimpleMachineMenu;
import dev.galacticraft.machinelib.testmod.menu.TestModMenuTypes;
import dev.galacticraft.machinelib.testmod.slot.TestModSlotGroupTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TestModMachineTypes {
    public static final MachineType<SimpleMachineBlockEntity, SimpleMachineMenu> SIMPLE_MACHINE = MachineType.create(TestModBlocks.SIMPLE_MACHINE_BLOCK,
            TestModBlockEntityTypes.SIMPLE_MACHINE,
            TestModMenuTypes.SIMPLE_MACHINE,
            () -> MachineEnergyStorage.of(30000, 300, 300, true, false),
            () -> MachineItemStorage.builder()
                    .addGroup(
                            SlotGroup.<Item, ItemStack, ItemResourceSlot>create(SlotGroupTypes.CHARGE)
                                    .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.CAN_EXTRACT_ENERGY, ResourceFilters.CAN_EXTRACT_ENERGY_STRICT, 32))
                                    .build()
                    )
                    .addGroup(
                            SlotGroup.<Item, ItemStack, ItemResourceSlot>create(TestModSlotGroupTypes.DIRT)
                                    .add(ItemResourceSlot.create(ItemSlotDisplay.create(16, 0), ResourceFilters.matchAnyNbt(Items.DIRT)))
                                    .build()
                    )
                    .addGroup(
                            SlotGroup.<Item, ItemStack, ItemResourceSlot>create(TestModSlotGroupTypes.DIAMONDS)
                                    .add(ItemResourceSlot.create(ItemSlotDisplay.create(32, 0), ResourceFilters.matchAnyNbt(Items.DIAMOND)))
                                    .build()
                    )
                    .build(),
            MachineFluidStorage::empty
    );

    public static void initialize() {
    }
}
