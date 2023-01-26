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

package dev.galacticraft.machinelib.gametest.storage;

import dev.galacticraft.machinelib.api.storage.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.ContainerSlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupTypes;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.gametest.MachineLibGametest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static dev.galacticraft.machinelib.gametest.Assertions.assertEquals;
import static dev.galacticraft.machinelib.gametest.Assertions.assertTrue;

public final class SlotGroupBuilderTest implements MachineLibGametest {
    @GameTest(template = EMPTY_STRUCTURE, batch = "slot_group", timeoutTicks = 0)
    void create_empty(GameTestHelper context) {
        assertEquals(null, SlotGroup.<Item, ItemStack, ItemResourceSlot>create(SlotGroupTypes.CHARGE).build());
        assertEquals(null, SlotGroup.<Item, ItemStack, ItemResourceSlot>create(SlotGroupTypes.CHARGE).add(null).build());
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "slot_group", timeoutTicks = 0)
    void create(GameTestHelper context) {
        assertEquals(1, SlotGroup.<Item, ItemStack, ItemResourceSlot>create(SlotGroupTypes.CHARGE)
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .build().size()
        );
        assertEquals(2, SlotGroup.<Item, ItemStack, ItemResourceSlot>create(SlotGroupTypes.CHARGE)
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .build().size()
        );
        assertEquals(5, SlotGroup.<Item, ItemStack, ItemResourceSlot>create(SlotGroupTypes.CHARGE)
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .build().size()
        );
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "slot_group", timeoutTicks = 0)
    void item_container_groups(GameTestHelper context) {
        assertTrue(SlotGroup.<Item, ItemStack, ItemResourceSlot>create(SlotGroupTypes.CHARGE)
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .build() instanceof ContainerSlotGroup<ItemResourceSlot>
        );
        assertTrue(SlotGroup.<Item, ItemStack, ItemResourceSlot>create(SlotGroupTypes.CHARGE)
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .build() instanceof ContainerSlotGroup<ItemResourceSlot>
        );
        assertTrue(SlotGroup.<Item, ItemStack, ItemResourceSlot>create(SlotGroupTypes.CHARGE)
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .build() instanceof ContainerSlotGroup<ItemResourceSlot>
        );
    }
}