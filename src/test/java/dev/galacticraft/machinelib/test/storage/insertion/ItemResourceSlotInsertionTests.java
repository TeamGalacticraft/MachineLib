/*
 * Copyright (c) 2021-2024 Team Galacticraft
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

public sealed class ItemResourceSlotInsertionTests implements JUnitTest {
    private static final int CAPACITY = 64;
    protected ItemResourceSlot slot;

    @BeforeEach
    public void setup() {
        this.slot = ItemResourceSlot.create(InputType.STORAGE, ItemSlotDisplay.create(0, 0), ResourceFilters.any());
    }

    @AfterEach
    public void verify() {
        assertTrue(((ResourceSlotImpl<?>) this.slot).isSane());
    }

    public static final class InsertionFailureTests extends ItemResourceSlotInsertionTests {
        @Test
        public void full() {
            this.slot.set(Items.GOLD_INGOT, CAPACITY);

            assertFalse(this.slot.canInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, this.slot.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, this.slot.insert(Items.GOLD_INGOT, 16));
        }

        @Test
        public void incorrectType() {
            this.slot.set(Items.IRON_INGOT, 16);

            assertFalse(this.slot.canInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, this.slot.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, this.slot.insert(Items.GOLD_INGOT, 16));

            assertEquals(this.slot.getAmount(), 16);
        }

        @Test
        public void insertTag() {
            this.slot.set(Items.GOLD_INGOT, 16);

            assertFalse(this.slot.canInsert(Items.GOLD_INGOT, Utils.generateNbt(), 16));
            assertEquals(0, this.slot.tryInsert(Items.GOLD_INGOT, Utils.generateNbt(), 16));
            assertEquals(0, this.slot.insert(Items.GOLD_INGOT, Utils.generateNbt(), 16));

            assertEquals(this.slot.getAmount(), 16);
        }

        @Test
        public void containedTag() {
            this.slot.set(Items.GOLD_INGOT, Utils.generateNbt(), 16);

            assertFalse(this.slot.canInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, this.slot.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, this.slot.insert(Items.GOLD_INGOT, 16));

            assertEquals(this.slot.getAmount(), 16);
        }

        @Test
        public void mismatchedTag() {
            this.slot.set(Items.GOLD_INGOT, Utils.generateNbt(), 16);

            assertFalse(this.slot.canInsert(Items.GOLD_INGOT, Utils.generateNbt(), 16));
            assertEquals(0, this.slot.tryInsert(Items.GOLD_INGOT, Utils.generateNbt(), 16));
            assertEquals(0, this.slot.insert(Items.GOLD_INGOT, Utils.generateNbt(), 16));

            assertEquals(this.slot.getAmount(), 16);
        }
    }

    public static final class InsertionSuccessTests extends ItemResourceSlotInsertionTests {
        @Test
        public void empty() {
            assertTrue(this.slot.canInsert(Items.GOLD_INGOT, 16));
            assertEquals(16, this.slot.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(16, this.slot.insert(Items.GOLD_INGOT, 16));

            assertEquals(16, this.slot.getAmount());
        }

        @Test
        public void toCapacity() {
            assertTrue(this.slot.canInsert(Items.GOLD_INGOT, CAPACITY));
            assertEquals(CAPACITY, this.slot.tryInsert(Items.GOLD_INGOT, CAPACITY));
            assertEquals(CAPACITY, this.slot.insert(Items.GOLD_INGOT, CAPACITY));

            assertEquals(CAPACITY, this.slot.getAmount());
        }

        @Test
        public void overCapacity() {
            assertEquals(CAPACITY, this.slot.tryInsert(Items.GOLD_INGOT, CAPACITY + 8));
            assertEquals(CAPACITY, this.slot.insert(Items.GOLD_INGOT, CAPACITY + 8));

            assertEquals(CAPACITY, this.slot.getAmount());
        }

        @Test
        public void preFill() {
            this.slot.set(Items.GOLD_INGOT, 16);
            assertTrue(this.slot.canInsert(Items.GOLD_INGOT, 48));
            assertEquals(48, this.slot.tryInsert(Items.GOLD_INGOT, 48));
            assertEquals(48, this.slot.insert(Items.GOLD_INGOT, 48));

            assertEquals(16 + 48, this.slot.getAmount());
        }

        @Test
        public void preFill_tag() {
            CompoundTag tag = Utils.generateNbt();
            this.slot.set(Items.GOLD_INGOT, tag, 16);
            assertTrue(this.slot.canInsert(Items.GOLD_INGOT, tag, 48));
            assertEquals(48, this.slot.tryInsert(Items.GOLD_INGOT, tag, 48));
            assertEquals(48, this.slot.insert(Items.GOLD_INGOT, tag, 48));

            assertEquals(16 + 48, this.slot.getAmount());
        }

        @Test
        public void preFill_overCapacity() {
            this.slot.set(Items.GOLD_INGOT, 50);
            assertEquals(14, this.slot.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(14, this.slot.insert(Items.GOLD_INGOT, 16));

            assertEquals(CAPACITY, this.slot.getAmount());
        }

        @Test
        public void preFill_overCapacity_tag() {
            CompoundTag tag = Utils.generateNbt();
            this.slot.set(Items.GOLD_INGOT, tag, 50);
            assertEquals(14, this.slot.tryInsert(Items.GOLD_INGOT, tag, 16));
            assertEquals(14, this.slot.insert(Items.GOLD_INGOT, tag, 16));

            assertEquals(CAPACITY, this.slot.getAmount());
        }
    }
}
