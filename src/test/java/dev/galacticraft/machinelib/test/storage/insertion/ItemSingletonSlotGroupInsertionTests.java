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

import dev.galacticraft.machinelib.api.storage.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.impl.storage.slot.ResourceSlotImpl;
import dev.galacticraft.machinelib.test.JUnitTest;
import dev.galacticraft.machinelib.test.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public sealed class ItemSingletonSlotGroupInsertionTests implements JUnitTest {
    private static final int CAPACITY = 64;
    private static final CompoundTag FILTERED_TAG = Utils.generateNbt();
    protected SlotGroup<Item, ItemStack, ItemResourceSlot> group;

    @BeforeEach
    public void setup() {
        this.group = SlotGroup.ofItem(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.not(ResourceFilters.ofNBT(FILTERED_TAG))));
    }

    @AfterEach
    public void verify() {
        assertTrue(((ResourceSlotImpl<?, ?>) this.group.getSlot(0)).isSane());
    }

    public static final class InsertionFailureTests extends ItemSingletonSlotGroupInsertionTests {
        @Test
        public void full() {
            this.group.getSlot(0).set(Items.GOLD_INGOT, CAPACITY);

            assertFalse(this.group.canInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, this.group.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, this.group.insert(Items.GOLD_INGOT, 16));
            assertEquals(0, this.group.insertStack(new ItemStack(Items.GOLD_INGOT, 16)));
        }

        @Test
        public void incorrectType() {
            this.group.getSlot(0).set(Items.IRON_INGOT, 16);

            assertFalse(this.group.canInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, this.group.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, this.group.insert(Items.GOLD_INGOT, 16));
            assertEquals(0, this.group.insertStack(new ItemStack(Items.GOLD_INGOT, 16)));

            assertEquals(this.group.getAmount(0), 16);
        }

        @Test
        public void filtered() {
            assertTrue(this.group.canInsert(Items.GOLD_INGOT));

            assertFalse(this.group.canInsert(Items.GOLD_INGOT, FILTERED_TAG, 16));
            assertEquals(0, this.group.tryInsert(Items.GOLD_INGOT, FILTERED_TAG, 16));
            assertEquals(0, this.group.insert(Items.GOLD_INGOT, FILTERED_TAG, 16));
            assertEquals(0, this.group.insertStack(Utils.itemStack(Items.GOLD_INGOT, FILTERED_TAG, 16)));

            assertTrue(group.isEmpty());
        }

        @Test
        public void insertTag() {
            this.group.getSlot(0).set(Items.GOLD_INGOT, 16);

            assertFalse(this.group.canInsert(Items.GOLD_INGOT, Utils.generateNbt(), 16));
            assertEquals(0, this.group.tryInsert(Items.GOLD_INGOT, Utils.generateNbt(), 16));
            assertEquals(0, this.group.insert(Items.GOLD_INGOT, Utils.generateNbt(), 16));
            assertEquals(0, this.group.insertStack(Utils.itemStack(Items.GOLD_INGOT, Utils.generateNbt(), 16)));

            assertEquals(this.group.getAmount(0), 16);
        }

        @Test
        public void containedTag() {
            this.group.getSlot(0).set(Items.GOLD_INGOT, Utils.generateNbt(), 16);

            assertFalse(this.group.canInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, this.group.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, this.group.insert(Items.GOLD_INGOT, 16));
            assertEquals(0, this.group.insertStack(new ItemStack(Items.GOLD_INGOT, 16)));

            assertEquals(this.group.getAmount(0), 16);
        }

        @Test
        public void mismatchedTag() {
            this.group.getSlot(0).set(Items.GOLD_INGOT, Utils.generateNbt(), 16);

            assertFalse(this.group.canInsert(Items.GOLD_INGOT, Utils.generateNbt(), 16));
            assertEquals(0, this.group.tryInsert(Items.GOLD_INGOT, Utils.generateNbt(), 16));
            assertEquals(0, this.group.insert(Items.GOLD_INGOT, Utils.generateNbt(), 16));
            assertEquals(0, this.group.insertStack(Utils.itemStack(Items.GOLD_INGOT, Utils.generateNbt(), 16)));

            assertEquals(this.group.getAmount(0), 16);
        }
    }

    public static final class InsertionSuccessTests extends ItemSingletonSlotGroupInsertionTests {
        @Test
        public void empty() {
            assertTrue(this.group.canInsert(Items.GOLD_INGOT, 16));
            assertEquals(16, this.group.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(16, this.group.insert(Items.GOLD_INGOT, 16));

            assertEquals(16, this.group.getAmount(0));
        }

        @Test
        public void empty_stack() {
            assertEquals(16, this.group.insertStack(new ItemStack(Items.GOLD_INGOT, 16)));

            assertEquals(16, this.group.getAmount(0));
        }

        @Test
        public void toCapacity() {
            assertTrue(this.group.canInsert(Items.GOLD_INGOT, CAPACITY));
            assertEquals(CAPACITY, this.group.tryInsert(Items.GOLD_INGOT, CAPACITY));
            assertEquals(CAPACITY, this.group.insert(Items.GOLD_INGOT, CAPACITY));

            assertEquals(CAPACITY, this.group.getAmount(0));
        }

        @Test
        public void toCapacity_stack() {
            assertEquals(CAPACITY, this.group.insertStack(new ItemStack(Items.GOLD_INGOT, CAPACITY)));

            assertEquals(CAPACITY, this.group.getAmount(0));
        }

        @Test
        public void overCapacity() {
            assertEquals(CAPACITY, this.group.tryInsert(Items.GOLD_INGOT, CAPACITY + 8));
            assertEquals(CAPACITY, this.group.insert(Items.GOLD_INGOT, CAPACITY + 8));

            assertEquals(CAPACITY, this.group.getAmount(0));
        }

        @Test
        public void overCapacity_stack() {
            assertEquals(CAPACITY, this.group.insertStack(new ItemStack(Items.GOLD_INGOT, CAPACITY + 8)));

            assertEquals(CAPACITY, this.group.getAmount(0));
        }

        @Test
        public void preFill() {
            this.group.getSlot(0).set(Items.GOLD_INGOT, 16);
            assertTrue(this.group.canInsert(Items.GOLD_INGOT, 48));
            assertEquals(48, this.group.tryInsert(Items.GOLD_INGOT, 48));
            assertEquals(48, this.group.insert(Items.GOLD_INGOT, 48));

            assertEquals(16 + 48, this.group.getAmount(0));
        }

        @Test
        public void preFill_tag() {
            CompoundTag tag = Utils.generateNbt();
            this.group.getSlot(0).set(Items.GOLD_INGOT, tag, 16);
            assertTrue(this.group.canInsert(Items.GOLD_INGOT, tag, 48));
            assertEquals(48, this.group.tryInsert(Items.GOLD_INGOT, tag, 48));
            assertEquals(48, this.group.insert(Items.GOLD_INGOT, tag, 48));

            assertEquals(16 + 48, this.group.getAmount(0));
        }

        @Test
        public void preFill_stack() {
            CompoundTag tag = Utils.generateNbt();
            this.group.getSlot(0).set(Items.GOLD_INGOT, tag, 16);
            assertEquals(48, this.group.insertStack(Utils.itemStack(Items.GOLD_INGOT, tag, 48)));

            assertEquals(16 + 48, this.group.getAmount(0));
        }

        @Test
        public void preFill_overCapacity() {
            this.group.getSlot(0).set(Items.GOLD_INGOT, 50);
            assertEquals(14, this.group.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(14, this.group.insert(Items.GOLD_INGOT, 16));

            assertEquals(CAPACITY, this.group.getAmount(0));
        }

        @Test
        public void preFill_overCapacity_tag() {
            CompoundTag tag = Utils.generateNbt();
            this.group.getSlot(0).set(Items.GOLD_INGOT, tag, 50);
            assertEquals(14, this.group.tryInsert(Items.GOLD_INGOT, tag, 16));
            assertEquals(14, this.group.insert(Items.GOLD_INGOT, tag, 16));

            assertEquals(CAPACITY, this.group.getAmount(0));
        }

        @Test
        public void preFill_overCapacity_stack() {
            CompoundTag tag = Utils.generateNbt();
            this.group.getSlot(0).set(Items.GOLD_INGOT, tag, 50);

            assertEquals(14, this.group.insertStack(Utils.itemStack(Items.GOLD_INGOT, tag, 16)));

            assertEquals(CAPACITY, this.group.getAmount(0));
        }
    }
}