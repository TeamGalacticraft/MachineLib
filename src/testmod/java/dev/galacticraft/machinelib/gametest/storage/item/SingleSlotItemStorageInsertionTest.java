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
import dev.galacticraft.machinelib.testmod.TestMod;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.galacticraft.machinelib.gametest.Assertions.assertEquals;
import static dev.galacticraft.machinelib.gametest.Assertions.assertThrows;
import static dev.galacticraft.machinelib.gametest.Util.generateNbt;
import static net.minecraft.world.item.Items.*;

public final class SingleSlotItemStorageInsertionTest implements MachineLibGametest {
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

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert$fail_nothing(@NotNull GameTestHelper context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertThrows(() -> this.storage.insert(ItemVariant.blank(), 5, transaction));
            assertThrows(() -> this.storage.insert(variant(GOLD_INGOT), -1, transaction));
        }
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert$empty(@NotNull GameTestHelper context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(5, this.storage.insert(variant(GOLD_INGOT), 5, transaction));
            assertEquals(5, this.storage.count(variant(GOLD_INGOT)));
        }
        assertEquals(0, this.storage.count(variant(GOLD_INGOT)));
    }


    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert$empty_nbt(@NotNull GameTestHelper context) {
        CompoundTag tag = generateNbt();
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(8, this.storage.insert(variant(GOLD_INGOT, tag), 8, transaction));
            assertEquals(8, this.storage.count(variant(GOLD_INGOT, tag)));
        }
        assertEquals(0, this.storage.count(variant(GOLD_INGOT, tag)));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert$empty_overflow(@NotNull GameTestHelper context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(64, this.storage.insert(variant(GOLD_INGOT), 65, transaction));
            assertEquals(64, this.storage.count(variant(GOLD_INGOT)));
        }
        assertEquals(0, this.storage.count(variant(GOLD_INGOT)));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert$empty_overflow_item(@NotNull GameTestHelper context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(1, this.storage.insert(variant(IRON_AXE), 64, transaction));
            assertEquals(1, this.storage.count(variant(IRON_AXE)));
        }
        assertEquals(0, this.storage.count(variant(IRON_AXE)));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert$filled(@NotNull GameTestHelper context) {
        this.storage.setSlot(0, variant(GOLD_INGOT), 1);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(1, this.storage.insert(variant(GOLD_INGOT), 4, transaction));
            assertEquals(5, this.storage.count(variant(GOLD_INGOT)));
        }
        assertEquals(0, this.storage.count(variant(GOLD_INGOT)));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert$filled_type_full(@NotNull GameTestHelper context) {
        this.storage.setSlot(0, variant(IRON_INGOT), 1);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(variant(GOLD_INGOT), 4, transaction));
            assertEquals(1, this.storage.count(variant(IRON_INGOT)));
        }
        assertEquals(1, this.storage.count(variant(IRON_INGOT)));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert$filled_nbt_type_full(@NotNull GameTestHelper context) {
        CompoundTag tag = generateNbt();
        this.storage.setSlot(0, variant(GOLD_INGOT, tag), 1);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(variant(GOLD_INGOT), 4, transaction));
            assertEquals(1, this.storage.count(variant(GOLD_INGOT, tag)));
        }
        assertEquals(1, this.storage.count(variant(GOLD_INGOT, tag)));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert$filled_type_full_nbt(@NotNull GameTestHelper context) {
        CompoundTag tag = generateNbt();
        this.storage.setSlot(0, variant(GOLD_INGOT), 1);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(variant(GOLD_INGOT, tag), 4, transaction));
            assertEquals(1, this.storage.count(variant(GOLD_INGOT)));
        }
        assertEquals(1, this.storage.count(variant(GOLD_INGOT)));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert$filled_type_full_different_nbt(@NotNull GameTestHelper context) {
        CompoundTag tag1 = generateNbt();
        CompoundTag tag2 = generateNbt();
        this.storage.setSlot(0, variant(GOLD_INGOT, tag2), 1);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(variant(GOLD_INGOT, tag1), 4, transaction));
            assertEquals(1, this.storage.count(variant(GOLD_INGOT, tag2)));
        }
        assertEquals(1, this.storage.count(variant(GOLD_INGOT, tag2)));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert$filled_count_full(@NotNull GameTestHelper context) {
        this.storage.setSlot(0, variant(GOLD_INGOT), 64);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(variant(GOLD_INGOT), 8, transaction));
            assertEquals(64, this.storage.count(variant(GOLD_INGOT)));
        }
        assertEquals(64, this.storage.count(variant(GOLD_INGOT)));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert$filled_overflow(@NotNull GameTestHelper context) {
        this.storage.setSlot(0, variant(GOLD_INGOT), 8);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(56, this.storage.insert(variant(GOLD_INGOT), 64, transaction));
            assertEquals(64, this.storage.count(variant(GOLD_INGOT)));
        }
        assertEquals(8, this.storage.count(variant(GOLD_INGOT)));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert$filled_overflow_item(@NotNull GameTestHelper context) {
        this.storage.setSlot(0, variant(IRON_AXE), 1);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(variant(IRON_AXE), 64, transaction));
            assertEquals(1, this.storage.count(variant(IRON_AXE)));
        }
        assertEquals(0, this.storage.count(variant(IRON_AXE)));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert$filter(@NotNull GameTestHelper context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(variant(DIAMOND), 8, transaction));
            assertEquals(0, this.storage.count(variant(DIAMOND)));
        }
        assertEquals(0, this.storage.count(variant(DIAMOND)));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert$filterNbt(@NotNull GameTestHelper context) {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("blocked", true);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(variant(IRON_INGOT, nbt), 8, transaction));
            assertEquals(0, this.storage.count(variant(IRON_INGOT, nbt)));
        }
        assertEquals(0, this.storage.count(variant(IRON_INGOT, nbt)));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert_slot$empty(@NotNull GameTestHelper context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(5, this.storage.insert(0, variant(GOLD_INGOT), 5, transaction));
            assertEquals(5, this.storage.getAmount(0));
            assertEquals(variant(GOLD_INGOT), this.storage.getVariant(0));
        }
        assertEquals(0, this.storage.getAmount(0));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert_slot$empty_nbt(@NotNull GameTestHelper context) {
        CompoundTag tag = generateNbt();
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(8, this.storage.insert(0, variant(GOLD_INGOT, tag), 8, transaction));
            assertEquals(8, this.storage.getAmount(0));
            assertEquals(variant(GOLD_INGOT, tag), this.storage.getVariant(0));
        }
        assertEquals(0, this.storage.getAmount(0));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert_slot$empty_overflow(@NotNull GameTestHelper context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(64, this.storage.insert(0, variant(GOLD_INGOT), 65, transaction));
            assertEquals(64, this.storage.getAmount(0));
            assertEquals(variant(GOLD_INGOT), this.storage.getVariant(0));
        }
        assertEquals(0, this.storage.getAmount(0));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert_slot$empty_overflow_item(@NotNull GameTestHelper context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(1, this.storage.insert(0, variant(IRON_AXE), 64, transaction));
            assertEquals(1, this.storage.getAmount(0));
            assertEquals(variant(IRON_AXE), this.storage.getVariant(0));
        }
        assertEquals(0, this.storage.getAmount(0));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert_slot$filled(@NotNull GameTestHelper context) {
        this.storage.setSlot(0, variant(GOLD_INGOT), 1);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(1, this.storage.insert(0, variant(GOLD_INGOT), 4, transaction));
            assertEquals(5, this.storage.getAmount(0));
            assertEquals(variant(GOLD_INGOT), this.storage.getVariant(0));
        }
        assertEquals(0, this.storage.getAmount(0));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert_slot$filled_type_full(@NotNull GameTestHelper context) {
        this.storage.setSlot(0, variant(IRON_INGOT), 1);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(0, variant(GOLD_INGOT), 4, transaction));
            assertEquals(1, this.storage.getAmount(0));
            assertEquals(variant(GOLD_INGOT), this.storage.getVariant(0));
        }
        assertEquals(1, this.storage.getAmount(0));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert_slot$filled_nbt_type_full(@NotNull GameTestHelper context) {
        CompoundTag tag = generateNbt();
        this.storage.setSlot(0, variant(GOLD_INGOT, tag), 1);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(0, variant(GOLD_INGOT), 4, transaction));
            assertEquals(1, this.storage.getAmount(0));
            assertEquals(variant(GOLD_INGOT, tag), this.storage.getVariant(0));
        }
        assertEquals(1, this.storage.getAmount(0));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert_slot$filled_type_full_nbt(@NotNull GameTestHelper context) {
        CompoundTag tag = generateNbt();
        this.storage.setSlot(0, variant(GOLD_INGOT), 1);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(0, variant(GOLD_INGOT, tag), 4, transaction));
            assertEquals(1, this.storage.getAmount(0));
            assertEquals(variant(GOLD_INGOT), this.storage.getVariant(0));
        }
        assertEquals(1, this.storage.getAmount(0));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert_slot$filled_type_full_different_nbt(@NotNull GameTestHelper context) {
        CompoundTag tag1 = generateNbt();
        CompoundTag tag2 = generateNbt();
        this.storage.setSlot(0, variant(GOLD_INGOT, tag2), 1);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(0, variant(GOLD_INGOT, tag1), 4, transaction));
            assertEquals(1, this.storage.getAmount(0));
            assertEquals(variant(GOLD_INGOT, tag2), this.storage.getVariant(0));
        }
        assertEquals(1, this.storage.getAmount(0));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert_slot$filled_count_full(@NotNull GameTestHelper context) {
        this.storage.setSlot(0, variant(GOLD_INGOT), 64);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(0, variant(GOLD_INGOT), 8, transaction));
            assertEquals(64, this.storage.getAmount(0));
            assertEquals(variant(GOLD_INGOT), this.storage.getVariant(0));
        }
        assertEquals(64, this.storage.getAmount(0));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert_slot$filled_overflow(@NotNull GameTestHelper context) {
        this.storage.setSlot(0, variant(GOLD_INGOT), 8);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(56, this.storage.insert(0, variant(GOLD_INGOT), 64, transaction));
            assertEquals(64, this.storage.getAmount(0));
            assertEquals(variant(GOLD_INGOT), this.storage.getVariant(0));
        }
        assertEquals(8, this.storage.getAmount(0));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert_slot$filled_overflow_item(@NotNull GameTestHelper context) {
        this.storage.setSlot(0, variant(IRON_AXE), 1);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(0, variant(IRON_AXE), 64, transaction));
            assertEquals(1, this.storage.getAmount(0));
            assertEquals(variant(IRON_AXE), this.storage.getVariant(0));
        }
        assertEquals(0, this.storage.getAmount(0));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert_slot$filter(@NotNull GameTestHelper context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(0, variant(DIAMOND), 8, transaction));
            assertEquals(0, this.storage.getAmount(0));
            assertEquals(variant(DIAMOND), this.storage.getVariant(0));
        }
        assertEquals(0, this.storage.getAmount(0));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage", timeoutTicks = 0)
    void insert_slot$filterNbt(@NotNull GameTestHelper context) {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("blocked", true);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(0, variant(IRON_INGOT, nbt), 8, transaction));
            assertEquals(0, this.storage.getAmount(0));
            assertEquals(variant(IRON_INGOT, nbt), this.storage.getVariant(0));
        }
        assertEquals(0, this.storage.getAmount(0));
    }

    private static @NotNull ItemVariant variant(@NotNull Item item, @Nullable CompoundTag nbt) {
        return ItemVariant.of(item, nbt);
    }
    
    private static @NotNull ItemVariant variant(@NotNull Item item) {
        return ItemVariant.of(item);
    }
}
