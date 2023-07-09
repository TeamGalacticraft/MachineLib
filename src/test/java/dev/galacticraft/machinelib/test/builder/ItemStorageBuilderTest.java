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

package dev.galacticraft.machinelib.test.builder;

import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.test.JUnitTest;
import dev.galacticraft.machinelib.test.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public final class ItemStorageBuilderTest implements JUnitTest {
    public MachineItemStorage.Builder builder;

    @BeforeEach
    public void setup() {
        this.builder = MachineItemStorage.builder();
    }

    @Test
    public void empty() {
        assertEquals(0, MachineItemStorage.empty().groups());
    }

    @Test
    public void buildEmpty() {
        assertSame(MachineItemStorage.empty(), builder.build());
    }

    @Test
    public void singleSlot() {
        MachineItemStorage storage = builder.single(Utils.INPUT_1, Utils.ITEM_SLOT_SUPPLIER).build();
        assertEquals(1, storage.groups());
        assertEquals(1, storage.getSlots().length);
    }

    @Test
    public void multiSingleSlot() {
        MachineItemStorage storage = builder.single(Utils.INPUT_1, Utils.ITEM_SLOT_SUPPLIER)
                .single(Utils.INPUT_2, Utils.ITEM_SLOT_SUPPLIER).build();
        assertEquals(2, storage.groups());
        assertEquals(2, storage.getSlots().length);
    }

    @Test
    public void singleSlotGroup() {
        MachineItemStorage storage = builder.group(Utils.INPUT_1, Utils.ITEM_GROUP_SUPPLIER).build();
        assertEquals(1, storage.groups());
        assertEquals(1, storage.getSlots().length);
    }

    @Test
    public void multiSlotGroup() {
        MachineItemStorage storage = builder.group(Utils.INPUT_1, SlotGroup.item()
                .add(Utils.ITEM_SLOT_SUPPLIER).add(Utils.ITEM_SLOT_SUPPLIER)::build).build();
        assertEquals(1, storage.groups());
        assertEquals(2, storage.getSlots().length);
    }

    @Test
    public void multiSingleSlotGroup() {
        MachineItemStorage storage = builder.group(Utils.INPUT_1, Utils.ITEM_GROUP_SUPPLIER)
                .group(Utils.OUTPUT_1, Utils.ITEM_GROUP_SUPPLIER).build();
        assertEquals(2, storage.groups());
        assertEquals(2, storage.getSlots().length);
    }

    @Test
    public void multiMultiSlotGroup() {
        MachineItemStorage storage = builder.group(Utils.INPUT_1, SlotGroup.item()
                .add(Utils.ITEM_SLOT_SUPPLIER).add(Utils.ITEM_SLOT_SUPPLIER)::build).group(Utils.INPUT_2, SlotGroup.item()
                .add(Utils.ITEM_SLOT_SUPPLIER).add(Utils.ITEM_SLOT_SUPPLIER)::build).build();
        assertEquals(2, storage.groups());
        assertEquals(4, storage.getSlots().length);
    }

    @Test
    public void failDuplicateType() {
        assertThrows(IllegalArgumentException.class, () -> builder.group(Utils.INPUT_1, Utils.ITEM_GROUP_SUPPLIER)
                .group(Utils.INPUT_1, Utils.ITEM_GROUP_SUPPLIER).build());

        assertThrows(IllegalArgumentException.class, () -> builder.group(Utils.OUTPUT_1, Utils.ITEM_GROUP_SUPPLIER)
                .single(Utils.OUTPUT_1, Utils.ITEM_SLOT_SUPPLIER).build());

        assertThrows(IllegalArgumentException.class, () -> builder.single(Utils.TRANSFER_1, Utils.ITEM_SLOT_SUPPLIER)
                .single(Utils.TRANSFER_1, Utils.ITEM_SLOT_SUPPLIER).build());

        assertThrows(IllegalArgumentException.class, () -> builder.single(Utils.STORAGE_1, Utils.ITEM_SLOT_SUPPLIER)
                .single(Utils.STORAGE_1, Utils.ITEM_SLOT_SUPPLIER).build());
    }
}
