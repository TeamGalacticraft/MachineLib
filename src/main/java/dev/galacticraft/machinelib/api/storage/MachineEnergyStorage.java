/*
 * Copyright (c) 2021-2026 Team Galacticraft
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

import dev.galacticraft.machinelib.api.compat.transfer.ExposedEnergyStorage;
import dev.galacticraft.machinelib.api.misc.DeltaPacketSerializable;
import dev.galacticraft.machinelib.api.misc.Modifiable;
import dev.galacticraft.machinelib.api.misc.PacketSerializable;
import dev.galacticraft.machinelib.api.misc.Serializable;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.impl.storage.EmptyMachineEnergyStorage;
import dev.galacticraft.machinelib.impl.storage.MachineEnergyStorageImpl;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.LongTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

/**
 * A simple energy storage implementation.
 * The flow of energy is not restricted here, use {@link #getExposedStorage(ResourceFlow)} if you need filtering.
 *
 * @see ExposedEnergyStorage
 * @see EnergyStorage
 */
public interface MachineEnergyStorage extends EnergyStorage, Serializable<LongTag>, PacketSerializable<ByteBuf>, DeltaPacketSerializable<ByteBuf, long[]>, Modifiable {

    /**
     * {@return an energy storage with a capacity of zero}
     */
    @Contract(pure = true)
    static @NotNull MachineEnergyStorage empty() {
        return EmptyMachineEnergyStorage.INSTANCE;
    }

    /**
     * Creates a new energy storage.
     *
     * @param energyCapacity The capacity of the energy storage
     * @param ioRate The maximum amount of energy that can be inserted or extracted per tick
     * @return The newly created energy storage
     */
    @Contract(pure = true)
    static @NotNull MachineEnergyStorage create(long energyCapacity, long ioRate) {
        return create(energyCapacity, ioRate, ioRate);
    }

    /**
     * Creates a new energy storage.
     *
     * @param energyCapacity the capacity of the energy storage
     * @param insertion the maximum amount of energy that can be inserted per tick
     * @param extraction the maximum amount of energy that can be extracted per tick
     * @return the newly created energy storage
     */
    @Contract(pure = true)
    static @NotNull MachineEnergyStorage create(long energyCapacity, long insertion, long extraction) {
        if (energyCapacity == 0) return empty();

        StoragePreconditions.notNegative(energyCapacity);
        StoragePreconditions.notNegative(insertion);
        StoragePreconditions.notNegative(extraction);

        return new MachineEnergyStorageImpl(energyCapacity, insertion, extraction);
    }

    @Contract(pure = true)
    static @NotNull Spec spec(long energyCapacity, long io) {
        return spec(energyCapacity, io, io);
    }

    @Contract(pure = true)
    static @NotNull Spec spec(long energyCapacity, long insertion, long extraction) {
        return new Spec(energyCapacity, insertion, extraction);
    }

    /**
     * {@return whether the given amount of energy can be extracted}
     */
    boolean canExtract(long amount);

    /**
     * {@return whether the given amount of energy can be inserted}
     */
    boolean canInsert(long amount);

    /**
     * {@return the amount of energy that can be extracted}
     */
    long tryExtract(long amount);

    /**
     * {@return the amount of energy that can be inserted}
     */
    long tryInsert(long amount);

    /**
     * Extracts the given amount of energy from the storage.
     *
     * @param amount the amount of energy to extract
     * @return the amount of energy that was actually extracted
     */
    long extract(long amount);

    /**
     * Inserts the given amount of energy into the storage.
     *
     * @param amount the amount of energy to insert
     * @return the amount of energy that was actually inserted
     */
    long insert(long amount);

    /**
     * Extracts the given amount of energy from the storage.
     * If there is not enough energy, nothing is extracted.
     *
     * @param amount the amount of energy to extract
     * @return whether the exact amount of energy was extracted
     */
    boolean extractExact(long amount);

    /**
     * Inserts the given amount of energy into the storage.
     * If there is not enough space, nothing is inserted.
     *
     * @param amount the amount of energy to insert
     * @return whether the exact amount of energy was inserted
     */
    boolean insertExact(long amount);

    @Override
    long extract(long amount, @NotNull TransactionContext transaction);

    @Override
    long insert(long amount, @NotNull TransactionContext transaction);

    /**
     * {@return whether the energy storage is full}
     * An energy storage with a capacity of zero can be both full and empty at the same time.
     */
    boolean isFull();

    /**
     * {@return whether the energy storage is empty}
     * An energy storage can be both full and empty at the same time.
     */
    boolean isEmpty();

    /**
     * Sets the energy stored to the given amount.
     *
     * @param amount The amount of energy to set the energy stored to
     * @param context The transaction context
     */
    void setEnergy(long amount, @Nullable TransactionContext context);

    /**
     * Sets the energy stored to the given amount.
     *
     * @param amount The amount of energy to set the energy stored to
     */
    void setEnergy(long amount);

    /**
     * {@return a new exposed energy storage}
     *
     * @param flow The resource flow
     */
    @Nullable
    EnergyStorage getExposedStorage(@NotNull ResourceFlow flow);

    /**
     * {@return the rate that external storages can insert into this storage}
     */
    long externalInsertionRate();

    /**
     * {@return the rate that external storages can extract from this storage}
     */
    long externalExtractionRate();

    /**
     * Sets the parent of this energy storage (notified when the energy storage changes). Internal use only.
     */
    @ApiStatus.Internal
    void setParent(BlockEntity parent);

    /**
     * {@return whether the energy storage should still be interacted with}
     */
    boolean isValid();

    record Spec(long capacity, long insertion, long extraction) {
        public Spec {
            StoragePreconditions.notNegative(capacity);
            StoragePreconditions.notNegative(insertion);
            StoragePreconditions.notNegative(extraction);
        }

        public MachineEnergyStorage create() {
            if (this.capacity == 0) return empty();
            return new MachineEnergyStorageImpl(this.capacity, this.insertion, this.extraction);
        }
    }
}
