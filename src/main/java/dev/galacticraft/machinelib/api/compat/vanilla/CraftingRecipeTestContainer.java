/*
 * Copyright (c) 2021-2024 Team Galacticraft
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

package dev.galacticraft.machinelib.api.compat.vanilla;

import com.google.common.collect.Iterators;
import dev.galacticraft.machinelib.api.storage.SlottedStorageAccess;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.util.ItemStackUtil;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A container for testing recipes using the vanilla recipe system.
 *
 * @see RecipeTestContainer
 * @see CraftingContainer
 */
public class CraftingRecipeTestContainer extends RecipeTestContainer implements CraftingContainer {
    /**
     * The width of the crafting grid
     */
    private final int width;
    /**
     * The height of the crafting grid
     */
    private final int height;

    /**
     * Creates a new container with the given slots.
     *
     * @param slots the slots to use
     * @return a new test container
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    public static @NotNull CraftingRecipeTestContainer create(int width, int height, ItemResourceSlot @NotNull ... slots) {
        assert slots.length > 0 && slots.length == width * height;
        return new CraftingRecipeTestContainer(width, height, slots);
    }

    /**
     * Creates a new container with the slots specified by a slice of the given storage access
     *
     * @param access the storage access providing the slots
     * @param start the index of the first slot to include in the container
     * @param len the number of slots to include in the container
     * @return a new test container
     */
    @Contract(value = "_, _, _, _, _-> new", pure = true)
    public static @NotNull CraftingRecipeTestContainer create(int width, int height, SlottedStorageAccess<Item, ItemResourceSlot> access, int start, int len) {
        assert len == width * height;
        Iterator<ItemResourceSlot> iterator = access.iterator();
        Iterators.advance(iterator, start);
        ItemResourceSlot[] slots = new ItemResourceSlot[len];
        for (int i = 0; i < len; i++) {
            slots[i] = iterator.next();
        }
        return new CraftingRecipeTestContainer(width, height, slots);
    }

    /**
     * Constructs a new RecipeTestContainer with the provided slots.
     *
     * @param width  the width of the crafting grid
     * @param height the height of the crafting grid
     * @param slots  the slots to be included in the container
     */
    private CraftingRecipeTestContainer(int width, int height, ItemResourceSlot[] slots) {
        super(slots);
        this.width = width;
        this.height = height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public @NotNull List<ItemStack> getItems() {
        List<ItemStack> list = new ArrayList<>(this.slots.length);
        for (ItemResourceSlot slot : this.slots) {
            list.add(ItemStackUtil.create(slot));
        }
        return list;
    }

    @Override
    public void fillStackedContents(StackedContents finder) {
    }
}
