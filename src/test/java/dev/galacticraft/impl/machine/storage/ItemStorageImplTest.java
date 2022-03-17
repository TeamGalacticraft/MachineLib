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

import dev.galacticraft.api.machine.storage.ItemStorage;
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.machine.storage.io.ResourceType;
import dev.galacticraft.api.machine.storage.io.SlotType;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TextColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemStorageImplTest {

    private ItemStorageImpl storage;

    @BeforeEach
    void setUp() {
        this.storage = ItemStorage.Builder.create()
                .addSlot(SlotType.create(
                        TextColor.fromRgb(0xFFFFFF),
                        new LiteralText("Slot 0"),
                        v -> true,
                        ResourceFlow.BOTH,
                        ResourceType.ITEM
                ))
                .addSlot(SlotType.create(
                        TextColor.fromRgb(0xFFFFFF),
                        new LiteralText("Slot 1"),
                        v -> v.getItem() != Items.DIAMOND,
                        ResourceFlow.BOTH,
                        ResourceType.ITEM
                ))
                .build();
    }

    @Test
    void size() {
        assertEquals(2, this.storage.size(), "Inventory should have exactly two slots!");
    }

    @Test
    void getModCount() {
        assertEquals(0, this.storage.getModCount(), "Inventory should not be modified");
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(3, this.storage.insert(0, ItemVariant.of(Items.DIAMOND), 3, transaction), "Items should have been inserted into slot 0!");
            assertEquals(1, this.storage.getModCount(), "Inventory should have been modified!");
            //abort transaction
        }
        assertEquals(0, this.storage.getModCount(), "Inventory should not be modified, as the transaction was aborted!");
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(2, this.storage.insert(0, ItemVariant.of(Items.EXPERIENCE_BOTTLE), 2, transaction), "Items should have been inserted into slot 0!");
            assertEquals(1, this.storage.getModCount(), "Inventory should have been modified!");
            transaction.commit();
        }
        assertEquals(1, this.storage.getModCount(), "Inventory should have been modified, as the transaction was committed!");
    }

    @Test
    void isEmpty() {
        assertTrue(this.storage.isEmpty(), "Inventory should be empty!");
        assertEquals(2, this.storage.insert(0, ItemVariant.of(Items.IRON_ORE), 2, null), "Items should have been inserted into slot 0!");
        assertFalse(this.storage.isEmpty(), "Inventory should not be empty!");
        assertEquals(2, this.storage.extract(0, ItemVariant.of(Items.IRON_ORE), 2, null), "Items should have been extracted from slot 0!");
        assertTrue(this.storage.isEmpty(), "Inventory should be empty!");
    }

    @Test
    void getStack() {
        // Should always return the EMPTY stack
        assertSame(ItemStack.EMPTY, this.storage.getStack(0), "Expected identity empty stack!");
        assertEquals(1, this.storage.insert(0, ItemVariant.of(Items.GOLDEN_SHOVEL), 1, null), "Items should have been inserted into slot 0!");
        assertEquals(new ItemStack(Items.GOLDEN_SHOVEL, 1), this.storage.getStack(0), "Expected 1 golden shovel in inventory!");
        assertEquals(1, this.storage.extract(0, ItemVariant.of(Items.GOLDEN_SHOVEL), 1, null), "Items should have been extracted from slot 0!");
        assertSame(ItemStack.EMPTY, this.storage.getStack(0), "Expected identity empty stack!");
    }

    @Test
    void insert() {
    }

    @Test
    void extract() {
    }

    @Test
    void iterator() {
    }

    @Test
    void replace() {
    }

    @Test
    void testInsert() {
    }

    @Test
    void canAccept() {
    }
}