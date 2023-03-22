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

import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.io.ResourceFlow;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.LongTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public class EmptyMachineEnergyStorage implements MachineEnergyStorage {
    public static final MachineEnergyStorage INSTANCE = new EmptyMachineEnergyStorage();

    @Override
    public boolean canExtract(long amount) {
        return false;
    }

    @Override
    public boolean canInsert(long amount) {
        return false;
    }

    @Override
    public long tryExtract(long amount) {
        return 0;
    }

    @Override
    public long tryInsert(long amount) {
        return 0;
    }

    @Override
    public long extract(long amount) {
        return 0;
    }

    @Override
    public long insert(long amount) {
        return 0;
    }

    @Override
    public boolean extractExact(long amount) {
        return false;
    }

    @Override
    public boolean insertExact(long amount) {
        return false;
    }

    @Override
    public long extract(long amount, @NotNull TransactionContext transaction) {
        return 0;
    }

    @Override
    public long getAmount() {
        return 0;
    }

    @Override
    public long getCapacity() {
        return 0;
    }

    @Override
    public boolean supportsInsertion() {
        return false;
    }

    @Override
    public long insert(long amount, @NotNull TransactionContext transaction) {
        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public boolean isFull() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void setEnergy(long amount, @Nullable TransactionContext context) {
    }

    @Override
    public void setEnergy(long amount) {
    }

    @Override
    public @Nullable EnergyStorage getExposedStorage(@NotNull ResourceFlow flow) {
        return null;
    }

    @Override
    public boolean canExposedInsert() {
        return false;
    }

    @Override
    public boolean canExposedExtract() {
        return false;
    }

    @Override
    public void setListener(Runnable listener) {
    }

    @Override
    public @NotNull LongTag createTag() {
        return LongTag.valueOf(0);
    }

    @Override
    public void readTag(@NotNull LongTag tag) {
    }

    @Override
    public void writePacket(@NotNull FriendlyByteBuf buf) {
    }

    @Override
    public void readPacket(@NotNull FriendlyByteBuf buf) {
    }

    @Override
    public @Nullable MenuSyncHandler createSyncHandler() {
        return null;
    }

    @Override
    public long getModifications() {
        return -1;
    }
}
