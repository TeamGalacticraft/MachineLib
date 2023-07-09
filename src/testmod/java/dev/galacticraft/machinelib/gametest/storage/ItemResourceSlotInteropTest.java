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
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.galacticraft.machinelib.gametest.Assertions.assertEquals;

public final class ItemResourceSlotInteropTest extends GameUnitTest<ItemResourceSlot> {
    public ItemResourceSlotInteropTest() {
        super("item_resource_slot_interop_test", () -> ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.any()));
    }

    @Override
    @GameTestGenerator
    public @NotNull List<TestFunction> generateTests() {
        return super.generateTests();
    }

    @UnitTest
    public void exchangeSingle(@NotNull ItemResourceSlot slot) {
        slot.set(Items.WATER_BUCKET, 1);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(1, slot.exchange(ItemVariant.of(Items.BUCKET), 1, transaction));
        }
    }

    @UnitTest
    public void exchangeFailFull(@NotNull ItemResourceSlot slot) {
        slot.set(Items.GOLD_INGOT, 2);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, slot.exchange(ItemVariant.of(Items.GOLD_BLOCK), 1, transaction));
        }
    }

    @UnitTest
    public void exchangeFailInsufficient(@NotNull ItemResourceSlot slot) {
        slot.set(Items.WATER_BUCKET, 1);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, slot.exchange(ItemVariant.of(Items.BUCKET), 2, transaction));
        }
    }

    @UnitTest
    public void exchangeEqual(@NotNull ItemResourceSlot slot) {
        slot.set(Items.OAK_SAPLING, 48);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(12, slot.exchange(ItemVariant.of(Items.OAK_SAPLING), 12, transaction));
        }
    }
}
