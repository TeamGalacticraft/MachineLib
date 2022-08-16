/*
 * Copyright (c) 2021-2022 Team Galacticraft
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

package dev.galacticraft.machinelib.gametest.storage.item;

import dev.galacticraft.api.machine.storage.MachineItemStorage;
import dev.galacticraft.api.machine.storage.display.ItemSlotDisplay;
import dev.galacticraft.impl.machine.storage.MachineItemStorageImpl;
import dev.galacticraft.machinelib.gametest.MachineLibGametest;
import dev.galacticraft.machinelib.gametest.Util;
import dev.galacticraft.machinelib.testmod.TestMod;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.galacticraft.machinelib.gametest.Assertions.assertEquals;
import static net.minecraft.world.item.Items.GOLD_INGOT;
import static net.minecraft.world.item.Items.IRON_INGOT;

public final class SingleSlotItemStorageExtractionTest implements MachineLibGametest {
    private MachineItemStorageImpl storage;

    @Override
    public void beforeEach(@NotNull GameTestHelper context) {
        this.storage = (MachineItemStorageImpl) MachineItemStorage.Builder.create()
                .addSlot(TestMod.NO_DIAMOND_SLOT, 64, new ItemSlotDisplay(0, 0))
                .build();
    }

    @Override
    public void afterEach(@NotNull GameTestHelper context) {
        this.storage = null;
    }

    private void extract$empty_fail() {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.extract(variant(GOLD_INGOT), 1, transaction));
        }
    }

    private void extract$single() {
        this.storage.setSlot(0, variant(GOLD_INGOT), 1);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(1, this.storage.extract(variant(GOLD_INGOT), 1, transaction));
        }
    }

    private void extract$multiple() {
        this.storage.setSlot(0, variant(GOLD_INGOT), 32);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(16, this.storage.extract(variant(GOLD_INGOT), 16, transaction));
        }
    }

    private void extract$multiple_exact() {
        this.storage.setSlot(0, variant(GOLD_INGOT), 16);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(16, this.storage.extract(variant(GOLD_INGOT), 16, transaction));
        }
    }

    private void extract$type_fail() {
        this.storage.setSlot(0, variant(IRON_INGOT), 32);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.extract(variant(GOLD_INGOT), 16, transaction));
        }
    }

    private void extract$type_fail_nbt() {
        this.storage.setSlot(0, variant(GOLD_INGOT), 32);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.extract(variant(GOLD_INGOT, Util.generateNbt()), 16, transaction));
        }
    }

    private void extract$nbt_type_fail() {
        this.storage.setSlot(0, variant(GOLD_INGOT, Util.generateNbt()), 32);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.extract(variant(GOLD_INGOT), 16, transaction));
        }
    }

    private void extract$different_nbt_type_fail() {
        this.storage.setSlot(0, variant(GOLD_INGOT, Util.generateNbt()), 32);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.extract(variant(GOLD_INGOT, Util.generateNbt()), 16, transaction));
        }
    }

    private void extract$overflow() {
        this.storage.setSlot(0, variant(GOLD_INGOT), 5);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(5, this.storage.extract(variant(GOLD_INGOT), 10, transaction));
        }
    }

    private static @NotNull ItemVariant variant(@NotNull Item item, @Nullable CompoundTag nbt) {
        return ItemVariant.of(item, nbt);
    }
    
    private static @NotNull ItemVariant variant(@NotNull Item item) {
        return ItemVariant.of(item);
    }
}
