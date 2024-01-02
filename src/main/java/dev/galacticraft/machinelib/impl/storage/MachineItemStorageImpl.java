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

package dev.galacticraft.machinelib.impl.storage;

import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.util.ItemStackUtil;
import dev.galacticraft.machinelib.impl.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MachineItemStorageImpl extends ResourceStorageImpl<Item, ItemResourceSlot> implements MachineItemStorage {
    public static final MachineItemStorageImpl EMPTY = new MachineItemStorageImpl(new ItemResourceSlot[0]);

    public MachineItemStorageImpl(@NotNull ItemResourceSlot @NotNull [] slots) {
        super(slots);
    }

    @Override
    public int getContainerSize() {
        return this.size();
    }

    @Override
    public @NotNull ItemStack getItem(int i) {
        return ItemStackUtil.copy(this.getSlot(i));
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        Utils.breakpointMe("attempted to remove item from vanilla compat container!");
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int i) {
        Utils.breakpointMe("attempted to remove item from vanilla compat container!");
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        Utils.breakpointMe("attempted to modify item from vanilla compat container!");
    }

    @Override
    public void setChanged() {
        Utils.breakpointMe("attempted to mark vanilla compat container as modified!");
    }

    @Override
    public boolean stillValid(Player player) {
        Utils.breakpointMe("testing player validity of vanilla compat container");
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
    public void clearContent() {
        Utils.breakpointMe("attempted to clear items in a vanilla compat container!");
    }

    @Override
    public boolean consumeOne(@NotNull Item resource) {
        for (ItemResourceSlot slot : this.slots) {
            if (slot.consumeOne(resource)) return true;
        }
        return false;
    }

    @Override
    public boolean consumeOne(@NotNull Item resource, @Nullable CompoundTag tag) {
        for (ItemResourceSlot slot : this.slots) {
            if (slot.consumeOne(resource, tag)) return true;
        }
        return false;
    }

    @Override
    public long consume(@NotNull Item resource, long amount) {
        long consumed = 0;
        for (ItemResourceSlot slot : this.slots) {
            consumed += slot.consume(resource, amount - consumed);
            if (consumed == amount) break;
        }
        return consumed;
    }

    @Override
    public long consume(@NotNull Item resource, @Nullable CompoundTag tag, long amount) {
        long consumed = 0;
        for (ItemResourceSlot slot : this.slots) {
            consumed += slot.consume(resource, tag, amount - consumed);
            if (consumed == amount) break;
        }
        return consumed;
    }

    @Override
    public @Nullable Item consumeOne(int slot) {
        return this.slots[slot].consumeOne();
    }

    @Override
    public boolean consumeOne(int slot, @NotNull Item resource) {
        return this.slots[slot].consumeOne(resource);
    }

    @Override
    public boolean consumeOne(int slot, @NotNull Item resource, @Nullable CompoundTag tag) {
        return this.slots[slot].consumeOne(resource, tag);
    }

    @Override
    public long consume(int slot, long amount) {
        return this.slots[slot].consume(amount);
    }

    @Override
    public long consume(int slot, @NotNull Item resource, long amount) {
        return this.slots[slot].consume(resource, amount);
    }

    @Override
    public long consume(int slot, @NotNull Item resource, @Nullable CompoundTag tag, long amount) {
        return this.slots[slot].consume(resource, tag, amount);
    }

    @Override
    public boolean consumeOne(int start, int len, @NotNull Item resource) {
        for (int i = start; i < start + len; i++) {
            ItemResourceSlot slot = this.slots[i];
            if (slot.consumeOne(resource)) return true;
        }
        return false;
    }

    @Override
    public boolean consumeOne(int start, int len, @NotNull Item resource, @Nullable CompoundTag tag) {
        for (int i = start; i < start + len; i++) {
            ItemResourceSlot slot = this.slots[i];
            if (slot.consumeOne(resource, tag)) return true;
        }
        return false;
    }

    @Override
    public long consume(int start, int len, @NotNull Item resource, long amount) {
        long consumed = 0;
        for (int i = start; i < start + len; i++) {
            ItemResourceSlot slot = this.slots[i];
            consumed += slot.consume(resource, amount - consumed);
            if (consumed == amount) break;
        }
        return consumed;
    }

    @Override
    public long consume(int start, int len, @NotNull Item resource, @Nullable CompoundTag tag, long amount) {
        long consumed = 0;
        for (int i = start; i < start + len; i++) {
            ItemResourceSlot slot = this.slots[i];
            consumed += slot.consume(resource, tag, amount - consumed);
            if (consumed == amount) break;
        }
        return consumed;
    }
}
