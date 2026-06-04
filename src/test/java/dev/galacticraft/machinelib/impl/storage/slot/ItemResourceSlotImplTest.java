/*
 * Copyright (c) 2021-2026 Team Galacticraft
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

package dev.galacticraft.machinelib.impl.storage.slot;

import dev.galacticraft.machinelib.api.filter.ResourceFilters;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import dev.galacticraft.machinelib.test.util.Utils;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import team.reborn.energy.api.EnergyStorage;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ItemResourceSlotImplTest extends ResourceSlotImplTest<Item, ItemResourceSlotImpl> {

    ItemResourceSlotImplTest() {
        super(Items.STICK, Items.GLASS);
    }

    @Override
    ItemResourceSlotImpl createSlot() {
        return new ItemResourceSlotImpl(TransferType.OUTPUT, null, ResourceFilters.any(), (int) CAPACITY);
    }

    @Test
    void find() {
        slot.set(resource0, 1);
        assertNull(slot.find(EnergyStorage.ITEM));
    }

    @Test
    void limitedCapacity() {
        assertEquals(Items.EGG.getDefaultMaxStackSize(), slot.getCapacityFor(Items.EGG, DataComponentPatch.EMPTY));

        slot.set(Items.EGG, 1);
        assertEquals(Items.EGG.getDefaultMaxStackSize(), slot.getRealCapacity());
    }

    @Nested
    class OneItem {
        private static final Item resource = Items.LAVA_BUCKET;
        private static final Item remainder = Items.BUCKET;

        @BeforeEach
        void fillHalf() {
            slot.set(resource, 1);
        }

        @Test
        void consumeN() {
            assertEquals(1, slot.consume(1));
            assertEquals(1, slot.getAmount());
            assertEquals(remainder, slot.getResource());
        }

        @Test
        void consume() {
            assertEquals(1, slot.consume(resource, 1));
            assertEquals(1, slot.getAmount());
            assertEquals(remainder, slot.getResource());
        }

        @Test
        void consumeDifferentComponents() {
            assertEquals(0, slot.consume(resource, Utils.generateComponents(), 1));
            assertEquals(1, slot.getAmount());
            assertEquals(resource, slot.getResource());
        }

        @Test
        void consumeOne() {
            assertEquals(resource, slot.consumeOne());
            assertEquals(1, slot.getAmount());
            assertEquals(remainder, slot.getResource());
        }

        @Test
        void consumeOneDifferent() {
            assertFalse(slot.consumeOne(resource0));
            assertEquals(1, slot.getAmount());
            assertEquals(resource, slot.getResource());
        }
    }

    @Nested
    class Recipes {
        private static final ResourceLocation recipe = ResourceLocation.withDefaultNamespace("test");

        @BeforeEach
        void fillHalf() {
            slot.recipeCrafted(recipe);
        }

        @Test
        void takeRecipes() {
            Set<ResourceLocation> recipes = slot.takeRecipes();
            assertNotNull(recipes);
            assertEquals(1, recipes.size());
            assertTrue(recipes.contains(recipe));

            assertNull(slot.takeRecipes());
        }
    }
}
