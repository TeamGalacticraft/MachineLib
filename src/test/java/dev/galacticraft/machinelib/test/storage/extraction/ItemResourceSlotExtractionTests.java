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

import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.api.transfer.InputType;
import dev.galacticraft.machinelib.filter.ResourceFilters;
import dev.galacticraft.machinelib.impl.storage.slot.ResourceSlotImpl;
import dev.galacticraft.machinelib.test.JUnitTest;
import dev.galacticraft.machinelib.test.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public sealed class ItemResourceSlotExtractionTests implements JUnitTest {
    protected ItemResourceSlot slot;

    @BeforeEach
    public void setup() {
        this.slot = ItemResourceSlot.create(InputType.STORAGE, ItemSlotDisplay.create(0, 0), ResourceFilters.any());
    }

    @AfterEach
    public void verify() {
        assertTrue(((ResourceSlotImpl<?>) this.slot).isSane());
    }

    public static final class ExtractionFailureTests extends ItemResourceSlotExtractionTests {
        @Test
        public void empty() {
            assertTrue(this.slot.isEmpty());

            assertFalse(this.slot.canExtract(1));
            assertEquals(0, this.slot.tryExtract(1));
            assertEquals(0, this.slot.extract(1));

            assertFalse(this.slot.canExtract(Items.GOLD_INGOT, 1));
            assertEquals(0, this.slot.tryExtract(Items.GOLD_INGOT, 1));
            assertEquals(0, this.slot.extract(Items.GOLD_INGOT, 1));

            assertFalse(this.slot.canExtract(Items.GOLD_INGOT, Utils.EMPTY_NBT, 1));
            assertEquals(0, this.slot.tryExtract(Items.GOLD_INGOT, Utils.EMPTY_NBT, 1));
            assertEquals(0, this.slot.extract(Items.GOLD_INGOT, Utils.EMPTY_NBT, 1));

            assertFalse(this.slot.canExtract(Items.GOLD_INGOT, Utils.generateNbt(), 1));
            assertEquals(0, this.slot.tryExtract(Items.GOLD_INGOT, Utils.generateNbt(), 1));
            assertEquals(0, this.slot.extract(Items.GOLD_INGOT, Utils.generateNbt(), 1));

            assertFalse(this.slot.extractOne());
        }

        @Test
        public void incorrectType() {
            this.slot.set(Items.IRON_INGOT, 1);

            assertFalse(this.slot.canExtract(Items.GOLD_INGOT, 1));
            assertEquals(0, this.slot.tryExtract(Items.GOLD_INGOT, 1));
            assertEquals(0, this.slot.extract(Items.GOLD_INGOT, 1));
            assertFalse(this.slot.extractOne(Items.GOLD_INGOT));

            assertFalse(this.slot.isEmpty());
        }

        @Test
        public void extractionTag() {
            this.slot.set(Items.GOLD_INGOT, 1);

            assertFalse(this.slot.canExtract(Items.GOLD_INGOT, Utils.generateNbt(), 1));
            assertEquals(0, this.slot.tryExtract(Items.GOLD_INGOT, Utils.generateNbt(), 1));
            assertEquals(0, this.slot.extract(Items.GOLD_INGOT, Utils.generateNbt(), 1));
            assertFalse(this.slot.extractOne(Items.GOLD_INGOT, Utils.generateNbt()));

            assertFalse(this.slot.isEmpty());
        }

        @Test
        public void containedTag() {
            this.slot.set(Items.GOLD_INGOT, Utils.generateNbt(), 1);

            assertFalse(this.slot.canExtract(Items.GOLD_INGOT, null, 1));
            assertEquals(0, this.slot.tryExtract(Items.GOLD_INGOT, null, 1));
            assertEquals(0, this.slot.extract(Items.GOLD_INGOT, null, 1));
            assertFalse(this.slot.extractOne(Items.GOLD_INGOT, null));

            assertFalse(this.slot.isEmpty());
        }

        @Test
        public void mismatchedTag() {
            this.slot.set(Items.GOLD_INGOT, Utils.generateNbt(), 1);

            assertFalse(this.slot.canExtract(Items.GOLD_INGOT, Utils.generateNbt(), 1));
            assertEquals(0, this.slot.tryExtract(Items.GOLD_INGOT, Utils.generateNbt(), 1));
            assertEquals(0, this.slot.extract(Items.GOLD_INGOT, Utils.generateNbt(), 1));
            assertFalse(this.slot.extractOne(Items.GOLD_INGOT, Utils.generateNbt()));

            assertFalse(this.slot.isEmpty());
        }
    }

    public static final class ExtractionSuccessTests extends ItemResourceSlotExtractionTests {
        @Test
        public void exact_any() {
            this.slot.set(Items.GOLD_INGOT, 1);

            assertTrue(this.slot.canExtract(1));
            assertEquals(1, this.slot.tryExtract(1));
            assertEquals(1, this.slot.extract(1));

            assertTrue(this.slot.isEmpty());
        }

        @Test
        public void exact_typed() {
            this.slot.set(Items.GOLD_INGOT, 1);

            assertTrue(this.slot.canExtract(Items.GOLD_INGOT, 1));
            assertEquals(1, this.slot.tryExtract(Items.GOLD_INGOT, 1));
            assertEquals(1, this.slot.extract(Items.GOLD_INGOT, 1));

            assertTrue(this.slot.isEmpty());
        }

        @Test
        public void exact_typed_nbt() {
            CompoundTag tag = Utils.generateNbt();
            this.slot.set(Items.GOLD_INGOT, tag, 1);

            assertTrue(this.slot.canExtract(Items.GOLD_INGOT, tag, 1));
            assertEquals(1, this.slot.tryExtract(Items.GOLD_INGOT, tag, 1));
            assertEquals(1, this.slot.extract(Items.GOLD_INGOT, tag, 1));

            assertTrue(this.slot.isEmpty());
        }

        @Test
        public void exact_typed_emptyNbt() {
            this.slot.set(Items.GOLD_INGOT, 1);

            assertTrue(this.slot.canExtract(Items.GOLD_INGOT, Utils.EMPTY_NBT, 1));
            assertEquals(1, this.slot.tryExtract(Items.GOLD_INGOT, Utils.EMPTY_NBT, 1));
            assertEquals(1, this.slot.extract(Items.GOLD_INGOT, Utils.EMPTY_NBT, 1));

            assertTrue(this.slot.isEmpty());
        }

        @Test
        public void excess_any() {
            this.slot.set(Items.GOLD_INGOT, 48);

            assertTrue(this.slot.canExtract(16));
            assertEquals(16, this.slot.tryExtract(16));
            assertEquals(16, this.slot.extract(16));

            assertEquals(this.slot.getAmount(), 48 - 16);
        }

        @Test
        public void excess_typed() {
            this.slot.set(Items.GOLD_INGOT, 48);

            assertTrue(this.slot.canExtract(Items.GOLD_INGOT, 16));
            assertEquals(16, this.slot.tryExtract(Items.GOLD_INGOT, 16));
            assertEquals(16, this.slot.extract(Items.GOLD_INGOT, 16));

            assertEquals(this.slot.getAmount(), 48 - 16);
        }

        @Test
        public void excess_typed_nbt() {
            CompoundTag tag = Utils.generateNbt();
            this.slot.set(Items.GOLD_INGOT, tag, 48);

            assertTrue(this.slot.canExtract(Items.GOLD_INGOT, tag, 16));
            assertEquals(16, this.slot.tryExtract(Items.GOLD_INGOT, tag, 16));
            assertEquals(16, this.slot.extract(Items.GOLD_INGOT, tag, 16));

            assertEquals(this.slot.getAmount(), 48 - 16);
        }

        @Test
        public void excess_typed_emptyNbt() {
            this.slot.set(Items.GOLD_INGOT, 48);

            assertTrue(this.slot.canExtract(Items.GOLD_INGOT, Utils.EMPTY_NBT, 16));
            assertEquals(16, this.slot.tryExtract(Items.GOLD_INGOT, Utils.EMPTY_NBT, 16));
            assertEquals(16, this.slot.extract(Items.GOLD_INGOT, Utils.EMPTY_NBT, 16));

            assertEquals(this.slot.getAmount(), 48 - 16);
        }

        @Test
        public void insufficient_any() {
            this.slot.set(Items.GOLD_INGOT, 16);

            assertEquals(16, this.slot.tryExtract(64));
            assertEquals(16, this.slot.extract(64));

            assertTrue(this.slot.isEmpty());
        }

        @Test
        public void insufficient_typed() {
            this.slot.set(Items.GOLD_INGOT, 16);

            assertEquals(16, this.slot.tryExtract(Items.GOLD_INGOT, 64));
            assertEquals(16, this.slot.extract(Items.GOLD_INGOT, 64));

            assertTrue(this.slot.isEmpty());
        }

        @Test
        public void insufficient_typed_nbt() {
            CompoundTag tag = Utils.generateNbt();
            this.slot.set(Items.GOLD_INGOT, tag, 16);

            assertEquals(16, this.slot.tryExtract(Items.GOLD_INGOT, tag, 64));
            assertEquals(16, this.slot.extract(Items.GOLD_INGOT, tag, 64));

            assertTrue(this.slot.isEmpty());
        }

        @Test
        public void insufficient_typed_emptyNbt() {
            this.slot.set(Items.GOLD_INGOT, 16);

            assertEquals(16, this.slot.tryExtract(Items.GOLD_INGOT, Utils.EMPTY_NBT, 64));
            assertEquals(16, this.slot.extract(Items.GOLD_INGOT, Utils.EMPTY_NBT, 64));

            assertTrue(this.slot.isEmpty());
        }
    }
}
