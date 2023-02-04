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

package dev.galacticraft.machinelib.impl.storage.slot;

import dev.galacticraft.machinelib.api.storage.slot.ContainerSlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.impl.Utils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class ItemSlotGroupImpl<Slot extends ResourceSlot<Item, ItemStack>> extends SlotGroupImpl<Item, ItemStack, Slot> implements ContainerSlotGroup<Slot> {
    public ItemSlotGroupImpl(@NotNull Slot @NotNull [] slots) {
        super(slots);
    }

    @Override
    public boolean canInsertStack(@NotNull ItemStack stack) {
        if (stack.isEmpty()) return true;
        assert stack.getItem() != Items.AIR && stack.getCount() > 0;
        long inserted = 0;
        for (Slot slot : this) {
            inserted += slot.tryInsert(stack.getItem(), stack.getTag(), stack.getCount() - inserted);
            if (stack.getCount() == inserted) return true;
        }
        return stack.getCount() == inserted;
    }

    @Override
    public long tryInsertStack(@NotNull ItemStack stack) {
        if (stack.isEmpty()) return 0;
        assert stack.getItem() != Items.AIR && stack.getCount() > 0;
        long inserted = 0;
        for (Slot slot : this) {
            inserted += slot.tryInsert(stack.getItem(), stack.getTag(), stack.getCount() - inserted);
            if (stack.getCount() == inserted) break;
        }
        return inserted;
    }

    @Override
    public long insertStack(@NotNull ItemStack stack) {
        if (stack.isEmpty()) return 0;
        assert stack.getItem() != Items.AIR && stack.getCount() > 0;
        long inserted = 0;
        for (Slot slot : this) {
            inserted += slot.insert(stack.getItem(), stack.getTag(), stack.getCount() - inserted);
            if (stack.getCount() == inserted) break;
        }
        return inserted;
    }

    @Override
    public int getContainerSize() {
        return this.size();
    }

    @Override
    public @NotNull ItemStack getItem(int i) {
        return this.copyStack(i);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        Utils.breakpointMe("attempted to remove item from recipe test container!");
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int i) {
        Utils.breakpointMe("attempted to remove item from recipe test container!");
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        Utils.breakpointMe("attempted to modify item from recipe test container!");
    }

    @Override
    public void setChanged() {
        Utils.breakpointMe("attempted to mark recipe test container as modified!");
    }

    @Override
    public boolean stillValid(Player player) {
        Utils.breakpointMe("testing player validity of inv view");
        return false;
    }

    @Override
    public void clearContent() {
        Utils.breakpointMe("attempted to clear items in a recipe test container!");
    }
}
