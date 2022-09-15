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

package dev.galacticraft.machinelib.impl.storage.empty;

import dev.galacticraft.machinelib.api.screen.StorageSyncHandler;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.io.ResourceFlow;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import team.reborn.energy.api.EnergyStorage;

@ApiStatus.Internal
public enum EmptyMachineEnergyStorage implements MachineEnergyStorage {
    INSTANCE;

    @Override
    public boolean isFull() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void setEnergy(long amount, @NotNull TransactionContext context) {
    }

    @Override
    public void setEnergyUnsafe(long amount) {
    }

    @Override
    public @NotNull EnergyStorage getExposedStorage(@NotNull ResourceFlow flow) {
        return this;
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
    public @NotNull Tag writeNbt() {
        return ByteTag.ZERO;
    }

    @Override
    public void readNbt(@NotNull Tag nbt) {
    }

    @Override
    public boolean canExposedExtract(int slot) {
        return false;
    }

    @Override
    public boolean canExposedInsert(int slot) {
        return false;
    }

    @Override
    public @NotNull StorageSyncHandler createSyncHandler() {
        return StorageSyncHandler.DEFAULT;
    }

    @Override
    public boolean supportsInsertion() {
        return false;
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
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
}
