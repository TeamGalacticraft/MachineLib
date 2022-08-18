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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class VanillaWrappedItemSlot extends Slot {
    private final @NotNull MachineItemStorageImpl storage;
    private final @Nullable Pair<ResourceLocation, ResourceLocation> icon;

    public VanillaWrappedItemSlot(@NotNull MachineItemStorageImpl storage, int index, @NotNull ItemSlotDisplay display) {
        super(storage.playerInventory(), index, display.x(), display.y());
        this.storage = storage;
        this.icon = display.icon();
    }

    @Nullable
    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return this.icon;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.storage.canAccept(this.getContainerSlot(), ItemVariant.of(stack));
    }

    @Override
    public ItemStack getItem() {
        return this.storage.getStack(this.getContainerSlot());
    }

    @Override
    public boolean hasItem() {
        return this.storage.getVariant(this.getContainerSlot()).isBlank();
    }

    @Override
    public void set(ItemStack stack) {
        this.storage.replace(this.getContainerSlot(), ItemVariant.of(stack), stack.getCount(), null);
    }

    @Override
    public void setChanged() {
        this.storage.incrementModCountUnsafe();
    }

    @Override
    public int getMaxStackSize() {
        return Math.toIntExact(this.storage.getSlot(this.getContainerSlot()).getCapacity());
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.toIntExact(this.storage.getSlot(this.getContainerSlot()).getCapacity(ItemVariant.of(stack)));
    }

    @Override
    public ItemStack remove(int amount) {
        return this.storage.extract(this.getContainerSlot(), amount, null);
    }

    @Override
    public boolean mayPickup(Player playerEntity) {
        return this.storage.canAccess(playerEntity);
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
    public ItemStack safeInsert(ItemStack stack, int count) {
        long inserted = this.storage.insert(this.getContainerSlot(), ItemVariant.of(stack), count, null);
        stack.setCount(Math.toIntExact(count - inserted));
        return stack;
    }

    @Override
    public boolean allowModification(Player player) {
        return super.allowModification(player);
    }

    @Override
    public Optional<ItemStack> tryRemove(int min, int max, Player player) {
        return super.tryRemove(min, max, player);
    }

    @Override
    public ItemStack safeTake(int min, int max, Player player) {
        return super.safeTake(min, max, player);
    }
}
