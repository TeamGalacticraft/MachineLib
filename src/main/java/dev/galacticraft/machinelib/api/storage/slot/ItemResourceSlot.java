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

package dev.galacticraft.machinelib.api.storage.slot;

import dev.galacticraft.machinelib.api.storage.ResourceFilter;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.impl.storage.slot.ItemResourceSlotImpl;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface ItemResourceSlot extends ResourceSlot<Item, ItemStack>, ContainerItemContext {
    @Contract("_, _ -> new")
    static @NotNull ItemResourceSlot create(@NotNull ItemSlotDisplay display, @NotNull ResourceFilter<Item> filter) {
        return create(display, filter, 64);
    }

    @Contract("_, _, _ -> new")
    static @NotNull ItemResourceSlot create(@NotNull ItemSlotDisplay display, @NotNull ResourceFilter<Item> filter, int capacity) {
        return create(display, filter, filter, capacity);
    }

    @Contract("_, _, _ -> new")
    static @NotNull ItemResourceSlot create(@NotNull ItemSlotDisplay display, @NotNull ResourceFilter<Item> filter, @NotNull ResourceFilter<Item> strictFilter) {
        return create(display, filter, strictFilter, 64);
    }

    @Contract("_, _, _, _ -> new")
    static @NotNull ItemResourceSlot create(@NotNull ItemSlotDisplay display, @NotNull ResourceFilter<Item> filter, @NotNull ResourceFilter<Item> strictFilter, int capacity) {
        if (capacity < 0 || capacity > 64) throw new IllegalArgumentException();
        return new ItemResourceSlotImpl(display, filter, strictFilter, capacity);
    }

    @NotNull ItemSlotDisplay getDisplay();

    @Override
    long getAmount();
}
