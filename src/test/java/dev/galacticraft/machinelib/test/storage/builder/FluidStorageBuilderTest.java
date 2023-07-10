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

package dev.galacticraft.machinelib.test.storage.builder;

import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.test.JUnitTest;
import dev.galacticraft.machinelib.test.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public final class FluidStorageBuilderTest implements JUnitTest {
    public MachineFluidStorage.Builder builder;

    @BeforeEach
    public void setup() {
        this.builder = MachineFluidStorage.builder();
    }

    @Test
    public void empty() {
        assertEquals(0, MachineFluidStorage.empty().groups());
    }

    @Test
    public void buildEmpty() {
        assertSame(MachineFluidStorage.empty(), builder.build());
    }

    @Test
    public void singleSlot() {
        MachineFluidStorage storage = builder.single(Utils.INPUT_1, Utils.FLUID_SLOT_SUPPLIER).build();
        assertEquals(1, storage.groups());
        assertEquals(1, storage.getSlots().length);
    }

    @Test
    public void multiSingleSlot() {
        MachineFluidStorage storage = builder.single(Utils.INPUT_1, Utils.FLUID_SLOT_SUPPLIER)
                .single(Utils.INPUT_2, Utils.FLUID_SLOT_SUPPLIER).build();
        assertEquals(2, storage.groups());
        assertEquals(2, storage.getSlots().length);
    }

    @Test
    public void singleSlotGroup() {
        MachineFluidStorage storage = builder.group(Utils.INPUT_1, Utils.FLUID_GROUP_SUPPLIER).build();
        assertEquals(1, storage.groups());
        assertEquals(1, storage.getSlots().length);
    }

    @Test
    public void multiSlotGroup() {
        MachineFluidStorage storage = builder.group(Utils.INPUT_1, SlotGroup.fluid()
                .add(Utils.FLUID_SLOT_SUPPLIER).add(Utils.FLUID_SLOT_SUPPLIER)::build).build();
        assertEquals(1, storage.groups());
        assertEquals(2, storage.getSlots().length);
    }

    @Test
    public void multiSingleSlotGroup() {
        MachineFluidStorage storage = builder.group(Utils.INPUT_1, Utils.FLUID_GROUP_SUPPLIER)
                .group(Utils.OUTPUT_1, Utils.FLUID_GROUP_SUPPLIER).build();
        assertEquals(2, storage.groups());
        assertEquals(2, storage.getSlots().length);
    }

    @Test
    public void multiMultiSlotGroup() {
        MachineFluidStorage storage = builder.group(Utils.INPUT_1, SlotGroup.fluid()
                .add(Utils.FLUID_SLOT_SUPPLIER).add(Utils.FLUID_SLOT_SUPPLIER)::build).group(Utils.INPUT_2, SlotGroup.fluid()
                .add(Utils.FLUID_SLOT_SUPPLIER).add(Utils.FLUID_SLOT_SUPPLIER)::build).build();
        assertEquals(2, storage.groups());
        assertEquals(4, storage.getSlots().length);
    }

    @Test
    public void failDuplicateType() {
        assertThrows(IllegalArgumentException.class, () -> builder.group(Utils.INPUT_1, Utils.FLUID_GROUP_SUPPLIER)
                .group(Utils.INPUT_1, Utils.FLUID_GROUP_SUPPLIER).build());

        assertThrows(IllegalArgumentException.class, () -> builder.group(Utils.OUTPUT_1, Utils.FLUID_GROUP_SUPPLIER)
                .single(Utils.OUTPUT_1, Utils.FLUID_SLOT_SUPPLIER).build());

        assertThrows(IllegalArgumentException.class, () -> builder.single(Utils.TRANSFER_1, Utils.FLUID_SLOT_SUPPLIER)
                .single(Utils.TRANSFER_1, Utils.FLUID_SLOT_SUPPLIER).build());

        assertThrows(IllegalArgumentException.class, () -> builder.single(Utils.STORAGE_1, Utils.FLUID_SLOT_SUPPLIER)
                .single(Utils.STORAGE_1, Utils.FLUID_SLOT_SUPPLIER).build());
    }
}
