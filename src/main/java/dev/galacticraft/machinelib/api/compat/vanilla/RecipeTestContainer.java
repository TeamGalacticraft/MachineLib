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

package dev.galacticraft.machinelib.api.compat.vanilla;

import com.google.common.collect.Iterators;
import dev.galacticraft.machinelib.api.misc.Modifiable;
import dev.galacticraft.machinelib.api.storage.SlottedStorageAccess;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.util.ItemStackUtil;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class RecipeTestContainer implements Container, Modifiable {
    private final ItemResourceSlot[] slots;
    private final long[] modifications;
    private long modCount = 0;

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull RecipeTestContainer create(ItemResourceSlot @NotNull ... slots) {
        assert slots.length > 0;
        return new RecipeTestContainer(slots);
    }
    @Contract(value = "_, _, _-> new", pure = true)
    public static @NotNull RecipeTestContainer create(SlottedStorageAccess<Item, ItemResourceSlot> access, int start, int len) {
        Iterator<ItemResourceSlot> iterator = access.iterator();
        Iterators.advance(iterator, start);
        ItemResourceSlot[] slots = new ItemResourceSlot[len];
        for (int i = 0; i < len; i++) {
            slots[i] = iterator.next();
        }
        return new RecipeTestContainer(slots);
    }

    private RecipeTestContainer(ItemResourceSlot[] slots) {
        this.slots = slots;
        this.modifications = new long[this.slots.length];
    }

    @Override
    public int getContainerSize() {
        return this.slots.length;
    }

    @Override
    public boolean isEmpty() {
        for (ItemResourceSlot slot : this.slots) {
            if (!slot.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int i) {
        return ItemStackUtil.copy(this.slots[i]);
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {

    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public boolean canPlaceItem(int i, ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean canTakeItem(Container container, int i, ItemStack itemStack) {
        return false;
    }

    @Override
    public void clearContent() {}

    @Override
    public long getModifications() {
        for (int i = 0; i < this.slots.length; i++) {
            if (this.modifications[i] != (this.modifications[i] = this.slots[i].getModifications())) {
                this.modCount++;
            }
        }
        return this.modCount;
    }
}
