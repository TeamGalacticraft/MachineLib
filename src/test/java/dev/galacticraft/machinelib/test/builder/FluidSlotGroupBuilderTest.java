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

import dev.galacticraft.machinelib.api.fluid.FluidStack;
import dev.galacticraft.machinelib.api.gametest.annotation.UnitTest;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.test.JUnitTest;
import dev.galacticraft.machinelib.test.Utils;
import net.minecraft.world.level.material.Fluid;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

public class FluidSlotGroupBuilderTest implements JUnitTest {
    private SlotGroup.Builder<Fluid, FluidStack, FluidResourceSlot> builder;

    @BeforeEach
    public void setup() {
        this.builder = SlotGroup.fluid();
    }
    
    @UnitTest
    public void empty() {
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @UnitTest
    public void single() {
        assertEquals(1, builder.add(Utils.FLUID_SLOT_SUPPLIER).build().size());
    }

    @UnitTest
    public void multiple() {
        assertEquals(2, builder.add(Utils.FLUID_SLOT_SUPPLIER).add(Utils.FLUID_SLOT_SUPPLIER).build().size());
    }

    @UnitTest
    public void reuse() {
        SlotGroup<Fluid, FluidStack, FluidResourceSlot> group = builder.add(Utils.FLUID_SLOT_SUPPLIER).add(Utils.FLUID_SLOT_SUPPLIER).build();
        SlotGroup<Fluid, FluidStack, FluidResourceSlot> group2 = builder.build();
        assertNotSame(group, group2);
        assertEquals(group.size(), group2.size());

        assertNotSame(group.getSlot(0), group2.getSlot(0));
        assertNotSame(group.getSlot(1), group2.getSlot(1));
    }
}
