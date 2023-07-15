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

package dev.galacticraft.machinelib.test.storage.insertion;

import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import dev.galacticraft.machinelib.api.transfer.InputType;
import dev.galacticraft.machinelib.filter.ResourceFilters;
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

public sealed class FluidStorageInsertionTests implements JUnitTest {
    private static final long CAPACITY = FluidConstants.BUCKET * 16;
    private static final CompoundTag FILTERED_TAG = Utils.generateNbt();
    protected MachineFluidStorage group;

    @BeforeEach
    public void setup() {
        this.group = MachineFluidStorage.create(FluidResourceSlot.create(InputType.STORAGE, TankDisplay.create(0, 0), CAPACITY, ResourceFilters.not(ResourceFilters.ofNBT(FILTERED_TAG))));
    }

    @AfterEach
    public void verify() {
        assertTrue(((ResourceSlotImpl<?>) this.group.getSlot(0)).isSane());
    }

    public static final class InsertionFailureTests extends FluidStorageInsertionTests {
        @Test
        public void full() {
            this.group.getSlot(0).set(Fluids.WATER, CAPACITY);

            assertFalse(this.group.canInsert(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, this.group.tryInsert(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, this.group.insert(Fluids.WATER, FluidConstants.BUCKET));
        }

        @Test
        public void incorrectType() {
            this.group.getSlot(0).set(Fluids.WATER, FluidConstants.BUCKET);

            assertFalse(this.group.canInsert(Fluids.LAVA, FluidConstants.BUCKET));
            assertEquals(0, this.group.tryInsert(Fluids.LAVA, FluidConstants.BUCKET));
            assertEquals(0, this.group.insert(Fluids.LAVA, FluidConstants.BUCKET));

            assertEquals(this.group.getAmount(0), FluidConstants.BUCKET);
        }

        @Test
        public void filtered() {
            assertTrue(this.group.canInsert(Fluids.WATER));

            assertFalse(this.group.canInsert(Fluids.WATER, FILTERED_TAG, FluidConstants.BUCKET));
            assertEquals(0, this.group.tryInsert(Fluids.WATER, FILTERED_TAG, FluidConstants.BUCKET));
            assertEquals(0, this.group.insert(Fluids.WATER, FILTERED_TAG, FluidConstants.BUCKET));

            assertTrue(group.isEmpty());
        }

        @Test
        public void insertTag() {
            this.group.getSlot(0).set(Fluids.WATER, FluidConstants.BUCKET);

            assertFalse(this.group.canInsert(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
            assertEquals(0, this.group.tryInsert(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
            assertEquals(0, this.group.insert(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));

            assertEquals(this.group.getAmount(0), FluidConstants.BUCKET);
        }

        @Test
        public void containedTag() {
            this.group.getSlot(0).set(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET);

            assertFalse(this.group.canInsert(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, this.group.tryInsert(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, this.group.insert(Fluids.WATER, FluidConstants.BUCKET));

            assertEquals(this.group.getAmount(0), FluidConstants.BUCKET);
        }

        @Test
        public void mismatchedTag() {
            this.group.getSlot(0).set(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET);

            assertFalse(this.group.canInsert(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
            assertEquals(0, this.group.tryInsert(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));
            assertEquals(0, this.group.insert(Fluids.WATER, Utils.generateNbt(), FluidConstants.BUCKET));

            assertEquals(this.group.getAmount(0), FluidConstants.BUCKET);
        }
    }

    public static final class InsertionSuccessTests extends FluidStorageInsertionTests {
        @Test
        public void empty() {
            assertTrue(this.group.canInsert(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, this.group.tryInsert(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, this.group.insert(Fluids.WATER, FluidConstants.BUCKET));

            assertEquals(FluidConstants.BUCKET, this.group.getAmount(0));
        }

        @Test
        public void toCapacity() {
            assertTrue(this.group.canInsert(Fluids.WATER, CAPACITY));
            assertEquals(CAPACITY, this.group.tryInsert(Fluids.WATER, CAPACITY));
            assertEquals(CAPACITY, this.group.insert(Fluids.WATER, CAPACITY));

            assertEquals(CAPACITY, this.group.getAmount(0));
        }

        @Test
        public void overCapacity() {
            assertEquals(CAPACITY, this.group.tryInsert(Fluids.WATER, CAPACITY + FluidConstants.BOTTLE));
            assertEquals(CAPACITY, this.group.insert(Fluids.WATER, CAPACITY + FluidConstants.BOTTLE));

            assertEquals(CAPACITY, this.group.getAmount(0));
        }

        @Test
        public void preFill() {
            this.group.getSlot(0).set(Fluids.WATER, FluidConstants.BUCKET);
            assertTrue(this.group.canInsert(Fluids.WATER, FluidConstants.BUCKET * 6));
            assertEquals(FluidConstants.BUCKET * 6, this.group.tryInsert(Fluids.WATER, FluidConstants.BUCKET * 6));
            assertEquals(FluidConstants.BUCKET * 6, this.group.insert(Fluids.WATER, FluidConstants.BUCKET * 6));

            assertEquals(FluidConstants.BUCKET * 7, this.group.getAmount(0));
        }

        @Test
        public void preFill_tag() {
            CompoundTag tag = Utils.generateNbt();
            this.group.getSlot(0).set(Fluids.WATER, tag, FluidConstants.BUCKET);
            assertTrue(this.group.canInsert(Fluids.WATER, tag, FluidConstants.BUCKET * 6));
            assertEquals(FluidConstants.BUCKET * 6, this.group.tryInsert(Fluids.WATER, tag, FluidConstants.BUCKET * 6));
            assertEquals(FluidConstants.BUCKET * 6, this.group.insert(Fluids.WATER, tag, FluidConstants.BUCKET * 6));

            assertEquals(FluidConstants.BUCKET * 7, this.group.getAmount(0));
        }

        @Test
        public void preFill_overCapacity() {
            this.group.getSlot(0).set(Fluids.WATER, FluidConstants.BUCKET * 12);
            assertEquals(FluidConstants.BUCKET * 4, this.group.tryInsert(Fluids.WATER, FluidConstants.BUCKET * 7));
            assertEquals(FluidConstants.BUCKET * 4, this.group.insert(Fluids.WATER, FluidConstants.BUCKET * 7));

            assertEquals(CAPACITY, this.group.getAmount(0));
        }

        @Test
        public void preFill_overCapacity_tag() {
            CompoundTag tag = Utils.generateNbt();
            this.group.getSlot(0).set(Fluids.WATER, tag, FluidConstants.BUCKET * 12);
            assertEquals(FluidConstants.BUCKET * 4, this.group.tryInsert(Fluids.WATER, tag, FluidConstants.BUCKET * 7));
            assertEquals(FluidConstants.BUCKET * 4, this.group.insert(Fluids.WATER, tag, FluidConstants.BUCKET * 7));

            assertEquals(CAPACITY, this.group.getAmount(0));
        }
    }
}
