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
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import dev.galacticraft.machinelib.gametest.Util;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.galacticraft.machinelib.gametest.Assertions.assertEquals;
import static dev.galacticraft.machinelib.gametest.Assertions.assertTrue;

public final class FluidResourceSlotExtractionTest extends GameUnitTest<FluidResourceSlot> {
    public FluidResourceSlotExtractionTest() {
        super("fluid_resource_slot_extraction", () -> FluidResourceSlot.create(TankDisplay.create(0, 0), 81000 * 10, ResourceFilters.any()));
    }

    @Override
    @GameTestGenerator
    public @NotNull List<TestFunction> generateTests() {
        return super.generateTests();
    }

    @UnitTest
    public void extractFromEmpty(@NotNull FluidResourceSlot slot) {
        assertEquals(0, slot.extract(Fluids.WATER, new CompoundTag(), 1));
        assertEquals(0, slot.extract(Fluids.WATER, null, 1));
        assertEquals(0, slot.extract(Fluids.WATER, 1));
    }

    @UnitTest
    public void extractOne(@NotNull FluidResourceSlot slot) {
        slot.set(Fluids.WATER, 1);

        assertEquals(1, slot.extract(Fluids.WATER, 1));
    }

    @UnitTest
    public void extractOneEmptyTag(@NotNull FluidResourceSlot slot) {
        slot.set(Fluids.WATER, 1);

        assertEquals(1, slot.extract(Fluids.WATER, new CompoundTag(), 1));
    }

    @UnitTest
    public void extractOneTag(@NotNull FluidResourceSlot slot) {
        CompoundTag tag = Util.generateUniqueNbt();
        slot.set(Fluids.WATER, tag, 1);

        assertEquals(1, slot.extract(Fluids.WATER, tag.copy(), 1));
    }

    @UnitTest
    public void extractSupplyUnder(@NotNull FluidResourceSlot slot) {
        slot.set(Fluids.WATER, 40500);

        assertEquals(40500, slot.extract(Fluids.WATER, 60750));
    }

    @UnitTest
    public void extractMultipleExact(@NotNull FluidResourceSlot slot) {
        slot.set(Fluids.WATER, 81000);

        assertEquals(81000, slot.extract(Fluids.WATER, 81000));
    }

    @UnitTest
    public void extractMultipleExactTag(@NotNull FluidResourceSlot slot) {
        CompoundTag tag = Util.generateUniqueNbt();
        slot.set(Fluids.WATER, tag, 81000);

        assertEquals(81000, slot.extract(Fluids.WATER, tag, 81000));
    }

    @UnitTest
    public void extractMultipleExactEmptyTag(@NotNull FluidResourceSlot slot) {
        slot.set(Fluids.WATER, 81000);

        assertEquals(81000, slot.extract(Fluids.WATER, new CompoundTag(), 81000));
    }

    @UnitTest
    public void extractMultipleSupplyOver(@NotNull FluidResourceSlot slot) {
        slot.set(Fluids.WATER, 81000);

        assertEquals(20250, slot.extract(Fluids.WATER, 20250));
    }

    @UnitTest
    public void extractMultipleSupplyUnder(@NotNull FluidResourceSlot slot) {
        slot.set(Fluids.WATER, 40500);

        assertEquals(40500, slot.extract(Fluids.WATER, 60750));
    }

    @UnitTest
    public void extractAny(@NotNull FluidResourceSlot slot) {
        slot.set(Fluids.WATER, 30375);

        assertEquals(30375, slot.extract(30375));
    }

    @UnitTest
    public void extractOneT(@NotNull FluidResourceSlot slot) {
        slot.set(Fluids.WATER, 30375);

        assertTrue(slot.extractOne(Fluids.WATER));
        assertTrue(slot.extractOne());
    }

    @UnitTest
    public void extractTypeMismatch(@NotNull FluidResourceSlot slot) {
        slot.set(Fluids.LAVA, 20250);

        assertEquals(0, slot.extract(Fluids.WATER, 1));
    }

    @UnitTest
    public void extractNbtTypeMismatch(@NotNull FluidResourceSlot slot) {
        slot.set(Fluids.WATER, 20250);

        assertEquals(0, slot.extract(Fluids.WATER, Util.generateUniqueNbt(), 1));
    }

    @UnitTest
    public void extractTypeNbtMismatch(@NotNull FluidResourceSlot slot) {
        slot.set(Fluids.WATER, Util.generateUniqueNbt(), 20250);

        assertEquals(0, slot.extract(Fluids.WATER, null, 1));
    }

    @UnitTest
    public void extractDualNbtTypeMismatch(@NotNull FluidResourceSlot slot) {
        slot.set(Fluids.WATER, Util.generateUniqueNbt(), 20250);

        assertEquals(0, slot.extract(Fluids.WATER, Util.generateUniqueNbt(), 1));
    }
}
