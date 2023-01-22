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
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
import dev.galacticraft.machinelib.impl.MachineLib;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ContainerSlotGroupImpl<Slot extends ResourceSlot<Item, ItemStack>> extends SlotGroupImpl<Item, ItemStack, Slot> implements ContainerSlotGroup<Slot> {
    public ContainerSlotGroupImpl(@NotNull SlotGroupType type, @NotNull Slot @NotNull [] slots) {
        super(type, slots);
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
        MachineLib.LOGGER.error("attempted to remove item from recipe test container!");
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int i) {
        MachineLib.LOGGER.error("attempted to remove item from recipe test container!");
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        MachineLib.LOGGER.error("attempted to modify item from recipe test container!");
    }

    @Override
    public void setChanged() {
        MachineLib.LOGGER.error("attempted to mark recipe test container as modified!");
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {
        MachineLib.LOGGER.error("attempted to clear items in a recipe test container!");
    }
}
