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

package dev.galacticraft.machinelib.api.util;

import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for instantiating item stacks.
 */
public class ItemStackUtil {
    /**
     * Prevent instantiation of this utility class.
     */
    private ItemStackUtil() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

    /**
     * Creates a new item stack from the contents of a slot.
     *
     * @param slot the slot to read from
     * @return a new item stack
     */
    public static @NotNull ItemStack create(ResourceSlot<Item> slot) {
        if (slot.isEmpty()) return ItemStack.EMPTY;
        assert slot.getResource() != null && slot.getAmount() > 0 && slot.getAmount() < Integer.MAX_VALUE;
        return new ItemStack(slot.getResource().builtInRegistryHolder(), (int) slot.getAmount(), slot.getComponents());
    }

    /**
     * Creates a new item stack from the specified item, components, and amount.
     *
     * @param resource the item to create the stack from
     * @param components the components to include in the stack
     * @param amount the amount of the item to include in the stack
     * @return a new item stack
     */
    public static ItemStack of(@Nullable Item resource, @NotNull DataComponentPatch components, int amount) {
        if (resource == null) return ItemStack.EMPTY;
        assert amount > 0;
        return new ItemStack(resource.builtInRegistryHolder(), amount, components);
    }

    /**
     * Creates a new item stack from the specified item and amount.
     *
     * @param resource the item to create the stack from
     * @param amount the amount of the item to include in the stack
     * @return a new item stack
     */
    public static ItemStack of(@Nullable Item resource, int amount) {
        if (resource == null) return ItemStack.EMPTY;
        assert amount > 0;
        return new ItemStack(resource, amount);
    }
}
