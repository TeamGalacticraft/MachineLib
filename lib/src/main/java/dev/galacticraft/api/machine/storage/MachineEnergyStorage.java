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

package dev.galacticraft.api.machine.storage;

import dev.galacticraft.api.machine.storage.io.ConfiguredStorage;
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.machine.storage.io.SlotType;
import dev.galacticraft.impl.machine.storage.MachineEnergyStorageImpl;
import dev.galacticraft.impl.machine.storage.empty.EmptyMachineEnergyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import team.reborn.energy.api.EnergyStorage;

/**
 * A simple energy storage implementation that allows for the restriction of input and output of energy.
 */
public interface MachineEnergyStorage extends EnergyStorage, ConfiguredStorage {
    SlotType<?, ?>[] NO_SLOTS = new SlotType[0];

    @Contract("_, _, _ -> new")
    static @NotNull MachineEnergyStorage of(long energyCapacity, long insertion, long extraction) {
        StoragePreconditions.notNegative(energyCapacity);
        if (energyCapacity == 0) return EmptyMachineEnergyStorage.INSTANCE;
        return new MachineEnergyStorageImpl(energyCapacity, insertion, extraction);
    }

    /**
     * Returns whether the energy storage is full (cannot insert more energy).
     * An energy storage can be both full and empty at the same time.
     * @return Whether the energy storage is full
     */
    boolean isFull();

    /**
     * Returns whether the energy storage is empty (contains no energy).
     * An energy storage can be both full and empty at the same time.
     * @return Whether the energy storage is empty
     */
    boolean isEmpty();

    /**
     * Sets the energy stored to the given amount.
     * @param amount The amount of energy to set the energy stored to
     * @param context The transaction context
     */
    void setEnergy(long amount, @NotNull TransactionContext context);

    /**
     * Sets the energy stored to the given amount, without using a transaction.
     * Used for syncing the energy stored between client and server.
     * Use at your own risk.
     * @param amount The amount of energy to set the energy stored to
     */
    @TestOnly
    void setEnergyUnsafe(long amount);

    /**
     * Returns an exposed energy storage that has restricted input and output.
     * @param flow The resource flow
     * @return The exposed energy storage
     */
    @NotNull EnergyStorage getExposedStorage(@NotNull ResourceFlow flow);

    /**
     * Returns a read-only view of the energy storage.
     * @return The read-only view of the energy storage
     */
    @NotNull EnergyStorage view();

    /**
     * Serializes the energy storage to NBT.
     * @return The serialized NBT
     */
    @NotNull Tag writeNbt();

    /**
     * Deserializes the energy storage from NBT.
     * @param nbt The NBT to deserialize from
     */
    void readNbt(@NotNull Tag nbt);

    @Override
    default @NotNull SlotType<?, ?> @NotNull [] getTypes() {
        return NO_SLOTS;
    }
}
