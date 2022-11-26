/*
 * Copyright (c) 2021-2022 Team Galacticraft
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

package dev.galacticraft.machinelib.impl.compat;

import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@ApiStatus.Internal
public final class ReadOnlySubInv implements Container {
    private final MachineItemStorage storage;
    private final int start;
    private final int size;

    public ReadOnlySubInv(@NotNull MachineItemStorage storage, int start, int size) {
        StoragePreconditions.notNegative(start);
        StoragePreconditions.notNegative(size);
        if (start + size > storage.size()) {
            throw new IndexOutOfBoundsException();
        }

        this.storage = storage;
        this.start = start;
        this.size = size;
    }

    @Override
    public int getContainerSize() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < this.size; i++) {
            if (storage.getAmount(this.start + i) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        if (slot < 0 || slot >= this.size) throw new IndexOutOfBoundsException();
        return this.storage.getStack(this.start + slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= this.size) throw new IndexOutOfBoundsException();
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= this.size) throw new IndexOutOfBoundsException();
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= this.size) throw new IndexOutOfBoundsException();
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return this.storage.canAccess(player);
    }

    @Override
    public void startOpen(Player player) {
    }

    @Override
    public void stopOpen(Player player) {
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot >= this.size) throw new IndexOutOfBoundsException();
        return this.storage.canAccept(this.start + slot, ItemVariant.of(stack));
    }

    @Override
    public int countItem(Item item) {
        int count = 0;
        for (int i = 0; i < this.size; i++) {
            if (storage.getVariant(this.start + i).getItem() == item) {
                count += storage.getAmount(this.start + i);
            }
        }
        return count;
    }

    @Override
    public boolean hasAnyOf(Set<Item> items) {
        for (int i = 0; i < this.size; i++) {
            if (items.contains(storage.getVariant(this.start + i).getItem())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void clearContent() {
    }
}
