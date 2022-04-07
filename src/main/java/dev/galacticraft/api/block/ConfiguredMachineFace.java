/*
 * Copyright (c) 2019-2022 Team Galacticraft
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

package dev.galacticraft.api.block;

import com.mojang.datafixers.util.Either;
import dev.galacticraft.api.machine.storage.MachineEnergyStorage;
import dev.galacticraft.api.machine.storage.ResourceStorage;
import dev.galacticraft.api.machine.storage.io.*;
import dev.galacticraft.impl.block.ConfiguredMachineFaceImpl;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

/**
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public interface ConfiguredMachineFace {
    @Contract(value = "_, _ -> new", pure = true)
    static @NotNull ConfiguredMachineFace of(ResourceType<?, ?> type, ResourceFlow flow) {
        return new ConfiguredMachineFaceImpl(type, flow);
    }
    @Contract(value = " -> new", pure = true)
    static @NotNull ConfiguredMachineFace create() {
        return new ConfiguredMachineFaceImpl(ResourceType.NONE, ResourceFlow.BOTH);
    }

    /**
     * Configures this face to accept the given resource type and flow.
     * @param type The type of resource to accept.
     * @param flow The flow direction of the resource.
     */
    void setOption(@NotNull ResourceType<?, ?> type, @NotNull ResourceFlow flow);

    /**
     * Sets the filter of this face.
     * @param matching the filter to set.
     */
    void setMatching(@Nullable Either<Integer, SlotType<?, ?>> matching);

    /**
     * Returns the type of resource that this face is configured to accept.
     * @return The type of resource that this face is configured to accept.
     */
    ResourceType<?, ?> getType();

    /**
     * Returns the flow direction of this face.
     * @return The flow direction of this face.
     */
    ResourceFlow getFlow();

    /**
     * Returns the filter of this face.
     * @return The filter of this face.
     */
    @Nullable Either<Integer, SlotType<?, ?>> getMatching();

    /**
     * Returns the exposed storage of this face.
     * If the type of storage is invalid for this slot, it will be replaced with a read only storage view.
     * @param storage The storage to use.
     * @param <T> The type of storage.
     * @param <V> The type of resource.
     * @return The exposed storage of this face.
     */
    <T, V extends TransferVariant<T>> ExposedStorage<T, V> getExposedStorage(@NotNull ResourceStorage<T, V, ?> storage);

    /**
     * Returns the exposed energy storage of this face.
     * If the type of storage is not an energy storage, it will be replaced with a read only storage view.
     * @param storage The storage to use.
     * @return The exposed energy storage of this face.
     */
    EnergyStorage getExposedStorage(@NotNull MachineEnergyStorage storage);

    /**
     * Returns the matching slots of this face in the provided storage.
     * @param storage The storage to match.
     * @param <T> The type of storage.
     * @param <V> The type of resource.
     * @return The matching slots of this face in the provided storage.
     */
    <T, V extends TransferVariant<T>> int[] getMatching(ConfiguredStorage<T, V> storage);

    /**
     * Write the face to a new nbt compound.
     * @return The nbt compound that was written to.
     */
    NbtCompound writeNbt();

    void readNbt(@NotNull NbtCompound nbt);
}
