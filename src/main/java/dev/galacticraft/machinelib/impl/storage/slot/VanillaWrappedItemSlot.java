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

package dev.galacticraft.machinelib.impl.storage.slot;

import com.mojang.datafixers.util.Pair;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.impl.storage.MachineItemStorageImpl;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

@ApiStatus.Internal
public class VanillaWrappedItemSlot extends Slot {
    private final @Nullable Pair<ResourceLocation, ResourceLocation> icon;
    private final @NotNull ResourceSlot<Item, ItemVariant, ItemStack> slot;
    private final boolean insert;
    private final UUID uuid;

    public VanillaWrappedItemSlot(@NotNull MachineItemStorageImpl storage, int index, @NotNull ItemSlotDisplay display, @NotNull Player player) {
        super(storage.playerInventory(), index, display.x(), display.y());
        this.icon = display.icon();
        this.slot = storage.getSlot(index);
        this.insert = storage.canPlayerInsert(index);
        this.uuid = player.getUUID();
    }

    @Nullable
    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return this.icon;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.insert && this.slot.canAccept(ItemVariant.of(stack));
    }

    @Override
    public ItemStack getItem() {
        return this.slot.copyStack();
    }

    @Override
    public boolean hasItem() {
        return !this.slot.isResourceBlank();
    }

    @Override
    public void set(ItemStack stack) {
        this.slot.setStack(ItemVariant.of(stack), stack.getCount());
    }

    @Override
    public void setChanged() {
        this.slot.incrementModCountUnsafe();
    }

    @Override
    public int getMaxStackSize() {
        return Math.toIntExact(this.slot.getCapacity());
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.toIntExact(this.slot.getCapacity(ItemVariant.of(stack)));
    }

    @Override
    public ItemStack remove(int amount) {
        ItemStack extract;
        try (Transaction transaction = Transaction.openOuter()) {
            extract = this.slot.extract(amount, transaction);
            transaction.commit();
        }
        return extract;
    }

    @Override
    public boolean mayPickup(@NotNull Player playerEntity) {
        return playerEntity.getUUID().equals(this.uuid);
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public ItemStack safeInsert(ItemStack stack) {
        return this.safeInsert(stack, stack.getCount());
    }

    @Override //return failed
    public ItemStack safeInsert(@NotNull ItemStack stack, int count) {
        assert stack.getCount() >= count;
        if (this.mayPlace(stack)) {
            long inserted;
            try (Transaction transaction = Transaction.openOuter()) {
                inserted = this.slot.insert(ItemVariant.of(stack), count, transaction);
                transaction.commit();
            }
            stack.setCount(Math.toIntExact(stack.getCount() - inserted));
            return stack;
        }
        return stack;
    }

    @Override
    public Optional<ItemStack> tryRemove(int i, int j, Player player) {
        return super.tryRemove(i, j, player);
    }
}
