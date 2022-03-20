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
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.machine.storage.io.ResourceType;
import dev.galacticraft.api.machine.storage.io.SlotType;
import dev.galacticraft.gametest.MachineLibGametest;
import dev.galacticraft.impl.machine.Constant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
            v -> true,
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
                .addSlot(TEST_SLOT_0)
                .addSlot(TEST_SLOT_1)
                .build();
    }

    @Override
    public void afterEach(TestContext context) {
        this.storage = null;
    }

    @GameTest(structureName = EMPTY_STRUCTURE, tickLimit = 0)
    void size(@NotNull TestContext context) {
        assertEquals(context, 2, this.storage.size(), "Item Storage should have exactly two slots!");
        assertEquals(context, 0, MachineItemStorage.Builder.create().build().size(), "Item Storage should have no slots!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, tickLimit = 0)
    void getModCount(@NotNull TestContext context) {
        assertEquals(context, 0, this.storage.getModCount(), "Item Storage should not be modified, as no transaction has occurred!");
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(context, 3, this.storage.insert(0, ItemVariant.of(Items.DIAMOND), 3, transaction), "Items should have been inserted into slot 0!");
            //abort transaction
        }
        assertEquals(context, 0, this.storage.getModCount(), "Item Storage should not be modified, as the transaction was aborted!");
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(context, 2, this.storage.insert(0, ItemVariant.of(Items.EXPERIENCE_BOTTLE), 2, transaction), "Items should have been inserted into slot 0!");
            transaction.commit();
        }
        assertEquals(context, 1, this.storage.getModCount(), "Item Storage should have been modified, as the transaction was committed!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, tickLimit = 0)
    void isEmpty(@NotNull TestContext context) {
        assertTrue(context, this.storage.isEmpty(), "Item Storage should be empty!");
        assertEquals(context, 2, this.storage.insert(0, ItemVariant.of(Items.IRON_ORE), 2, null), "Items should have been inserted into slot 0!");
        assertFalse(context, this.storage.isEmpty(), "Item Storage should not be empty!");
        assertEquals(context, 2, this.storage.extract(0, ItemVariant.of(Items.IRON_ORE), 2, null), "Items should have been extracted from slot 0!");
        assertTrue(context, this.storage.isEmpty(), "Item Storage should be empty!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, tickLimit = 0)
    void getStack(@NotNull TestContext context) {
        // Should always return the EMPTY stack
        assertSame(context, ItemStack.EMPTY, this.storage.getStack(0), "Expected identity empty stack in empty inventory!");
        assertEquals(context, 1, this.storage.insert(0, ItemVariant.of(Items.GOLDEN_SHOVEL), 1, null), "Items should have been inserted into slot 0!");
        assertEquals(context, new ItemStack(Items.GOLDEN_SHOVEL, 1), this.storage.getStack(0), "Expected 1 golden shovel in Item Storage!");
        assertEquals(context, 1, this.storage.extract(0, ItemVariant.of(Items.GOLDEN_SHOVEL), 1, null), "Items should have been extracted from slot 0!");
        assertSame(context, ItemStack.EMPTY, this.storage.getStack(0), "Expected identity empty stack after extraction!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, tickLimit = 0)
    void insert(@NotNull TestContext context) {
    }

    @GameTest(structureName = EMPTY_STRUCTURE, tickLimit = 0)
    void extract(@NotNull TestContext context) {
    }

    @GameTest(structureName = EMPTY_STRUCTURE, tickLimit = 0)
    void iterator(@NotNull TestContext context) {
    }

    @GameTest(structureName = EMPTY_STRUCTURE, tickLimit = 0)
    void replace(@NotNull TestContext context) {
    }

    @GameTest(structureName = EMPTY_STRUCTURE, tickLimit = 0)
    void testInsert(@NotNull TestContext context) {
    }

    @GameTest(structureName = EMPTY_STRUCTURE, tickLimit = 0)
    void canAccept(@NotNull TestContext context) {
    }
}