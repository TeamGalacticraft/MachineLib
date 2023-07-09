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

package dev.galacticraft.machinelib.test.storage.insertion.failure;

import dev.galacticraft.machinelib.api.fluid.FluidStack;
import dev.galacticraft.machinelib.api.storage.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import dev.galacticraft.machinelib.test.JUnitTest;
import dev.galacticraft.machinelib.test.Utils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FluidSingletonSlotGroupInsertionFailureTest implements JUnitTest {
    private static final CompoundTag FILTERED_TAG = Utils.generateNbt();
    private SlotGroup<Fluid, FluidStack, FluidResourceSlot> group;

    @BeforeEach
    public void setup() {
        this.group = SlotGroup.ofFluid(FluidResourceSlot.create(TankDisplay.create(0, 0), FluidConstants.BUCKET * 16, ResourceFilters.not(ResourceFilters.ofNBT(FILTERED_TAG))));
    }

    @Test
    public void full() {
        this.group.getSlot(0).set(Fluids.WATER, this.group.getSlot(0).getCapacityFor(Fluids.WATER));

        assertEquals(0, this.group.tryInsert(Fluids.WATER, FluidConstants.BUCKET));
        assertEquals(0, this.group.insert(Fluids.WATER, FluidConstants.BUCKET));
        assertEquals(0, this.group.insertStack(FluidStack.create(Fluids.WATER, FluidConstants.BUCKET)));
    }

    @Test
    public void incorrectType() {
        this.group.getSlot(0).set(Fluids.WATER, FluidConstants.BUCKET);

        assertFalse(this.group.canInsert(Fluids.LAVA, FluidConstants.BUCKET));
        assertEquals(0, this.group.tryInsert(Fluids.LAVA, FluidConstants.BUCKET));
        assertEquals(0, this.group.insert(Fluids.LAVA, FluidConstants.BUCKET));
        assertEquals(0, this.group.insertStack(FluidStack.create(Fluids.LAVA, FluidConstants.BUCKET)));

        assertEquals(this.group.getAmount(0), FluidConstants.BUCKET);
    }

    @Test
    public void filtered() {
        assertTrue(this.group.canInsert(Fluids.WATER));

        assertFalse(this.group.canInsert(Fluids.WATER, FILTERED_TAG, FluidConstants.BUCKET));
        assertEquals(0, this.group.tryInsert(Fluids.WATER, FILTERED_TAG, FluidConstants.BUCKET));
        assertEquals(0, this.group.insert(Fluids.WATER, FILTERED_TAG, FluidConstants.BUCKET));
        assertEquals(0, this.group.insertStack(FluidStack.create(Fluids.WATER, FILTERED_TAG, FluidConstants.BUCKET)));

        assertTrue(group.isEmpty());
    }

    @Test
    public void insertTag() {
        this.group.getSlot(0).set(Fluids.WATER, FluidConstants.BUCKET);

        assertFalse(this.group.canInsert(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
        assertEquals(0, this.group.tryInsert(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
        assertEquals(0, this.group.insert(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
        assertEquals(0, this.group.insertStack(FluidStack.create(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET)));

        assertEquals(this.group.getAmount(0), FluidConstants.BUCKET);
    }

    @Test
    public void containedTag() {
        this.group.getSlot(0).set(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET);

        assertFalse(this.group.canInsert(Fluids.WATER, FluidConstants.BUCKET));
        assertEquals(0, this.group.tryInsert(Fluids.WATER, FluidConstants.BUCKET));
        assertEquals(0, this.group.insert(Fluids.WATER, FluidConstants.BUCKET));
        assertEquals(0, this.group.insertStack(FluidStack.create(Fluids.WATER, FluidConstants.BUCKET)));

        assertEquals(this.group.getAmount(0), FluidConstants.BUCKET);
    }

    @Test
    public void mismatchedTag() {
        this.group.getSlot(0).set(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET);

        assertFalse(this.group.canInsert(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
        assertEquals(0, this.group.tryInsert(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
        assertEquals(0, this.group.insert(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
        assertEquals(0, this.group.insertStack(FluidStack.create(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET)));

        assertEquals(this.group.getAmount(0), FluidConstants.BUCKET);
    }
}
