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

package dev.galacticraft.machinelib.api.util;

import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemStackUtil {
    public static @NotNull ItemStack create(ResourceSlot<Item> slot) {
        if (slot.isEmpty()) return ItemStack.EMPTY;
        assert slot.getResource() != null && slot.getAmount() < Integer.MAX_VALUE;
        ItemStack stack = new ItemStack(slot.getResource(), (int) slot.getAmount());
        stack.setTag(slot.getTag());
        return stack;
    }

    public static @NotNull ItemStack copy(ResourceSlot<Item> slot) {
        if (slot.isEmpty()) return ItemStack.EMPTY;
        assert slot.getResource() != null && slot.getAmount() < Integer.MAX_VALUE;
        ItemStack stack = new ItemStack(slot.getResource(), (int) slot.getAmount());
        stack.setTag(slot.copyTag());
        return stack;
    }
}
