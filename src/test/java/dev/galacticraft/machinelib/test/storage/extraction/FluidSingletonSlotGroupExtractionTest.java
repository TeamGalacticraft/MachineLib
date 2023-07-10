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

package dev.galacticraft.machinelib.test.storage.extraction;

import dev.galacticraft.machinelib.api.fluid.FluidStack;
import dev.galacticraft.machinelib.api.storage.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import dev.galacticraft.machinelib.impl.storage.slot.ResourceSlotImpl;
import dev.galacticraft.machinelib.test.JUnitTest;
import dev.galacticraft.machinelib.test.Utils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public sealed class FluidSingletonSlotGroupExtractionTest implements JUnitTest {
    protected SlotGroup<Fluid, FluidStack, FluidResourceSlot> group;

    @BeforeEach
    public void setup() {
        this.group = SlotGroup.ofFluid(FluidResourceSlot.create(TankDisplay.create(0, 0), FluidConstants.BUCKET * 16, ResourceFilters.any()));
    }

    @AfterEach
    public void verify() {
        assertTrue(((ResourceSlotImpl<?, ?>) this.group.getSlot(0)).isSane());
    }

    public static final class ExtractionFailureTests extends FluidSingletonSlotGroupExtractionTest {
        @Test
        public void empty() {
            assertTrue(this.group.isEmpty());

            assertFalse(this.group.canExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, this.group.tryExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, this.group.extract(Fluids.WATER, FluidConstants.BUCKET));

            assertFalse(this.group.canExtract(Fluids.WATER, Utils.EMPTY_NBT, FluidConstants.BUCKET));
            assertEquals(0, this.group.tryExtract(Fluids.WATER, Utils.EMPTY_NBT, FluidConstants.BUCKET));
            assertEquals(0, this.group.extract(Fluids.WATER, Utils.EMPTY_NBT, FluidConstants.BUCKET));

            assertFalse(this.group.canExtract(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
            assertEquals(0, this.group.tryExtract(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
            assertEquals(0, this.group.extract(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));

            assertFalse(this.group.extractOne(Fluids.WATER));
        }

        @Test
        public void incorrectType() {
            this.group.getSlot(0).set(Fluids.LAVA, FluidConstants.BUCKET);

            assertFalse(this.group.canExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, this.group.tryExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, this.group.extract(Fluids.WATER, FluidConstants.BUCKET));
            assertFalse(this.group.extractOne(Fluids.WATER));

            assertFalse(this.group.isEmpty());
        }

        @Test
        public void extractionTag() {
            this.group.getSlot(0).set(Fluids.WATER, FluidConstants.BUCKET);

            assertFalse(this.group.canExtract(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
            assertEquals(0, this.group.tryExtract(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
            assertEquals(0, this.group.extract(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
            assertFalse(this.group.extractOne(Fluids.WATER, Utils.generateNbt()));

            assertFalse(this.group.isEmpty());
        }

        @Test
        public void containedTag() {
            this.group.getSlot(0).set(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET);

            assertFalse(this.group.canExtract(Fluids.WATER, null, FluidConstants.BUCKET));
            assertEquals(0, this.group.tryExtract(Fluids.WATER, null, FluidConstants.BUCKET));
            assertEquals(0, this.group.extract(Fluids.WATER, null, FluidConstants.BUCKET));
            assertFalse(this.group.extractOne(Fluids.WATER, null));

            assertFalse(this.group.isEmpty());
        }

        @Test
        public void mismatchedTag() {
            this.group.getSlot(0).set(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET);

            assertFalse(this.group.canExtract(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
            assertEquals(0, this.group.tryExtract(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
            assertEquals(0, this.group.extract(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
            assertFalse(this.group.extractOne(Fluids.WATER, Utils.generateNbt()));

            assertFalse(this.group.isEmpty());
        }
    }

    public static final class ExtractionSuccessTests extends FluidSingletonSlotGroupExtractionTest {
        @Test
        public void exact_typed() {
            this.group.getSlot(0).set(Fluids.WATER, FluidConstants.BUCKET);

            assertTrue(this.group.canExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, this.group.tryExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, this.group.extract(Fluids.WATER, FluidConstants.BUCKET));

            assertTrue(this.group.isEmpty());
        }

        @Test
        public void exact_typed_nbt() {
            CompoundTag tag = Utils.generateNbt();
            this.group.getSlot(0).set(Fluids.WATER, tag, FluidConstants.BUCKET);

            assertTrue(this.group.canExtract(Fluids.WATER, tag, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, this.group.tryExtract(Fluids.WATER, tag, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, this.group.extract(Fluids.WATER, tag, FluidConstants.BUCKET));

            assertTrue(this.group.isEmpty());
        }

        @Test
        public void exact_typed_emptyNbt() {
            this.group.getSlot(0).set(Fluids.WATER, FluidConstants.BUCKET);

            assertTrue(this.group.canExtract(Fluids.WATER, Utils.EMPTY_NBT, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, this.group.tryExtract(Fluids.WATER, Utils.EMPTY_NBT, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, this.group.extract(Fluids.WATER, Utils.EMPTY_NBT, FluidConstants.BUCKET));

            assertTrue(this.group.isEmpty());
        }

        @Test
        public void excess_typed() {
            this.group.getSlot(0).set(Fluids.WATER, FluidConstants.BUCKET);

            assertTrue(this.group.canExtract(Fluids.WATER, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, this.group.tryExtract(Fluids.WATER, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, this.group.extract(Fluids.WATER, FluidConstants.BOTTLE));

            assertEquals(this.group.getAmount(0), FluidConstants.BUCKET - FluidConstants.BOTTLE);
        }

        @Test
        public void excess_typed_nbt() {
            CompoundTag tag = Utils.generateNbt();
            this.group.getSlot(0).set(Fluids.WATER, tag, FluidConstants.BUCKET);

            assertTrue(this.group.canExtract(Fluids.WATER, tag, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, this.group.tryExtract(Fluids.WATER, tag, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, this.group.extract(Fluids.WATER, tag, FluidConstants.BOTTLE));

            assertEquals(this.group.getAmount(0), FluidConstants.BUCKET - FluidConstants.BOTTLE);
        }

        @Test
        public void excess_typed_emptyNbt() {
            this.group.getSlot(0).set(Fluids.WATER, FluidConstants.BUCKET);

            assertTrue(this.group.canExtract(Fluids.WATER, Utils.EMPTY_NBT, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, this.group.tryExtract(Fluids.WATER, Utils.EMPTY_NBT, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, this.group.extract(Fluids.WATER, Utils.EMPTY_NBT, FluidConstants.BOTTLE));

            assertEquals(this.group.getAmount(0), FluidConstants.BUCKET - FluidConstants.BOTTLE);
        }

        @Test
        public void insufficient_typed() {
            this.group.getSlot(0).set(Fluids.WATER, FluidConstants.BOTTLE);

            assertEquals(FluidConstants.BOTTLE, this.group.tryExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BOTTLE, this.group.extract(Fluids.WATER, FluidConstants.BUCKET));

            assertTrue(this.group.isEmpty());
        }

        @Test
        public void insufficient_typed_nbt() {
            CompoundTag tag = Utils.generateNbt();
            this.group.getSlot(0).set(Fluids.WATER, tag, FluidConstants.BOTTLE);

            assertEquals(FluidConstants.BOTTLE, this.group.tryExtract(Fluids.WATER, tag, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BOTTLE, this.group.extract(Fluids.WATER, tag, FluidConstants.BUCKET));

            assertTrue(this.group.isEmpty());
        }

        @Test
        public void insufficient_typed_emptyNbt() {
            this.group.getSlot(0).set(Fluids.WATER, FluidConstants.BOTTLE);

            assertEquals(FluidConstants.BOTTLE, this.group.tryExtract(Fluids.WATER, Utils.EMPTY_NBT, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BOTTLE, this.group.extract(Fluids.WATER, Utils.EMPTY_NBT, FluidConstants.BUCKET));

            assertTrue(this.group.isEmpty());
        }
    }
}
