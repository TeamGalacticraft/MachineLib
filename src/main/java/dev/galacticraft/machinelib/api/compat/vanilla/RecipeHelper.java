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

package dev.galacticraft.machinelib.api.compat.vanilla;

import dev.galacticraft.machinelib.api.storage.SlottedStorageAccess;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.util.ItemStackUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RecipeHelper {
    public static @NotNull CraftingInput craftingInput(int width, int height, SlottedStorageAccess<Item, ItemResourceSlot> storage) {
        List<ItemStack> items = new ArrayList<>(width * height);
        for (int i = 0; i < storage.size(); i++) {
            ItemResourceSlot slot = storage.slot(i);
            if (slot.transferMode().isInput()) {
                items.add(ItemStackUtil.create(slot));
            }
        }

        return CraftingInput.of(width, height, items);
    }

    public static @NotNull CraftingInput craftingInput(int offset, int width, int height, SlottedStorageAccess<Item, ItemResourceSlot> storage) {
        List<ItemStack> items = new ArrayList<>(width * height);
        for (int i = 0; i < width * height; i++) {
            items.add(ItemStackUtil.create(storage.slot(offset + i)));
        }
        return CraftingInput.of(width, height, items);
    }

    public static @NotNull CraftingInput craftingInput(int width, int height, ItemResourceSlot... slots) {
        return CraftingInput.of(width, height, new ItemList(slots));
    }

    public static @NotNull RecipeInput input(@NotNull SlottedStorageAccess<Item, ItemResourceSlot> storage) {
        List<ItemResourceSlot> slots = new ArrayList<>();
        ItemResourceSlot[] storageSlots = storage.getSlots();
        for (int i = 0; i < storage.size(); i++) {
            if (storageSlots[i].transferMode().isInput()) {
                slots.add(storageSlots[i]);
            }
        }

        return input(slots.toArray(ItemResourceSlot[]::new));
    }

    public static @NotNull RecipeInput input(SlottedStorageAccess<Item, ItemResourceSlot> storage, int start, int len) {
        ItemResourceSlot[] slots = new ItemResourceSlot[len];
        for (int i = 0; i < len; i++) {
            slots[i] = storage.getSlots()[start + i];
        }

        return input(slots);
    }

    public static @NotNull RecipeInput input(ItemResourceSlot... slots) {
        return new MachineRecipeInput(slots);
    }

    public static @NotNull SingleRecipeInput single(ItemResourceSlot slot) {
        return new SingleRecipeInput(ItemStackUtil.create(slot));
    }
}
