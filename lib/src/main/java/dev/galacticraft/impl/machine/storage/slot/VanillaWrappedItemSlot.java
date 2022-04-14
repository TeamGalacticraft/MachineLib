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

package dev.galacticraft.impl.machine.storage.slot;

import com.mojang.datafixers.util.Pair;
import dev.galacticraft.api.machine.storage.display.ItemSlotDisplay;
import dev.galacticraft.impl.machine.storage.MachineItemStorageImpl;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class VanillaWrappedItemSlot extends Slot {
    private final @NotNull MachineItemStorageImpl storage;
    private final @Nullable Pair<Identifier, Identifier> icon;

    public VanillaWrappedItemSlot(@NotNull MachineItemStorageImpl storage, int index, @NotNull ItemSlotDisplay display) {
        super(storage.playerInventory(), index, display.x(), display.y());
        this.storage = storage;
        this.icon = display.icon();
    }

    @Nullable
    @Override
    public Pair<Identifier, Identifier> getBackgroundSprite() {
        return this.icon;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return this.storage.canAccept(this.getIndex(), ItemVariant.of(stack));
    }

    @Override
    public ItemStack getStack() {
        return this.storage.getStack(this.getIndex());
    }

    @Override
    public boolean hasStack() {
        return this.storage.getVariant(this.getIndex()).isBlank();
    }

    @Override
    public void setStack(ItemStack stack) {
        this.storage.replace(this.getIndex(), ItemVariant.of(stack), stack.getCount(), null);
    }

    @Override
    public void markDirty() {
        this.storage.incrementModCountUnsafe();
    }

    @Override
    public int getMaxItemCount() {
        return Math.toIntExact(this.storage.getSlot(this.getIndex()).getCapacity());
    }

    @Override
    public int getMaxItemCount(ItemStack stack) {
        return Math.toIntExact(this.storage.getSlot(this.getIndex()).getCapacity(ItemVariant.of(stack)));
    }

    @Override
    public ItemStack takeStack(int amount) {
        return this.storage.extract(this.getIndex(), amount, null);
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return this.storage.canAccess(playerEntity);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public ItemStack insertStack(ItemStack stack) {
        return this.insertStack(stack, stack.getCount());
    }

    @Override //return failed
    public ItemStack insertStack(ItemStack stack, int count) {
        long inserted = this.storage.insert(this.getIndex(), ItemVariant.of(stack), count, null);
        stack.setCount(Math.toIntExact(count - inserted));
        return stack;
    }

    @Override
    public boolean canTakePartial(PlayerEntity player) {
        return super.canTakePartial(player);
    }

    @Override
    public Optional<ItemStack> tryTakeStackRange(int min, int max, PlayerEntity player) {
        return super.tryTakeStackRange(min, max, player);
    }

    @Override
    public ItemStack takeStackRange(int min, int max, PlayerEntity player) {
        return super.takeStackRange(min, max, player);
    }
}
