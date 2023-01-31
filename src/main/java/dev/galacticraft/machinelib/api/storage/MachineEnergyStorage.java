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

package dev.galacticraft.machinelib.api.storage;

import dev.galacticraft.machinelib.api.storage.io.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.exposed.ExposedEnergyStorage;
import dev.galacticraft.machinelib.impl.storage.EmptyMachineEnergyStorage;
import dev.galacticraft.machinelib.impl.storage.MachineEnergyStorageImpl;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.LongTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import team.reborn.energy.api.EnergyStorage;

/**
 * A simple energy storage implementation.
 * The flow of energy is not restricted here, use {@link #getExposedStorage(ResourceFlow)} if you need filtering.
 *
 * @see ExposedEnergyStorage
 * @see team.reborn.energy.api.EnergyStorage
 */
public interface MachineEnergyStorage extends EnergyStorage, Deserializable<LongTag>, MenuSynchronizable, Modifiable {

    @Contract(pure = true)
    static @NotNull MachineEnergyStorage empty() {
        return EmptyMachineEnergyStorage.INSTANCE;
    }

    @Contract(pure = true)
    static @NotNull MachineEnergyStorage of(long energyCapacity, long insertion, long extraction, boolean externalInsertion, boolean externalExtraction) {
        if (energyCapacity == 0 || insertion == 0 || extraction == 0) return empty();

        StoragePreconditions.notNegative(energyCapacity);
        StoragePreconditions.notNegative(insertion);
        StoragePreconditions.notNegative(extraction);

        return new MachineEnergyStorageImpl(energyCapacity, insertion, extraction, externalInsertion, externalExtraction);
    }

    @Contract(pure = true)
    static @NotNull MachineEnergyStorage of(long energyCapacity, long ioRate, boolean externalInsertion, boolean externalExtraction) {
        if (energyCapacity == 0 || ioRate == 0) return empty();

        StoragePreconditions.notNegative(energyCapacity);
        StoragePreconditions.notNegative(ioRate);
        return new MachineEnergyStorageImpl(energyCapacity, ioRate, ioRate, externalInsertion, externalExtraction);
    }

    boolean canExtract(long amount);

    boolean canInsert(long amount);

    long tryExtract(long amount);

    long tryInsert(long amount);

    long extract(long amount);

    long insert(long amount);

    boolean extractExact(long amount);

    boolean insertExact(long amount);

    @Override
    long extract(long amount, @NotNull TransactionContext transaction);

    @Override
    long insert(long amount, @NotNull TransactionContext transaction);

    /**
     * Returns whether the energy storage is full (cannot insert more energy).
     * An energy storage can be both full and empty at the same time.
     *
     * @return Whether the energy storage is full
     */
    boolean isFull();

    /**
     * Returns whether the energy storage is empty (contains no energy).
     * An energy storage can be both full and empty at the same time.
     *
     * @return Whether the energy storage is empty
     */
    boolean isEmpty();

    /**
     * Sets the energy stored to the given amount.
     *
     * @param amount  The amount of energy to set the energy stored to
     * @param context The transaction context
     */
    void setEnergy(long amount, @NotNull TransactionContext context);

    /**
     * Sets the energy stored to the given amount, without using a transaction.
     * Used for syncing the energy stored between client and server.
     * Use at your own risk.
     *
     * @param amount The amount of energy to set the energy stored to
     */
    @TestOnly
    void setEnergyUnsafe(long amount);

    /**
     * Returns an exposed energy storage that has restricted input and output.
     *
     * @param flow The resource flow
     * @return The exposed energy storage
     */
    @NotNull EnergyStorage getExposedStorage(@NotNull ResourceFlow flow);

    boolean canExposedInsert();

    boolean canExposedExtract();

    void setListener(Runnable listener);
}
