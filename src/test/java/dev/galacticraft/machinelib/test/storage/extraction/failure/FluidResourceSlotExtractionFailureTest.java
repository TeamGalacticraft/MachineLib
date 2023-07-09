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

package dev.galacticraft.machinelib.test.storage.extraction.failure;

import dev.galacticraft.machinelib.api.storage.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import dev.galacticraft.machinelib.test.JUnitTest;
import dev.galacticraft.machinelib.test.Utils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FluidResourceSlotExtractionFailureTest implements JUnitTest {
    private FluidResourceSlot slot;

    @BeforeEach
    public void setup() {
        this.slot = FluidResourceSlot.create(TankDisplay.create(0, 0), FluidConstants.BUCKET * 16, ResourceFilters.any());
    }

    @Test
    public void empty() {
        assertTrue(this.slot.isEmpty());
        
        assertFalse(this.slot.canExtract(FluidConstants.BUCKET));
        assertEquals(0, this.slot.tryExtract(FluidConstants.BUCKET));
        assertEquals(0, this.slot.extract(FluidConstants.BUCKET));

        assertFalse(this.slot.canExtract(Fluids.WATER, FluidConstants.BUCKET));
        assertEquals(0, this.slot.tryExtract(Fluids.WATER, FluidConstants.BUCKET));
        assertEquals(0, this.slot.extract(Fluids.WATER, FluidConstants.BUCKET));

        assertFalse(this.slot.canExtract(Fluids.WATER, Utils.EMPTY_NBT, FluidConstants.BUCKET));
        assertEquals(0, this.slot.tryExtract(Fluids.WATER, Utils.EMPTY_NBT, FluidConstants.BUCKET));
        assertEquals(0, this.slot.extract(Fluids.WATER, Utils.EMPTY_NBT, FluidConstants.BUCKET));

        assertFalse(this.slot.canExtract(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
        assertEquals(0, this.slot.tryExtract(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
        assertEquals(0, this.slot.extract(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));

        assertFalse(this.slot.extractOne());
    }

    @Test
    public void incorrectType() {
        this.slot.set(Fluids.WATER, FluidConstants.BUCKET);

        assertFalse(this.slot.canExtract(Fluids.LAVA, FluidConstants.BUCKET));
        assertEquals(0, this.slot.tryExtract(Fluids.LAVA, FluidConstants.BUCKET));
        assertEquals(0, this.slot.extract(Fluids.LAVA, FluidConstants.BUCKET));
        assertFalse(this.slot.extractOne(Fluids.LAVA));

        assertFalse(this.slot.isEmpty());
    }

    @Test
    public void extractionTag() {
        this.slot.set(Fluids.WATER, FluidConstants.BUCKET);

        assertFalse(this.slot.canExtract(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
        assertEquals(0, this.slot.tryExtract(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
        assertEquals(0, this.slot.extract(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
        assertFalse(this.slot.extractOne(Fluids.WATER, Utils.generateNbt()));

        assertFalse(this.slot.isEmpty());
    }

    @Test
    public void containedTag() {
        this.slot.set(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET);

        assertFalse(this.slot.canExtract(Fluids.WATER, null, FluidConstants.BUCKET));
        assertEquals(0, this.slot.tryExtract(Fluids.WATER, null, FluidConstants.BUCKET));
        assertEquals(0, this.slot.extract(Fluids.WATER, null, FluidConstants.BUCKET));
        assertFalse(this.slot.extractOne(Fluids.WATER, null));

        assertFalse(this.slot.isEmpty());
    }

    @Test
    public void mismatchedTag() {
        this.slot.set(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET);

        assertFalse(this.slot.canExtract(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
        assertEquals(0, this.slot.tryExtract(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
        assertEquals(0, this.slot.extract(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
        assertFalse(this.slot.extractOne(Fluids.WATER, Utils.generateNbt()));

        assertFalse(this.slot.isEmpty());
    }
}
