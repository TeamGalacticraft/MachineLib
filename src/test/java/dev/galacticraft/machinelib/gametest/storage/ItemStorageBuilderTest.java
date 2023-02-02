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

import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.gametest.MachineLibGametest;
import dev.galacticraft.machinelib.testmod.slot.TestModSlotGroupTypes;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import org.jetbrains.annotations.NotNull;

import static dev.galacticraft.machinelib.gametest.Assertions.*;

public final class ItemStorageBuilderTest implements MachineLibGametest {
    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void create_empty(@NotNull GameTestHelper context) {
        assertEquals(0, MachineItemStorage.empty().groups());
        assertEquals(0, MachineItemStorage.builder().build().groups());

        assertIdentityEquals(MachineItemStorage.empty(), MachineItemStorage.builder().build());
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void create_slot_size(@NotNull GameTestHelper context) {
        assertEquals(1, MachineItemStorage.builder()
                .group(TestModSlotGroupTypes.CHARGE, SlotGroup.item()
                        .add(ItemResourceSlot.builder()::build)
                        ::build
                )
                .build().groups()
        );

        assertEquals(1, MachineItemStorage.builder()
                .single(TestModSlotGroupTypes.CHARGE, ItemResourceSlot.builder()::build)
                .build().groups()
        );
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void fail_duplicate(@NotNull GameTestHelper context) {
        assertThrows(() -> MachineItemStorage.builder()
                .group(TestModSlotGroupTypes.CHARGE, SlotGroup.item()
                        .add(ItemResourceSlot.builder()::build)
                        ::build
                )
                .group(TestModSlotGroupTypes.CHARGE, SlotGroup.item() // duplicate group
                        .add(ItemResourceSlot.builder()::build)
                        ::build
                )
                .build().groups()
        );
    }
}
