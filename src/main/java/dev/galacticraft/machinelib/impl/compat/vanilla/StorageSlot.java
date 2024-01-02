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

package dev.galacticraft.machinelib.impl.compat.vanilla;

import com.mojang.datafixers.util.Pair;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.api.util.ItemStackUtil;
import dev.galacticraft.machinelib.impl.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@ApiStatus.Internal
public class StorageSlot extends Slot {
    private final @Nullable Pair<ResourceLocation, ResourceLocation> icon;
    private final @NotNull ItemResourceSlot slot;
    private final @NotNull UUID player;
    private @Nullable ItemStack watchedStack = null;
    private long watchModCount = Long.MIN_VALUE;

    public StorageSlot(Container group, @NotNull ItemResourceSlot slot, @NotNull ItemSlotDisplay display, int index, @NotNull UUID player) {
        super(group, index, display.x(), display.y());
        this.icon = display.icon();
        this.slot = slot;
        this.player = player;
    }

    public @NotNull ItemResourceSlot getSlot() {
        return this.slot;
    }

    @Nullable
    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return this.icon;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.slot.inputType().playerInsertion() && (stack.isEmpty() || this.slot.getFilter().test(stack.getItem(), stack.getTag()));
    }

    @Override
    public @NotNull ItemStack getItem() {
        if (this.watchModCount != this.slot.getModifications()) {
            this.watchModCount = this.slot.getModifications();
            this.watchedStack = ItemStackUtil.copy(this.slot);
        }
        assert this.watchedStack != null;
        return this.watchedStack;
    }

    @Override
    public boolean hasItem() {
        return !this.slot.isEmpty();
    }

    @Override
    public void set(ItemStack stack) {
        if (stack.isEmpty()) {
            this.slot.set(null, null, 0);
        } else {
            this.slot.set(stack.getItem(), stack.getTag(), stack.getCount());
        }
        this.slot.markModified();
    }

    @Override
    public void setChanged() {
        if (this.watchModCount == this.slot.getModifications()) {
            assert this.watchedStack != null;
            if (this.watchedStack.getCount() != this.slot.getAmount()
                    || !Utils.tagsEqual(this.watchedStack.getTag(), this.slot.getTag())
                    || !Utils.itemsEqual(this.slot.getResource(), this.watchedStack.getItem())
            ) {
                if (true) throw new AssertionError();
                this.set(this.watchedStack);
                this.slot.markModified();
            }
            this.watchModCount = this.slot.getModifications();
        }
    }

    @Override
    public int getMaxStackSize() {
        return Math.toIntExact(this.slot.getCapacity());
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return (int) Math.min(stack.getMaxStackSize(), this.slot.getCapacity());
    }

    @Override
    public @NotNull ItemStack remove(int amount) {
        if (this.slot.isEmpty()) return ItemStack.EMPTY;
        ItemStack extract = ItemStackUtil.copy(this.slot);
        extract.setCount((int) this.slot.extract(amount));
        return extract;
    }

    @Override
    public boolean mayPickup(@NotNull Player playerEntity) {
        return playerEntity.getUUID().equals(this.player);
    }

    @Override //return failed
    public @NotNull ItemStack safeInsert(@NotNull ItemStack stack, int count) {
        if (this.mayPlace(stack)) {
            long inserted = this.slot.insert(stack.getItem(), stack.getTag(), Math.min(count, stack.getCount()));
            stack.shrink((int) inserted);
            return stack;
        }
        return stack;
    }
}
