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

package dev.galacticraft.machinelib.api.block.face;

import com.google.common.base.Preconditions;
import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.ResourceStorage;
import dev.galacticraft.machinelib.api.storage.exposed.ExposedStorage;
import dev.galacticraft.machinelib.api.storage.io.ConfiguredStorage;
import dev.galacticraft.machinelib.api.storage.io.ResourceFlow;
import dev.galacticraft.machinelib.api.storage.io.ResourceType;
import dev.galacticraft.machinelib.api.storage.io.StorageSelection;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.impl.block.face.MachineIOFaceConfigImpl;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

/**
 * Represents a face of a {@link MachineBlockEntity} that has been configured to
 * accept certain types of resources.
 */
public interface MachineIOFaceConfig {
    /**
     * Creates a new, blank {@link MachineIOFaceConfig}.
     *
     * @see #of(ResourceType, ResourceFlow)
     * @return A new, blank {@link MachineIOFaceConfig}.
     */
    @Contract(value = " -> new", pure = true)
    static @NotNull MachineIOFaceConfig blank() {
        return of(ResourceType.NONE, ResourceFlow.BOTH);
    }

    /**
     * Creates a new {@link MachineIOFaceConfig}.
     *
     * @param type The type of resource to accept.
     * @param flow The flow direction of the resource.
     * @see MachineIOFaceConfigImpl the default implementation
     * @return A new {@link MachineIOFaceConfig}.
     */
    @Contract(value = "_, _ -> new", pure = true)
    static @NotNull MachineIOFaceConfig of(@NotNull ResourceType type, @NotNull ResourceFlow flow) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(flow);

        return new MachineIOFaceConfigImpl(type, flow);
    }

    /**
     * Configures this face to accept the given resource type and flow.
     *
     * @param type The type of resource to accept.
     * @param flow The flow direction of the resource.
     */
    @Contract(mutates = "this")
    void setOption(@NotNull ResourceType type, @NotNull ResourceFlow flow);

    /**
     * Sets the filter of this face.
     *
     * @param matching the filter to set.
     */
    @Contract(mutates = "this")
    void setSelection(@Nullable StorageSelection matching);

    /**
     * Returns the type of resource that this face is configured to accept.
     *
     * @return The type of resource that this face is configured to accept.
     */
    @Contract(pure = true)
    @NotNull ResourceType getType();

    /**
     * Returns the flow direction of this face.
     *
     * @return The flow direction of this face.
     */
    @Contract(pure = true)
    @NotNull ResourceFlow getFlow();

    /**
     * Returns the filter of this face.
     *
     * @return The filter of this face.
     */
    @Contract(pure = true)
    @Nullable StorageSelection getSelection();

    /**
     * Returns the exposed storage of this face.
     * If the type of storage is invalid for this slot, this method will return {@code null}.
     *
     * @param storage The storage to use.
     * @param <T> The type of storage.
     * @param <V> The type of resource.
     * @return The exposed storage of this face.
     */
    <T, V extends TransferVariant<T>> @Nullable ExposedStorage<T, V> getExposedStorage(@NotNull ResourceStorage<T, V, ?> storage);

    /**
     * Returns the exposed energy storage of this face.
     * If the type of storage is not an energy storage, this method will return {@code null}.
     *
     * @param storage The storage to use.
     * @return The exposed energy storage of this face.
     */
    @Nullable EnergyStorage getExposedStorage(@NotNull MachineEnergyStorage storage);

    /**
     * Returns a sorted array of the matching slots of this face in the provided storage.
     *
     * @param storage The storage to match.
     * @return The matching slots of this face in the provided storage.
     */
    int @NotNull[] getMatching(@Nullable ConfiguredStorage storage);

    /**
     * Returns a sorted array of the matching slots of this face in the provided storage.
     * Ignores face selection filtering.
     *
     * @param storage The storage to match.
     * @return The matching slots of this face in the provided storage.
     */
    int @NotNull[] getMatchingWild(@Nullable ConfiguredStorage storage);

    /**
     * Returns the matching slot groups for this face.
     * Ignores face selection filtering.
     *
     * @param machine the machine to get the groups from.
     * @return the matching slot groups for this face.
     */
    @NotNull SlotGroup @NotNull[] getMatchingGroups(MachineBlockEntity machine);

    /**
     * Write this configuration to a new nbt compound.
     *
     * @param groups The available slot groups.
     * @return The nbt compound that was written to.
     */
    @NotNull CompoundTag writeNbt(@NotNull SlotGroup @Nullable[] groups);

    /**
     * Read the configuration from the given nbt compound.
     *
     * @param nbt    The nbt compound to read from.
     * @param groups The available slot groups.
     */
    void readNbt(@NotNull CompoundTag nbt, @NotNull SlotGroup @Nullable[] groups);
}
