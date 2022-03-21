/*
 * Copyright (c) 2019-2022 Team Galacticraft
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

package dev.galacticraft.impl.machine.storage;

import dev.galacticraft.api.machine.storage.MachineItemStorage;
import dev.galacticraft.api.machine.storage.display.ItemSlotDisplay;
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.machine.storage.io.ResourceType;
import dev.galacticraft.api.machine.storage.io.SlotType;
import dev.galacticraft.gametest.MachineLibGametest;
import dev.galacticraft.impl.machine.Constant;
import dev.galacticraft.impl.machine.storage.slot.ItemSlot;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public final class ItemStorageImplTest implements MachineLibGametest {
    private static final String EMPTY_STRUCTURE = "machinelib-test:empty";

    private static final SlotType<Item, ItemVariant> TEST_SLOT_0 = SlotType.create(
            new Identifier(Constant.MOD_ID, "test_slot_0"),
            TextColor.fromRgb(0xFFFFFF),
            new TranslatableText("Slot 0"),
            v -> v.getItem() != Items.BEETROOT_SEEDS,
            ResourceFlow.BOTH,
            ResourceType.ITEM
    );
    private static final SlotType<Item, ItemVariant> TEST_SLOT_1 = SlotType.create(
            new Identifier(Constant.MOD_ID, "test_slot_1"), TextColor.fromRgb(0xFFFFFF),
            new TranslatableText("Slot 1"),
            v -> v.getItem() != Items.DIAMOND,
            ResourceFlow.BOTH,
            ResourceType.ITEM
    );

    private MachineItemStorageImpl storage;

    @Override
    public void beforeEach(TestContext context) {
        this.storage = MachineItemStorage.Builder.create()
                .addSlot(TEST_SLOT_0, new ItemSlotDisplay(0, 0))
                .addSlot(TEST_SLOT_1, new ItemSlotDisplay(0, 16))
                .build();
    }

    @Override
    public void afterEach(TestContext context) {
        this.storage = null;
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void size(@NotNull TestContext context) {
        assertEquals(2, this.storage.size(), "Item Storage should have exactly 2 slots!");
        assertEquals(1, MachineItemStorage.Builder.create().addSlot(TEST_SLOT_0, new ItemSlotDisplay(0, 0)).build().size(), "Item Storage should have 1 slot!");
        assertEquals(0, MachineItemStorage.Builder.create().build().size(), "Item Storage should have 0 slots!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_no_slot__generic(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(6, this.storage.insert(ItemVariant.of(Items.HONEYCOMB), 6, transaction), "Expected 6 honeycombs to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_no_slot__pass_to_next(@NotNull TestContext context) {
        ItemSlot slot = this.storage.getSlot(0);
        slot.variant = ItemVariant.of(Items.BAMBOO);
        slot.amount = 4;
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(12, this.storage.insert(ItemVariant.of(Items.YELLOW_DYE), 12, transaction), "Expected 12 yellow dye to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_no_slot__filled(@NotNull TestContext context) {
        ItemSlot slot = this.storage.getSlot(0);
        slot.variant = ItemVariant.of(Items.HONEYCOMB);
        slot.amount = 6;
        slot = this.storage.getSlot(1);
        slot.variant = ItemVariant.of(Items.PAINTING);
        slot.amount = 2;
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(6, this.storage.insert(ItemVariant.of(Items.HONEYCOMB), 6, transaction), "Expected 6 honeycombs to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_no_slot__full(@NotNull TestContext context) {
        ItemSlot slot = this.storage.getSlot(0);
        slot.variant = ItemVariant.of(Items.QUARTZ);
        slot.amount = 64;
        slot = this.storage.getSlot(1);
        slot.variant = ItemVariant.of(Items.LANTERN);
        slot.amount = 2;
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(ItemVariant.of(Items.QUARTZ), 35, transaction), "Expected 0 honeycombs to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_no_slot__filled_block(@NotNull TestContext context) {
        ItemSlot slot = this.storage.getSlot(0);
        slot.variant = ItemVariant.of(Items.BEACON);
        slot.amount = 7;
        slot = this.storage.getSlot(1);
        slot.variant = ItemVariant.of(Items.KELP);
        slot.amount = 8;
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(ItemVariant.of(Items.PAPER), 20, transaction), "Expected 0 paper to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_no_slot__filter_block(@NotNull TestContext context) {
        ItemSlot slot = this.storage.getSlot(0);
        slot.variant = ItemVariant.of(Items.BEACON);
        slot.amount = 3;
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(ItemVariant.of(Items.DIAMOND), 14, transaction), "Expected 0 diamonds to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_no_slot__overflow(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(65, this.storage.insert(ItemVariant.of(Items.HONEYCOMB), 65, transaction), "Expected 65 honeycombs to be inserted!");
            assertEquals(0, this.storage.insert(1, ItemVariant.of(Items.AMETHYST_SHARD), 34, transaction), "Expected 0 amethyst shards to be inserted!"); //test to see that it overflowed
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_no_slot__overflow_filled(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(7, this.storage.insert(ItemVariant.of(Items.HONEYCOMB), 7, transaction), "Expected 7 honeycombs to be inserted!");
            assertEquals(65, this.storage.insert(ItemVariant.of(Items.HONEYCOMB), 65, transaction), "Expected 65 honeycombs to be inserted!");
            assertEquals(0, this.storage.insert(1, ItemVariant.of(Items.AMETHYST_SHARD), 31, transaction), "Expected 0 amethyst shards to be inserted!"); //test to see that it overflowed
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_no_slot__overflow_full_block(@NotNull TestContext context) {
        ItemSlot slot = this.storage.getSlot(1);
        slot.variant = ItemVariant.of(Items.BEACON);
        slot.amount = 1;
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(64, this.storage.insert(ItemVariant.of(Items.HONEYCOMB), 65, transaction), "Expected 64 honeycombs to be inserted!");
            assertEquals(3, this.storage.insert(ItemVariant.of(Items.BEACON), 3, transaction), "Expected 3 beacons to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_no_slot__overflow_filter_block(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(64, this.storage.insert(ItemVariant.of(Items.DIAMOND), 65, transaction), "Expected 64 diamonds to be inserted!");
            assertEquals(5, this.storage.insert(ItemVariant.of(Items.AMETHYST_SHARD), 5, transaction), "Expected 5 amethyst shards to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_no_slot__nbt(@NotNull TestContext context) {
        NbtCompound compound = new NbtCompound();
        compound.putString("key", "value");
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(8, this.storage.insert(ItemVariant.of(Items.DIAMOND, compound), 8, transaction), "Expected 8 diamonds with nbt to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_no_slot__nbt_fill(@NotNull TestContext context) {
        NbtCompound compound = new NbtCompound();
        compound.putString("key", "value");
        ItemSlot slot = this.storage.getSlot(0);
        slot.variant = ItemVariant.of(Items.CALCITE, compound);
        slot.amount = 9;
        slot = this.storage.getSlot(1);
        slot.variant = ItemVariant.of(Items.JUKEBOX);
        slot.amount = 21;

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(2, this.storage.insert(ItemVariant.of(Items.CALCITE, compound), 2, transaction), "Expected 2 calcite with nbt to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_no_slot__nbt_fill_block(@NotNull TestContext context) {
        NbtCompound compound = new NbtCompound();
        compound.putString("key", "value");
        ItemSlot slot = this.storage.getSlot(0);
        slot.variant = ItemVariant.of(Items.FEATHER, compound);
        slot.amount = 9;
        slot = this.storage.getSlot(1);
        slot.variant = ItemVariant.of(Items.ALLIUM);
        slot.amount = 1;

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(ItemVariant.of(Items.FEATHER), 4, transaction), "Expected 0 feathers to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_slot__generic(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(39, this.storage.insert(0, ItemVariant.of(Items.CARVED_PUMPKIN), 39, transaction), "Expected 39 carved pumpkins to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_slot__filled(@NotNull TestContext context) {
        ItemSlot slot = this.storage.getSlot(0);
        slot.variant = ItemVariant.of(Items.OAK_BUTTON);
        slot.amount = 13;
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(4, this.storage.insert(0, ItemVariant.of(Items.OAK_BUTTON), 4, transaction), "Expected 4 oak buttons to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_slot__full(@NotNull TestContext context) {
        ItemSlot slot = this.storage.getSlot(0);
        slot.variant = ItemVariant.of(Items.WAXED_CUT_COPPER);
        slot.amount = 64;
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(0, ItemVariant.of(Items.WAXED_CUT_COPPER), 4, transaction), "Expected 0 waxed cut copper to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_slot__filled_block(@NotNull TestContext context) {
        ItemSlot slot = this.storage.getSlot(0);
        slot.variant = ItemVariant.of(Items.JUNGLE_LOG);
        slot.amount = 7;
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(0, ItemVariant.of(Items.FIRE_CHARGE), 23, transaction), "Expected 0 fire charges to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_slot__filter_block(@NotNull TestContext context) {
        ItemSlot slot = this.storage.getSlot(1);
        slot.variant = ItemVariant.of(Items.PODZOL);
        slot.amount = 3;
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(1, ItemVariant.of(Items.DIAMOND), 5, transaction), "Expected 0 diamonds to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_slot__overflow(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(64, this.storage.insert(0, ItemVariant.of(Items.CHORUS_FRUIT), 65, transaction), "Expected 64 chorus fruit to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_slot__overflow_full(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(13, this.storage.insert(0, ItemVariant.of(Items.TARGET), 13, transaction), "Expected 13 targets to be inserted!");
            assertEquals(51, this.storage.insert(0, ItemVariant.of(Items.TARGET), 64, transaction), "Expected 51 targets to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_slot__nbt(@NotNull TestContext context) {
        NbtCompound compound = new NbtCompound();
        compound.putString("key", "value");
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(25, this.storage.insert(0, ItemVariant.of(Items.FIREWORK_ROCKET, compound), 25, transaction), "Expected 25 firework rockets with nbt to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_slot__nbt_fill(@NotNull TestContext context) {
        NbtCompound compound = new NbtCompound();
        compound.putString("key", "value");
        ItemSlot slot = this.storage.getSlot(0);
        slot.variant = ItemVariant.of(Items.PLAYER_HEAD, compound);
        slot.amount = 22;
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(4, this.storage.insert(0, ItemVariant.of(Items.PLAYER_HEAD, compound), 4, transaction), "Expected 4 player heads with nbt to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_slot__nbt_fill_block(@NotNull TestContext context) {
        NbtCompound compound = new NbtCompound();
        compound.putString("key", "value");
        ItemSlot slot = this.storage.getSlot(0);
        slot.variant = ItemVariant.of(Items.SMOOTH_QUARTZ, compound);
        slot.amount = 9;
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(0, ItemVariant.of(Items.SMOOTH_QUARTZ), 7, transaction), "Expected 0 smooth quartz to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void extract(@NotNull TestContext context) {

    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void getModCount(@NotNull TestContext context) {
        assertEquals(0, this.storage.getModCount(), "Item Storage should not be modified, as no transaction has occurred!");
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(3, this.storage.insert(0, ItemVariant.of(Items.DIAMOND), 3, transaction), "Items should have been inserted into slot 0!");
            assertThrows(IllegalStateException.class, () -> this.storage.getModCount(), "Expected ModCount access to fail during a transaction!");
        }
        assertEquals(0, this.storage.getModCount(), "Item Storage should not be modified, as the transaction was aborted!");
        try (Transaction transaction = Transaction.openOuter()) {
            assertThrows(IllegalStateException.class, () -> this.storage.getModCount(), "Expected ModCount access to fail during a transaction!");
            assertEquals(2, this.storage.insert(0, ItemVariant.of(Items.EXPERIENCE_BOTTLE), 2, transaction), "Items should have been inserted into slot 0!");
            transaction.commit();
        }
        assertEquals(1, this.storage.getModCount(), "Item Storage should have been modified, as the transaction was committed!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void isEmpty(@NotNull TestContext context) {
        assertTrue(this.storage.isEmpty(), "Item Storage should be empty!");
        assertEquals(2, this.storage.insert(0, ItemVariant.of(Items.IRON_ORE), 2, null), "Items should have been inserted into slot 0!");
        assertFalse(this.storage.isEmpty(), "Item Storage should not be empty!");
        assertEquals(2, this.storage.extract(0, ItemVariant.of(Items.IRON_ORE), 2, null), "Items should have been extracted from slot 0!");
        assertTrue(this.storage.isEmpty(), "Item Storage should be empty!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void getStack(@NotNull TestContext context) {
        // Should always return the EMPTY stack
        assertSame(ItemStack.EMPTY, this.storage.getStack(0), "Expected identity empty stack in empty inventory!");
        assertEquals(1, this.storage.insert(0, ItemVariant.of(Items.GOLDEN_SHOVEL), 1, null), "A gold shovel should have been inserted into slot 0!");
        assertEquals(new ItemStack(Items.GOLDEN_SHOVEL, 1), this.storage.getStack(0), "Expected 1 golden shovel in Item Storage!");
        assertEquals(1, this.storage.extract(0, ItemVariant.of(Items.GOLDEN_SHOVEL), 1, null), "A gold shovel should have been extracted from slot 0!");
        assertSame(ItemStack.EMPTY, this.storage.getStack(0), "Expected identity empty stack after extraction!");
        assertEquals(4, this.storage.insert(0, ItemVariant.of(Items.ACACIA_SAPLING), 4, null), "Acacia saplings should have been inserted into slot 0!");
        assertEquals(new ItemStack(Items.ACACIA_SAPLING, 4), this.storage.getStack(0), "Expected 4 acacia saplings in Item Storage!");
        assertEquals(4, this.storage.extract(0, ItemVariant.of(Items.ACACIA_SAPLING), 4, null), "Acacia saplings should have been extracted from slot 0!");
        assertSame(ItemStack.EMPTY, this.storage.getStack(0), "Expected identity empty stack after extraction!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void iterator(@NotNull TestContext context) {
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void replace(@NotNull TestContext context) {
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void testInsert(@NotNull TestContext context) {
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void canAccept(@NotNull TestContext context) {
    }
}