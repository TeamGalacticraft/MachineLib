/*
 * Copyright (c) 2021-2025 Team Galacticraft
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

import dev.galacticraft.machinelib.api.misc.DeltaPacketSerializable;
import dev.galacticraft.machinelib.api.misc.Modifiable;
import dev.galacticraft.machinelib.api.misc.PacketSerializable;
import dev.galacticraft.machinelib.api.misc.Serializable;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortTarget;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.impl.storage.EmptyMachineEnergyStorage;
import dev.galacticraft.machinelib.impl.storage.MachineEnergyStorageImpl;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.LongTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A simple energy storage implementation.
 */
public interface MachineEnergyStorage extends EnergyStorage, Serializable<LongTag>, PacketSerializable<ByteBuf>, DeltaPacketSerializable<ByteBuf, long[]>, Modifiable {

    /**
     * Gets an energy storage with zero capacity.
     *
     * @return empty energy storage
     */
    @Contract(pure = true)
    static @NotNull MachineEnergyStorage empty() {
        return EmptyMachineEnergyStorage.INSTANCE;
    }

    /**
     * Creates energy storage with shared insertion/extraction rate.
     *
     * @param energyCapacity capacity
     * @param ioRate insertion and extraction rate
     * @return created storage
     */
    @Contract(pure = true)
    static @NotNull MachineEnergyStorage create(
            final long energyCapacity,
            final long ioRate
    ) {
        return create(
                energyCapacity,
                ioRate,
                ioRate
        );
    }

    /**
     * Creates energy storage.
     *
     * @param energyCapacity capacity
     * @param insertion insertion rate
     * @param extraction extraction rate
     * @return created storage
     */
    @Contract(pure = true)
    static @NotNull MachineEnergyStorage create(
            final long energyCapacity,
            final long insertion,
            final long extraction
    ) {
        if (energyCapacity == 0) {
            return empty();
        }

        StoragePreconditions.notNegative(energyCapacity);
        StoragePreconditions.notNegative(insertion);
        StoragePreconditions.notNegative(extraction);

        return new MachineEnergyStorageImpl(
                energyCapacity,
                insertion,
                extraction
        );
    }

    /**
     * Creates an energy storage specification with shared insertion/extraction rate.
     *
     * @param energyCapacity capacity
     * @param io insertion and extraction rate
     * @return storage specification
     */
    @Contract(pure = true)
    static @NotNull Spec spec(
            final long energyCapacity,
            final long io
    ) {
        return spec(
                energyCapacity,
                io,
                io
        );
    }

    /**
     * Creates an energy storage specification.
     *
     * @param energyCapacity capacity
     * @param insertion insertion rate
     * @param extraction extraction rate
     * @return storage specification
     */
    @Contract(pure = true)
    static @NotNull Spec spec(
            final long energyCapacity,
            final long insertion,
            final long extraction
    ) {
        return new Spec(
                energyCapacity,
                insertion,
                extraction
        );
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
    long extract(
            long amount,
            @NotNull TransactionContext transaction
    );

    @Override
    long insert(
            long amount,
            @NotNull TransactionContext transaction
    );

    boolean isFull();

    boolean isEmpty();

    void setEnergy(
            long amount,
            @Nullable TransactionContext context
    );

    void setEnergy(long amount);

    /**
     * Gets the exact logical energy target id.
     *
     * @return energy target id, or {@code null} if unnamed
     */
    @Nullable
    ResourceLocation id();

    /**
     * Gets logical energy target groups.
     *
     * @return immutable group set
     */
    @NotNull
    Set<ResourceLocation> groups();

    /**
     * Gets exposed energy storage for a flow.
     *
     * @param flow resource flow
     * @return exposed energy storage, or {@code null}
     */
    @Nullable
    EnergyStorage getExposedStorage(@NotNull ResourceFlow flow);

    /**
     * Gets exposed energy storage for a target.
     *
     * @param flow resource flow
     * @param target port target
     * @return exposed energy storage, or {@code null} if target does not match
     */
    @Nullable
    EnergyStorage getExposedStorage(
            @NotNull ResourceFlow flow,
            @NotNull MultiblockPortTarget target
    );

    long externalInsertionRate();

    long externalExtractionRate();

    @ApiStatus.Internal
    void setParent(BlockEntity parent);

    boolean isValid();

    /**
     * Mutable energy storage specification.
     */
    final class Spec {

        private final long capacity;
        private final long insertion;
        private final long extraction;

        private @Nullable ResourceLocation id = null;
        private final Set<ResourceLocation> groups = new LinkedHashSet<>();

        /**
         * Creates an energy storage specification.
         *
         * @param capacity capacity
         * @param insertion insertion rate
         * @param extraction extraction rate
         */
        public Spec(
                final long capacity,
                final long insertion,
                final long extraction
        ) {
            StoragePreconditions.notNegative(capacity);
            StoragePreconditions.notNegative(insertion);
            StoragePreconditions.notNegative(extraction);

            this.capacity = capacity;
            this.insertion = insertion;
            this.extraction = extraction;
        }

        /**
         * Sets the exact logical target id for this energy storage.
         *
         * @param id target id
         * @return this specification
         */
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec id(final ResourceLocation id) {
            this.id = id;
            return this;
        }

        /**
         * Adds this energy storage to one logical target group.
         *
         * @param group group id
         * @return this specification
         */
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec group(final ResourceLocation group) {
            this.groups.add(group);
            return this;
        }

        /**
         * Adds this energy storage to multiple logical target groups.
         *
         * @param groups group ids
         * @return this specification
         */
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec groups(final ResourceLocation... groups) {
            this.groups.addAll(List.of(groups));
            return this;
        }

        /**
         * Creates energy storage from this specification.
         *
         * @return created energy storage
         */
        public MachineEnergyStorage create() {
            if (this.capacity == 0) {
                return empty();
            }

            return new MachineEnergyStorageImpl(
                    this.capacity,
                    this.insertion,
                    this.extraction,
                    this.id,
                    this.groups
            );
        }
    }
}