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

import dev.galacticraft.machinelib.api.filter.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.api.transfer.InputType;
import dev.galacticraft.machinelib.impl.storage.slot.ResourceSlotImpl;
import dev.galacticraft.machinelib.test.JUnitTest;
import dev.galacticraft.machinelib.test.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public sealed class ItemStorageInsertionTests implements JUnitTest {
    private static final int CAPACITY = 64;
    protected MachineItemStorage storage;

    @BeforeEach
    public void setup() {
        this.storage = MachineItemStorage.create(ItemResourceSlot.create(InputType.STORAGE, ItemSlotDisplay.create(0, 0), ResourceFilters.any()));
    }

    @AfterEach
    public void verify() {
        assertTrue(((ResourceSlotImpl<?>) this.storage.getSlot(0)).isSane());
    }

    public static final class InsertionFailureTests extends ItemStorageInsertionTests {
        @Test
        public void full() {
            this.storage.getSlot(0).set(Items.GOLD_INGOT, CAPACITY);

            assertFalse(this.storage.canInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, this.storage.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, this.storage.insert(Items.GOLD_INGOT, 16));
        }

        @Test
        public void incorrectType() {
            this.storage.getSlot(0).set(Items.IRON_INGOT, 16);

            assertFalse(this.storage.canInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, this.storage.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, this.storage.insert(Items.GOLD_INGOT, 16));

            assertEquals(this.storage.getAmount(0), 16);
        }

        @Test
        public void insertTag() {
            this.storage.getSlot(0).set(Items.GOLD_INGOT, 16);

            assertFalse(this.storage.canInsert(Items.GOLD_INGOT, Utils.generateNbt(), 16));
            assertEquals(0, this.storage.tryInsert(Items.GOLD_INGOT, Utils.generateNbt(), 16));
            assertEquals(0, this.storage.insert(Items.GOLD_INGOT, Utils.generateNbt(), 16));

            assertEquals(this.storage.getAmount(0), 16);
        }

        @Test
        public void containedTag() {
            this.storage.getSlot(0).set(Items.GOLD_INGOT, Utils.generateNbt(), 16);

            assertFalse(this.storage.canInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, this.storage.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, this.storage.insert(Items.GOLD_INGOT, 16));

            assertEquals(this.storage.getAmount(0), 16);
        }

        @Test
        public void mismatchedTag() {
            this.storage.getSlot(0).set(Items.GOLD_INGOT, Utils.generateNbt(), 16);

            assertFalse(this.storage.canInsert(Items.GOLD_INGOT, Utils.generateNbt(), 16));
            assertEquals(0, this.storage.tryInsert(Items.GOLD_INGOT, Utils.generateNbt(), 16));
            assertEquals(0, this.storage.insert(Items.GOLD_INGOT, Utils.generateNbt(), 16));

            assertEquals(this.storage.getAmount(0), 16);
        }
    }

    public static final class InsertionSuccessTests extends ItemStorageInsertionTests {
        @Test
        public void empty() {
            assertTrue(this.storage.canInsert(Items.GOLD_INGOT, 16));
            assertEquals(16, this.storage.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(16, this.storage.insert(Items.GOLD_INGOT, 16));

            assertEquals(16, this.storage.getAmount(0));
        }

        @Test
        public void toCapacity() {
            assertTrue(this.storage.canInsert(Items.GOLD_INGOT, CAPACITY));
            assertEquals(CAPACITY, this.storage.tryInsert(Items.GOLD_INGOT, CAPACITY));
            assertEquals(CAPACITY, this.storage.insert(Items.GOLD_INGOT, CAPACITY));

            assertEquals(CAPACITY, this.storage.getAmount(0));
        }

        @Test
        public void overCapacity() {
            assertEquals(CAPACITY, this.storage.tryInsert(Items.GOLD_INGOT, CAPACITY + 8));
            assertEquals(CAPACITY, this.storage.insert(Items.GOLD_INGOT, CAPACITY + 8));

            assertEquals(CAPACITY, this.storage.getAmount(0));
        }

        @Test
        public void preFill() {
            this.storage.getSlot(0).set(Items.GOLD_INGOT, 16);
            assertTrue(this.storage.canInsert(Items.GOLD_INGOT, 48));
            assertEquals(48, this.storage.tryInsert(Items.GOLD_INGOT, 48));
            assertEquals(48, this.storage.insert(Items.GOLD_INGOT, 48));

            assertEquals(16 + 48, this.storage.getAmount(0));
        }

        @Test
        public void preFill_tag() {
            CompoundTag tag = Utils.generateNbt();
            this.storage.getSlot(0).set(Items.GOLD_INGOT, tag, 16);
            assertTrue(this.storage.canInsert(Items.GOLD_INGOT, tag, 48));
            assertEquals(48, this.storage.tryInsert(Items.GOLD_INGOT, tag, 48));
            assertEquals(48, this.storage.insert(Items.GOLD_INGOT, tag, 48));

            assertEquals(16 + 48, this.storage.getAmount(0));
        }

        @Test
        public void preFill_overCapacity() {
            this.storage.getSlot(0).set(Items.GOLD_INGOT, 50);
            assertEquals(14, this.storage.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(14, this.storage.insert(Items.GOLD_INGOT, 16));

            assertEquals(CAPACITY, this.storage.getAmount(0));
        }

        @Test
        public void preFill_overCapacity_tag() {
            CompoundTag tag = Utils.generateNbt();
            this.storage.getSlot(0).set(Items.GOLD_INGOT, tag, 50);
            assertEquals(14, this.storage.tryInsert(Items.GOLD_INGOT, tag, 16));
            assertEquals(14, this.storage.insert(Items.GOLD_INGOT, tag, 16));

            assertEquals(CAPACITY, this.storage.getAmount(0));
        }
    }
}
