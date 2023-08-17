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

package dev.galacticraft.machinelib.test.storage.interop;

import dev.galacticraft.machinelib.api.filter.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import dev.galacticraft.machinelib.api.transfer.InputType;
import dev.galacticraft.machinelib.impl.storage.slot.ResourceSlotImpl;
import dev.galacticraft.machinelib.test.JUnitTest;
import dev.galacticraft.machinelib.test.Utils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public sealed class FluidResourceSlotTransactionTests implements JUnitTest {
    private static final long CAPACITY = FluidConstants.BUCKET * 16;
    private static final CompoundTag FILTERED_TAG = Utils.generateNbt();
    protected FluidResourceSlot slot;

    @BeforeEach
    public void setup() {
        this.slot = FluidResourceSlot.create(InputType.STORAGE, TankDisplay.create(0, 0), CAPACITY, ResourceFilters.not(ResourceFilters.ofNBT(FILTERED_TAG)));
    }

    @AfterEach
    public void verify() {
        assertTrue(((ResourceSlotImpl<?>) this.slot).isSane());
    }

    public static final class TransactionCancelledTests extends FluidResourceSlotTransactionTests {
        @Test
        public void extraction() {
            CompoundTag tag = Utils.generateNbt();
            this.slot.set(Fluids.WATER, tag, FluidConstants.BUCKET * 8);

            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(FluidConstants.BUCKET * 8, this.slot.extract(Fluids.WATER, tag, FluidConstants.BUCKET * 8, transaction));

                assertTrue(this.slot.isEmpty());
                assertNull(this.slot.getResource());
                assertNull(this.slot.getTag());
                assertEquals(0, this.slot.getAmount());
            }

            assertFalse(this.slot.isEmpty());
        }

        @Test
        public void insertion() {
            CompoundTag tag = Utils.generateNbt();
            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(FluidConstants.BUCKET * 5, this.slot.insert(Fluids.WATER, tag, FluidConstants.BUCKET * 5, transaction));

                assertEquals(Fluids.WATER, this.slot.getResource());
                assertEquals(tag, this.slot.getTag());
                assertEquals(FluidConstants.BUCKET * 5, this.slot.getAmount());
            }

            assertTrue(this.slot.isEmpty());
            assertNull(this.slot.getResource());
            assertNull(this.slot.getTag());
            assertEquals(0, this.slot.getAmount());
        }

        @Test
        public void exchange() {
            CompoundTag tag = Utils.generateNbt();
            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(FluidConstants.BUCKET * 5, this.slot.insert(Fluids.WATER, tag, FluidConstants.BUCKET * 5, transaction));

                assertEquals(Fluids.WATER, this.slot.getResource());
                assertEquals(tag, this.slot.getTag());
                assertEquals(FluidConstants.BUCKET * 5, this.slot.getAmount());
            }

            assertTrue(this.slot.isEmpty());
            assertNull(this.slot.getResource());
            assertNull(this.slot.getTag());
            assertEquals(0, this.slot.getAmount());
        }
    }

    public static final class TransactionCommittedTests extends FluidResourceSlotTransactionTests {
        @Test
        public void extraction() {
            CompoundTag tag = Utils.generateNbt();
            this.slot.set(Fluids.WATER, tag, FluidConstants.BUCKET * 8);

            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(FluidConstants.BUCKET * 8, this.slot.extract(Fluids.WATER, tag, FluidConstants.BUCKET * 8, transaction));

                assertTrue(this.slot.isEmpty());
                assertNull(this.slot.getResource());
                assertNull(this.slot.getTag());
                assertEquals(0, this.slot.getAmount());

                transaction.commit();
            }

            assertTrue(this.slot.isEmpty());
        }

        @Test
        public void insertion() {
            CompoundTag tag = Utils.generateNbt();
            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(FluidConstants.BUCKET * 5, this.slot.insert(Fluids.WATER, tag, FluidConstants.BUCKET * 5, transaction));

                assertEquals(Fluids.WATER, this.slot.getResource());
                assertEquals(tag, this.slot.getTag());
                assertEquals(FluidConstants.BUCKET * 5, this.slot.getAmount());

                transaction.commit();
            }

            assertEquals(FluidConstants.BUCKET * 5, this.slot.getAmount());
        }

        @Test
        public void exchange() {
            CompoundTag tag = Utils.generateNbt();
            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(FluidConstants.BUCKET * 5, this.slot.insert(Fluids.WATER, tag, FluidConstants.BUCKET * 5, transaction));

                assertEquals(Fluids.WATER, this.slot.getResource());
                assertEquals(tag, this.slot.getTag());

                assertEquals(FluidConstants.BUCKET * 5, this.slot.getAmount());
                transaction.commit();
            }

            assertEquals(FluidConstants.BUCKET * 5, this.slot.getAmount());
        }
    }
}
