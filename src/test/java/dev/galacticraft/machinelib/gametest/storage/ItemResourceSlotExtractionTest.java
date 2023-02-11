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

package dev.galacticraft.machinelib.gametest.storage;

import dev.galacticraft.machinelib.api.gametest.GameUnitTest;
import dev.galacticraft.machinelib.api.gametest.annotation.UnitTest;
import dev.galacticraft.machinelib.api.storage.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.gametest.Util;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.galacticraft.machinelib.gametest.Assertions.assertEquals;
import static dev.galacticraft.machinelib.gametest.Assertions.assertTrue;

public final class ItemResourceSlotExtractionTest extends GameUnitTest<ItemResourceSlot> {
    public ItemResourceSlotExtractionTest() {
        super("item_resource_slot_extraction", () -> ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.any()));
    }

    @Override
    @GameTestGenerator
    public @NotNull List<TestFunction> generateTests() {
        return super.generateTests();
    }

    @UnitTest
    public void extractFromEmpty(@NotNull ItemResourceSlot slot) {
        assertEquals(0, slot.extract(Items.GOLD_INGOT, new CompoundTag(), 1));
        assertEquals(0, slot.extract(Items.GOLD_INGOT, null, 1));
        assertEquals(0, slot.extract(Items.GOLD_INGOT, 1));
    }

    @UnitTest
    public void extractOne(@NotNull ItemResourceSlot slot) {
        slot.set(Items.GOLD_INGOT, 1);

        assertEquals(1, slot.extract(Items.GOLD_INGOT, 1));
    }

    @UnitTest
    public void extractOneEmptyTag(@NotNull ItemResourceSlot slot) {
        slot.set(Items.GOLD_INGOT, 1);

        assertEquals(1, slot.extract(Items.GOLD_INGOT, new CompoundTag(), 1));
    }

    @UnitTest
    public void extractOneTag(@NotNull ItemResourceSlot slot) {
        CompoundTag tag = Util.generateUniqueNbt();
        slot.set(Items.GOLD_INGOT, tag, 1);

        assertEquals(1, slot.extract(Items.GOLD_INGOT, tag.copy(), 1));
    }

    @UnitTest
    public void extractSupplyUnder(@NotNull ItemResourceSlot slot) {
        slot.set(Items.GOLD_INGOT, 32);

        assertEquals(32, slot.extract(Items.GOLD_INGOT, 48));
    }

    @UnitTest
    public void extractMultipleExact(@NotNull ItemResourceSlot slot) {
        slot.set(Items.GOLD_INGOT, 64);

        assertEquals(64, slot.extract(Items.GOLD_INGOT, 64));
    }

    @UnitTest
    public void extractMultipleExactTag(@NotNull ItemResourceSlot slot) {
        CompoundTag tag = Util.generateUniqueNbt();
        slot.set(Items.GOLD_INGOT, tag, 64);

        assertEquals(64, slot.extract(Items.GOLD_INGOT, tag, 64));
    }

    @UnitTest
    public void extractMultipleExactEmptyTag(@NotNull ItemResourceSlot slot) {
        slot.set(Items.GOLD_INGOT, 64);

        assertEquals(64, slot.extract(Items.GOLD_INGOT, new CompoundTag(), 64));
    }

    @UnitTest
    public void extractMultipleSupplyOver(@NotNull ItemResourceSlot slot) {
        slot.set(Items.GOLD_INGOT, 64);

        assertEquals(16, slot.extract(Items.GOLD_INGOT, 16));
    }

    @UnitTest
    public void extractMultipleSupplyUnder(@NotNull ItemResourceSlot slot) {
        slot.set(Items.GOLD_INGOT, 32);

        assertEquals(32, slot.extract(Items.GOLD_INGOT, 48));
    }

    @UnitTest
    public void extractAny(@NotNull ItemResourceSlot slot) {
        slot.set(Items.GOLD_INGOT, 24);

        assertEquals(24, slot.extract(24));
    }

    @UnitTest
    public void extractOneT(@NotNull ItemResourceSlot slot) {
        slot.set(Items.GOLD_INGOT, 24);

        assertTrue(slot.extractOne(Items.GOLD_INGOT));
        assertTrue(slot.extractOne());
    }

    @UnitTest
    public void extractTypeMismatch(@NotNull ItemResourceSlot slot) {
        slot.set(Items.IRON_INGOT, 16);

        assertEquals(0, slot.extract(Items.GOLD_INGOT, 1));
    }

    @UnitTest
    public void extractNbtTypeMismatch(@NotNull ItemResourceSlot slot) {
        slot.set(Items.GOLD_INGOT, 16);

        assertEquals(0, slot.extract(Items.GOLD_INGOT, Util.generateUniqueNbt(), 1));
    }

    @UnitTest
    public void extractTypeNbtMismatch(@NotNull ItemResourceSlot slot) {
        slot.set(Items.GOLD_INGOT, Util.generateUniqueNbt(), 16);

        assertEquals(0, slot.extract(Items.GOLD_INGOT, null, 1));
    }

    @UnitTest
    public void extractDualNbtTypeMismatch(@NotNull ItemResourceSlot slot) {
        slot.set(Items.GOLD_INGOT, Util.generateUniqueNbt(), 16);

        assertEquals(0, slot.extract(Items.GOLD_INGOT, Util.generateUniqueNbt(), 1));
    }
}
