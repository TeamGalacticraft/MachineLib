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

package dev.galacticraft.gametest.storage.item;

import dev.galacticraft.api.machine.storage.MachineItemStorage;
import dev.galacticraft.api.machine.storage.display.ItemSlotDisplay;
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.machine.storage.io.ResourceType;
import dev.galacticraft.api.machine.storage.io.SlotType;
import dev.galacticraft.gametest.MachineLibGametest;
import dev.galacticraft.impl.machine.Constant;
import dev.galacticraft.impl.machine.storage.MachineItemStorageImpl;
import dev.galacticraft.impl.machine.storage.slot.ItemSlot;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class ItemStorageImplTest implements MachineLibGametest {
    private static final String EMPTY_STRUCTURE = "machinelib-test:empty";

    private static final SlotType<Item, ItemVariant> TEST_SLOT_0 = SlotType.create(
            new Identifier(Constant.MOD_ID, "item_test_slot_0"),
            TextColor.fromRgb(0xFFFFFF),
            new TranslatableText("Slot 0"),
            v -> v.getItem() != Items.BEETROOT_SEEDS,
            ResourceFlow.BOTH,
            ResourceType.ITEM
    );

    private static final SlotType<Item, ItemVariant> TEST_SLOT_1 = SlotType.create(
            new Identifier(Constant.MOD_ID, "item_test_slot_1"), TextColor.fromRgb(0xFFFFFFFE),
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
        assertEquals(2, this.storage.size(), "Item Storage should have 2 slots!");
        assertEquals(1, MachineItemStorage.Builder.create().addSlot(TEST_SLOT_0, new ItemSlotDisplay(0, 0)).build().size(), "Item Storage should have 1 slot!");
        assertEquals(2, MachineItemStorage.Builder.create().addSlot(TEST_SLOT_0, new ItemSlotDisplay(0, 0)).addSlot(TEST_SLOT_0, new ItemSlotDisplay(0, 0)).build().size(), "Item Storage should have 2 slots!");
        assertEquals(0, MachineItemStorage.Builder.create().build().size(), "Item Storage should have 0 slots!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert__generic(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(6, this.storage.insert(ItemVariant.of(Items.POTATO), 6, transaction), "Expected 6 potatoes to be inserted!");
            assertSlotContains(0, ItemVariant.of(Items.POTATO), 6, "Expected inventory to contain 6 potatoes!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert__pass_to_next(@NotNull TestContext context) {
        fillSlotType(0);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(12, this.storage.insert(ItemVariant.of(Items.YELLOW_DYE), 12, transaction), "Expected 12 yellow dye to be inserted!");
            assertSlotContains(1, ItemVariant.of(Items.YELLOW_DYE), 12, "Expected inventory to contain 12 yellow dye!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert__filled(@NotNull TestContext context) {
        setSlot(0, ItemVariant.of(Items.HONEYCOMB), 6);
        fillSlotType(1);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(6, this.storage.insert(ItemVariant.of(Items.HONEYCOMB), 6, transaction), "Expected 6 honeycombs to be inserted!");
            assertSlotContains(0, ItemVariant.of(Items.HONEYCOMB), 12, "Expected inventory to contain 12 honeycombs!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert__type_full_block(@NotNull TestContext context) {
        fillSlotType(0);
        fillSlotType(1);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(ItemVariant.of(Items.MELON), 23, transaction), "Expected 0 melons to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert__cap_full_block(@NotNull TestContext context) {
        setSlot(0, ItemVariant.of(Items.QUARTZ), 64);
        setSlot(1, ItemVariant.of(Items.QUARTZ), 64);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(ItemVariant.of(Items.QUARTZ), 27, transaction), "Expected 0 quartz to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert__filter_block(@NotNull TestContext context) {
        fillSlotType(0);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(ItemVariant.of(Items.DIAMOND), 14, transaction), "Expected 0 diamonds to be inserted!");
            assertSlotContains(1, ItemVariant.blank(), 0, "Expected inventory to contain 0 diamonds!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert__overflow(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(65, this.storage.insert(ItemVariant.of(Items.CARROT), 65, transaction), "Expected 65 carrots to be inserted!");
            assertEquals(0, this.storage.insert(1, ItemVariant.of(Items.AMETHYST_SHARD), 34, transaction), "Expected 0 amethyst shards to be inserted!"); //test to see that it overflowed
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert__overflow_filled(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(7, this.storage.insert(ItemVariant.of(Items.BONE), 7, transaction), "Expected 7 bone to be inserted!");
            assertEquals(65, this.storage.insert(ItemVariant.of(Items.BONE), 65, transaction), "Expected 65 bone to be inserted!");
            assertEquals(0, this.storage.insert(1, ItemVariant.of(Items.AMETHYST_SHARD), 31, transaction), "Expected 0 amethyst shards to be inserted!"); //test to see that it overflowed
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert__overflow_type_full_block(@NotNull TestContext context) {
        setSlot(1, ItemVariant.of(Items.BEACON), 1);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(64, this.storage.insert(ItemVariant.of(Items.ITEM_FRAME), 65, transaction), "Expected 64 item frames to be inserted!");
            assertEquals(3, this.storage.insert(ItemVariant.of(Items.BEACON), 3, transaction), "Expected 3 beacons to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert__overflow_cap_full_block(@NotNull TestContext context) {
        setSlot(1, ItemVariant.of(Items.GRASS), 64);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(64, this.storage.insert(ItemVariant.of(Items.GRASS), 65, transaction), "Expected 64 grass to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert__overflow_filter_block(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(64, this.storage.insert(ItemVariant.of(Items.DIAMOND), 65, transaction), "Expected 64 diamonds to be inserted!");
            assertEquals(5, this.storage.insert(ItemVariant.of(Items.AMETHYST_SHARD), 5, transaction), "Expected 5 amethyst shards to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert__nbt(@NotNull TestContext context) {
        NbtCompound compound = generateRandomNbt();

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(8, this.storage.insert(ItemVariant.of(Items.DIAMOND, compound), 8, transaction), "Expected 8 diamonds with nbt to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert__nbt_fill(@NotNull TestContext context) {
        NbtCompound compound = generateRandomNbt();
        setSlot(0, ItemVariant.of(Items.CALCITE, compound), 9);
        setSlot(1, ItemVariant.of(Items.JUKEBOX, compound), 21);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(2, this.storage.insert(ItemVariant.of(Items.CALCITE, compound), 2, transaction), "Expected 2 calcite with nbt to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert__nbt_fill_block(@NotNull TestContext context) {
        NbtCompound compound = generateRandomNbt();
        setSlot(0, ItemVariant.of(Items.FEATHER, compound), 9);
        fillSlotType(1);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(ItemVariant.of(Items.FEATHER), 4, transaction), "Expected 0 feathers to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert__nbt_fill_block_n(@NotNull TestContext context) {
        NbtCompound compound = generateRandomNbt();
        setSlot(0, ItemVariant.of(Items.WARPED_FUNGUS), 3);
        fillSlotType(1);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(ItemVariant.of(Items.WARPED_FUNGUS, compound), 4, transaction), "Expected 0 warped fungi to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0, maxAttempts = 2)
    void insert__nbt_fill_block_d(@NotNull TestContext context) {
        setSlot(0, ItemVariant.of(Items.LEVER, generateRandomNbt()), 3);
        fillSlotType(1);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(ItemVariant.of(Items.LEVER, generateRandomNbt()), 4, transaction), "Expected 0 levers to be inserted!");
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
        setSlot(0, ItemVariant.of(Items.OAK_BUTTON), 13);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(4, this.storage.insert(0, ItemVariant.of(Items.OAK_BUTTON), 4, transaction), "Expected 4 oak buttons to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_slot__full(@NotNull TestContext context) {
        setSlot(0, ItemVariant.of(Items.WAXED_CUT_COPPER), 64);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(0, ItemVariant.of(Items.WAXED_CUT_COPPER), 4, transaction), "Expected 0 waxed cut copper to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_slot__filled_block(@NotNull TestContext context) {
        fillSlotType(0);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(0, ItemVariant.of(Items.FIRE_CHARGE), 23, transaction), "Expected 0 fire charges to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_slot__filter_block(@NotNull TestContext context) {
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
        NbtCompound compound = generateRandomNbt();

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(25, this.storage.insert(0, ItemVariant.of(Items.FIREWORK_ROCKET, compound), 25, transaction), "Expected 25 firework rockets with nbt to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_slot__nbt_fill(@NotNull TestContext context) {
        NbtCompound compound = generateRandomNbt();
        setSlot(0, ItemVariant.of(Items.PLAYER_HEAD, compound), 22);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(4, this.storage.insert(0, ItemVariant.of(Items.PLAYER_HEAD, compound), 4, transaction), "Expected 4 player heads with nbt to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_slot__nbt_dif(@NotNull TestContext context) {
        setSlot(0, ItemVariant.of(Items.OXEYE_DAISY, generateRandomNbt()), 22);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(0, ItemVariant.of(Items.OXEYE_DAISY, generateRandomNbt()), 5, transaction), "Expected 0 oxeye daisies with nbt to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void insert_slot__nbt_fill_block(@NotNull TestContext context) {
        NbtCompound compound = generateRandomNbt();
        setSlot(0, ItemVariant.of(Items.SMOOTH_QUARTZ, compound), 9);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(0, ItemVariant.of(Items.SMOOTH_QUARTZ), 7, transaction), "Expected 0 smooth quartz to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void extract__generic(@NotNull TestContext context) {
        setSlot(0, ItemVariant.of(Items.DEAD_BUSH), 37);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(37, this.storage.extract(ItemVariant.of(Items.DEAD_BUSH), 37, transaction), "Expected 37 dead bushes to be extracted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void extract__over(@NotNull TestContext context) {
        setSlot(0, ItemVariant.of(Items.GLASS_BOTTLE), 24);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(24, this.storage.extract(ItemVariant.of(Items.GLASS_BOTTLE), 25, transaction), "Expected 24 glass bottles to be extracted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void extract__under(@NotNull TestContext context) {
        setSlot(0, ItemVariant.of(Items.COPPER_INGOT), 9);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(7, this.storage.extract(ItemVariant.of(Items.COPPER_INGOT), 7, transaction), "Expected 7 copper ingots to be extracted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void extract__overflow(@NotNull TestContext context) {
        setSlot(0, ItemVariant.of(Items.ALLIUM), 64);
        setSlot(1, ItemVariant.of(Items.ALLIUM), 9);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(73, this.storage.extract(ItemVariant.of(Items.ALLIUM), 73, transaction), "Expected 73 copper ingots to be extracted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void extract__overflow_over(@NotNull TestContext context) {
        setSlot(0, ItemVariant.of(Items.LAPIS_LAZULI), 64);
        setSlot(1, ItemVariant.of(Items.LAPIS_LAZULI), 23);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(87, this.storage.extract(ItemVariant.of(Items.LAPIS_LAZULI), 88, transaction), "Expected 87 lapis lazuli to be extracted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void extract__overflow_under(@NotNull TestContext context) {
        setSlot(0, ItemVariant.of(Items.BLAZE_POWDER), 64);
        setSlot(1, ItemVariant.of(Items.BLAZE_POWDER), 9);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(65, this.storage.extract(ItemVariant.of(Items.BLAZE_POWDER), 65, transaction), "Expected 65 blaze powder to be extracted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void extract__nbt(@NotNull TestContext context) {
        NbtCompound compound = generateRandomNbt();
        setSlot(0, ItemVariant.of(Items.GREEN_DYE, compound), 13);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(10, this.storage.extract(ItemVariant.of(Items.GREEN_DYE, compound), 10, transaction), "Expected 10 green dye to be extracted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void extract__nbt_n_block(@NotNull TestContext context) {
        NbtCompound compound = generateRandomNbt();
        setSlot(0, ItemVariant.of(Items.STRIPPED_ACACIA_LOG, compound), 13);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.extract(ItemVariant.of(Items.STRIPPED_ACACIA_LOG), 42, transaction), "Expected 0 stripped acacia logs to be extracted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void extract__nbt_d_block(@NotNull TestContext context) {
        setSlot(0, ItemVariant.of(Items.BEEF, generateRandomNbt()), 13);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.extract(ItemVariant.of(Items.BEEF, generateRandomNbt()), 56, transaction), "Expected 0 beef to be extracted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void extract__nbt_n_o_block(@NotNull TestContext context) {
        NbtCompound compound = generateRandomNbt();
        setSlot(0, ItemVariant.of(Items.CRAFTING_TABLE), 13);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.extract(ItemVariant.of(Items.CRAFTING_TABLE, compound), 13, transaction), "Expected 0 crafting tables to be extracted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void extract__nbt_d(@NotNull TestContext context) {
        NbtCompound nbt = generateRandomNbt();
        setSlot(0, ItemVariant.of(Items.PISTON, nbt), 13);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(13, this.storage.extract(ItemVariant.of(Items.PISTON, nbt), 13, transaction), "Expected 13 pistons to be extracted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void mod_count__initial(@NotNull TestContext context) {
        assertEquals(0, this.storage.getModCount(), "Item Storage should not be modified, as no transaction has occurred!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void mod_count__transaction_fail(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertThrowsExactly(IllegalStateException.class, () -> this.storage.getModCount(), "Expected ModCount access to fail during a transaction!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void mod_count__transaction_cancel(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(32, this.storage.insert(0, ItemVariant.of(Items.SAND), 32, transaction), "Items should have been inserted into slot 0!");
        }
        assertEquals(0, this.storage.getModCount(), "Item Storage should not be modified, as the transaction was aborted!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void mod_count__transaction_commit(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(44, this.storage.insert(0, ItemVariant.of(Items.STICK), 44, transaction), "Items should have been inserted into slot 0!");
            transaction.commit();
        }
        assertEquals(1, this.storage.getModCount(), "Item Storage should have been modified, as the transaction was committed!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void empty__initial(@NotNull TestContext context) {
        assertTrue(this.storage.isEmpty(), "Item Storage should be empty!");
        assertEquals(2, this.storage.insert(0, ItemVariant.of(Items.IRON_ORE), 2, null), "Items should have been inserted into slot 0!");
        assertFalse(this.storage.isEmpty(), "Item Storage should not be empty!");
        assertEquals(2, this.storage.extract(0, ItemVariant.of(Items.IRON_ORE), 2, null), "Items should have been extracted from slot 0!");
        assertTrue(this.storage.isEmpty(), "Item Storage should be empty!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void empty__inserted(@NotNull TestContext context) {
        assertEquals(2, this.storage.insert(0, ItemVariant.of(Items.COOKIE), 2, null), "Items should have been inserted into slot 0!");
        assertFalse(this.storage.isEmpty(), "Item Storage should not be empty!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void empty__extracted(@NotNull TestContext context) {
        assertEquals(2, this.storage.insert(0, ItemVariant.of(Items.BLACK_CONCRETE_POWDER), 2, null), "Items should have been inserted into slot 0!");
        assertEquals(2, this.storage.extract(0, ItemVariant.of(Items.BLACK_CONCRETE_POWDER), 2, null), "Items should have been extracted from slot 0!");
        assertTrue(this.storage.isEmpty(), "Item Storage should be empty!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void empty__extract_partial(@NotNull TestContext context) {
        assertEquals(6, this.storage.insert(0, ItemVariant.of(Items.ORANGE_DYE), 6, null), "Items should have been inserted into slot 0!");
        assertEquals(5, this.storage.extract(0, ItemVariant.of(Items.ORANGE_DYE), 5, null), "Items should have been extracted from slot 0!");
        assertFalse(this.storage.isEmpty(), "Item Storage should not be empty!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void getStack(@NotNull TestContext context) {
        // Should always return the EMPTY stack
        assertSame(ItemStack.EMPTY, this.storage.getStack(0), "Expected identity empty stack in empty inventory!");
        setSlot(0, ItemVariant.of(Items.GOLDEN_SHOVEL), 1);
        assertEquals(new ItemStack(Items.GOLDEN_SHOVEL, 1), this.storage.getStack(0), "Expected 1 golden shovel in storage!");
        setSlot(0, ItemVariant.blank(), 0);
        assertSame(ItemStack.EMPTY, this.storage.getStack(0), "Expected identity empty stack after extraction!");
        setSlot(0, ItemVariant.of(Items.ACACIA_SAPLING), 4);
        assertEquals(new ItemStack(Items.ACACIA_SAPLING, 4), this.storage.getStack(0), "Expected 4 acacia saplings in storage!");
        setSlot(0, ItemVariant.blank(), 0);
        assertSame(ItemStack.EMPTY, this.storage.getStack(0), "Expected identity empty stack after extraction!");
    }

    private static @NotNull NbtCompound generateRandomNbt() {
        NbtCompound compound = new NbtCompound();
        compound.putUuid("id", UUID.randomUUID());
        return compound;
    }

    private void setSlot(int index, ItemVariant variant, long amount) {
        ItemSlot slot = this.storage.getSlot(index);
        slot.variant = variant;
        slot.amount = amount;
    }

    private void assertSlotContains(int index, @NotNull ItemVariant variant, long amount, String message) {
        ItemSlot slot = this.storage.getSlot(index);
        if (!variant.equals(slot.variant)) {
            throw new GameTestException(format(message, variant, slot.variant, 1));
        } else if (amount != slot.amount) {
            throw new GameTestException(format(message, amount, slot.amount, 1));
        }
    }

    private void fillSlotType(int index) {
        fillSlot(index, 1);
    }

    private void fillSlot(int index, long amount) {
        ItemSlot slot = this.storage.getSlot(index);
        slot.variant = ItemVariant.of(Items.BARRIER);
        slot.amount = amount;
    }
}