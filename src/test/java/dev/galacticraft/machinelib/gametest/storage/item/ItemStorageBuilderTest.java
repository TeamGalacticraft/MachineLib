/*
 * Copyright (c) 2021-2022 Team Galacticraft
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

package dev.galacticraft.machinelib.gametest.storage.item;

import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.gametest.MachineLibGametest;
import dev.galacticraft.machinelib.testmod.TestMod;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import org.jetbrains.annotations.NotNull;

import static dev.galacticraft.machinelib.gametest.Assertions.assertEquals;
import static dev.galacticraft.machinelib.gametest.Assertions.assertThrows;

public final class ItemStorageBuilderTest implements MachineLibGametest {
    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void size(@NotNull GameTestHelper context) {
        assertEquals(0, MachineItemStorage.empty().size());
        assertEquals(1, MachineItemStorage.Builder.create().addSlot(TestMod.NO_DIAMOND_SLOT, TestMod.NO_DIAMONDS, true, new ItemSlotDisplay(0, 0)).build().size());
        assertEquals(2, MachineItemStorage.Builder.create().addSlot(TestMod.NO_DIAMOND_SLOT, TestMod.NO_DIAMONDS, true, new ItemSlotDisplay(0, 0)).addSlot(TestMod.NO_DIAMOND_SLOT, TestMod.NO_DIAMONDS, true, new ItemSlotDisplay(0, 0)).build().size());
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void create_empty(@NotNull GameTestHelper context) {
        assertEquals(MachineItemStorage.empty(), MachineItemStorage.Builder.create().build());
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void create_slot_size(@NotNull GameTestHelper context) {
        assertEquals(64, MachineItemStorage.Builder.create().addSlot(TestMod.NO_DIAMOND_SLOT, TestMod.NO_DIAMONDS, true, new ItemSlotDisplay(0, 0)).build().getCapacity(0));
        assertEquals(16, MachineItemStorage.Builder.create().addSlot(TestMod.NO_DIAMOND_SLOT, TestMod.NO_DIAMONDS, true, 16, new ItemSlotDisplay(0, 0)).build().getCapacity(0));
        assertThrows(() -> MachineItemStorage.Builder.create().addSlot(TestMod.NO_DIAMOND_SLOT, TestMod.NO_DIAMONDS, true, 5000, new ItemSlotDisplay(0, 0)));
        assertThrows(() -> MachineItemStorage.Builder.create().addSlot(TestMod.NO_DIAMOND_SLOT, TestMod.NO_DIAMONDS, true, -1, new ItemSlotDisplay(0, 0)));
    }
}
