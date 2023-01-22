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

package dev.galacticraft.machinelib.impl.storage;

import dev.galacticraft.machinelib.api.fluid.FluidStack;
import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;

public class EmptyMachineFluidStorage implements MachineFluidStorage {
    public static final EmptyMachineFluidStorage INSTANCE = new EmptyMachineFluidStorage();

    private EmptyMachineFluidStorage() {
    }

    @Override
    public long getModifications() {
        return -2;
    }

    @Override
    public void revertModification() {
    }

    @Override
    public void markModified(@Nullable TransactionContext context) {
    }

    @Override
    public void markModified() {
    }

    @Override
    public void setListener(Runnable listener) {

    }

    @Override
    public int groups() {
        return 0;
    }

    @Override
    public @NotNull SlotGroup<Fluid, FluidStack, FluidResourceSlot> getGroup(@NotNull SlotGroupType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull SlotGroupType @NotNull [] getTypes() {
        return new SlotGroupType[0];
    }

    @Override
    public @NotNull ListTag createTag() {
        return new ListTag();
    }

    @Override
    public void readTag(@NotNull ListTag tag) {
    }

    @Override
    public void writePacket(@NotNull FriendlyByteBuf buf) {
    }

    @Override
    public void readPacket(@NotNull FriendlyByteBuf buf) {
    }

    @Override
    public FluidResourceSlot[] getSlots() {
        return new FluidResourceSlot[0];
    }

    @NotNull
    @Override
    public Iterator<SlotGroup<Fluid, FluidStack, FluidResourceSlot>> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public @Nullable MenuSyncHandler createSyncHandler() {
        return null;
    }
}
