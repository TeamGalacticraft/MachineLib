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

package dev.galacticraft.impl.machine.storage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public record PlayerExposedVanillaInventory(MachineItemStorageImpl storage) implements Container {
    @Override
    public int getContainerSize() {
        return this.storage.size();
    }

    @Override
    public boolean isEmpty() {
        return this.storage.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.storage.getStack(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (this.storage.canExposedExtract(slot) || this.storage.canExposedInsert(slot)) {
            return this.storage.extract(slot, amount, null);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (this.storage.canExposedExtract(slot) || this.storage.canExposedInsert(slot)) {
            return this.storage.extract(slot, Long.MAX_VALUE, null);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.storage.setSlotUnsafe(slot, ItemVariant.of(stack), stack.getCount(), true);
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void setChanged() {
        this.storage.incrementModCountUnsafe();
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
        return this.storage.canAccept(slot, ItemVariant.of(stack));
    }

    @Override
    public int countItem(Item item) {
        return Math.toIntExact(this.storage.count(item));
    }

    @Override
    public boolean hasAnyOf(Set<Item> items) {
        return this.storage.containsAny(items);
    }

    @Override
    public void clearContent() {
        this.storage.clearContent();
    }
}
