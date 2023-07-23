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

import dev.galacticraft.machinelib.api.filter.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import dev.galacticraft.machinelib.api.transfer.InputType;
import dev.galacticraft.machinelib.impl.storage.slot.ResourceSlotImpl;
import dev.galacticraft.machinelib.test.JUnitTest;
import dev.galacticraft.machinelib.test.Utils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public sealed class FluidResourceSlotExtractionTests implements JUnitTest {
    protected FluidResourceSlot slot;

    @BeforeEach
    public void setup() {
        this.slot = FluidResourceSlot.create(InputType.STORAGE, TankDisplay.create(0, 0), FluidConstants.BUCKET * 16, ResourceFilters.any());
    }

    @AfterEach
    public void verify() {
        assertTrue(((ResourceSlotImpl<?>) this.slot).isSane());
    }

    public static final class ExtractionFailureTests extends FluidResourceSlotExtractionTests {
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
            this.slot.set(Fluids.LAVA, FluidConstants.BUCKET);

            assertFalse(this.slot.canExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, this.slot.tryExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, this.slot.extract(Fluids.WATER, FluidConstants.BUCKET));
            assertFalse(this.slot.extractOne(Fluids.WATER));

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

    public static final class ExtractionSuccessTests extends FluidResourceSlotExtractionTests {
        @Test
        public void exact_any() {
            this.slot.set(Fluids.WATER, FluidConstants.BUCKET);

            assertTrue(this.slot.canExtract(FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, this.slot.tryExtract(FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, this.slot.extract(FluidConstants.BUCKET));

            assertTrue(this.slot.isEmpty());
        }

        @Test
        public void exact_typed() {
            this.slot.set(Fluids.WATER, FluidConstants.BUCKET);

            assertTrue(this.slot.canExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, this.slot.tryExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, this.slot.extract(Fluids.WATER, FluidConstants.BUCKET));

            assertTrue(this.slot.isEmpty());
        }

        @Test
        public void exact_typed_nbt() {
            CompoundTag tag = Utils.generateNbt();
            this.slot.set(Fluids.WATER, tag, FluidConstants.BUCKET);

            assertTrue(this.slot.canExtract(Fluids.WATER, tag, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, this.slot.tryExtract(Fluids.WATER, tag, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, this.slot.extract(Fluids.WATER, tag, FluidConstants.BUCKET));

            assertTrue(this.slot.isEmpty());
        }

        @Test
        public void exact_typed_emptyNbt() {
            this.slot.set(Fluids.WATER, FluidConstants.BUCKET);

            assertTrue(this.slot.canExtract(Fluids.WATER, Utils.EMPTY_NBT, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, this.slot.tryExtract(Fluids.WATER, Utils.EMPTY_NBT, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, this.slot.extract(Fluids.WATER, Utils.EMPTY_NBT, FluidConstants.BUCKET));

            assertTrue(this.slot.isEmpty());
        }

        @Test
        public void excess_any() {
            this.slot.set(Fluids.WATER, FluidConstants.BUCKET);

            assertTrue(this.slot.canExtract(FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, this.slot.tryExtract(FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, this.slot.extract(FluidConstants.BOTTLE));

            assertEquals(this.slot.getAmount(), FluidConstants.BUCKET - FluidConstants.BOTTLE);
        }

        @Test
        public void excess_typed() {
            this.slot.set(Fluids.WATER, FluidConstants.BUCKET);

            assertTrue(this.slot.canExtract(Fluids.WATER, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, this.slot.tryExtract(Fluids.WATER, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, this.slot.extract(Fluids.WATER, FluidConstants.BOTTLE));

            assertEquals(this.slot.getAmount(), FluidConstants.BUCKET - FluidConstants.BOTTLE);
        }

        @Test
        public void excess_typed_nbt() {
            CompoundTag tag = Utils.generateNbt();
            this.slot.set(Fluids.WATER, tag, FluidConstants.BUCKET);

            assertTrue(this.slot.canExtract(Fluids.WATER, tag, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, this.slot.tryExtract(Fluids.WATER, tag, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, this.slot.extract(Fluids.WATER, tag, FluidConstants.BOTTLE));

            assertEquals(this.slot.getAmount(), FluidConstants.BUCKET - FluidConstants.BOTTLE);
        }

        @Test
        public void excess_typed_emptyNbt() {
            this.slot.set(Fluids.WATER, FluidConstants.BUCKET);

            assertTrue(this.slot.canExtract(Fluids.WATER, Utils.EMPTY_NBT, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, this.slot.tryExtract(Fluids.WATER, Utils.EMPTY_NBT, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, this.slot.extract(Fluids.WATER, Utils.EMPTY_NBT, FluidConstants.BOTTLE));

            assertEquals(this.slot.getAmount(), FluidConstants.BUCKET - FluidConstants.BOTTLE);
        }

        @Test
        public void insufficient_any() {
            this.slot.set(Fluids.WATER, FluidConstants.BOTTLE);

            assertEquals(FluidConstants.BOTTLE, this.slot.tryExtract(FluidConstants.BUCKET));
            assertEquals(FluidConstants.BOTTLE, this.slot.extract(FluidConstants.BUCKET));

            assertTrue(this.slot.isEmpty());
        }

        @Test
        public void insufficient_typed() {
            this.slot.set(Fluids.WATER, FluidConstants.BOTTLE);

            assertEquals(FluidConstants.BOTTLE, this.slot.tryExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BOTTLE, this.slot.extract(Fluids.WATER, FluidConstants.BUCKET));

            assertTrue(this.slot.isEmpty());
        }

        @Test
        public void insufficient_typed_nbt() {
            CompoundTag tag = Utils.generateNbt();
            this.slot.set(Fluids.WATER, tag, FluidConstants.BOTTLE);

            assertEquals(FluidConstants.BOTTLE, this.slot.tryExtract(Fluids.WATER, tag, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BOTTLE, this.slot.extract(Fluids.WATER, tag, FluidConstants.BUCKET));

            assertTrue(this.slot.isEmpty());
        }

        @Test
        public void insufficient_typed_emptyNbt() {
            this.slot.set(Fluids.WATER, FluidConstants.BOTTLE);

            assertEquals(FluidConstants.BOTTLE, this.slot.tryExtract(Fluids.WATER, Utils.EMPTY_NBT, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BOTTLE, this.slot.extract(Fluids.WATER, Utils.EMPTY_NBT, FluidConstants.BUCKET));

            assertTrue(this.slot.isEmpty());
        }
    }
}
