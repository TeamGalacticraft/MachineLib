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

package dev.galacticraft.machinelib.impl.block.face;

import dev.galacticraft.machinelib.api.compat.transfer.ExposedStorage;
import dev.galacticraft.machinelib.api.machine.configuration.MachineIOFace;
import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.impl.menu.sync.MachineIOFaceSyncHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

@ApiStatus.Internal
public final class DirectionlessMachineIOFaceImpl implements MachineIOFace {
    private @Nullable ExposedStorage<Item, ItemVariant> cachedItemStorage = null;
    private @Nullable ExposedStorage<Fluid, FluidVariant> cachedFluidStorage = null;
    private @Nullable EnergyStorage cachedEnergyStorage = null;

    public DirectionlessMachineIOFaceImpl() {
    }

    @Override
    public void setOption(@NotNull ResourceType type, @NotNull ResourceFlow flow) {
    }

    @Override
    public @NotNull ResourceType getType() {
        return ResourceType.ANY;
    }

    @Override
    public @NotNull ResourceFlow getFlow() {
        return ResourceFlow.BOTH;
    }

    @Override
    public @Nullable ExposedStorage<Item, ItemVariant> getExposedItemStorage(@NotNull StorageProvider<Item, ItemVariant> provider) {
        if (this.cachedItemStorage == null) {
            this.cachedItemStorage = provider.createExposedStorage(ResourceFlow.BOTH);
        }
        return this.cachedItemStorage;
    }

    @Override
    public @Nullable ExposedStorage<Fluid, FluidVariant> getExposedFluidStorage(@NotNull StorageProvider<Fluid, FluidVariant> provider) {
        if (this.cachedFluidStorage == null) {
            this.cachedFluidStorage = provider.createExposedStorage(ResourceFlow.BOTH);
        }
        return this.cachedFluidStorage;
    }

    @Override
    public @Nullable EnergyStorage getExposedEnergyStorage(@NotNull MachineEnergyStorage storage) {
        if (this.cachedEnergyStorage == null) {
            this.cachedEnergyStorage = storage.getExposedStorage(ResourceFlow.BOTH);
        }
        return this.cachedEnergyStorage;
    }

    @Override
    public @NotNull CompoundTag createTag() {
        return new CompoundTag();
    }

    @Override
    public void readTag(@NotNull CompoundTag tag) {
    }

    @Override
    public void writePacket(@NotNull FriendlyByteBuf buf) {
    }

    @Override
    public void readPacket(@NotNull FriendlyByteBuf buf) {
    }

    @Contract(" -> new")
    @Override
    public @NotNull MenuSyncHandler createSyncHandler() {
        return new MachineIOFaceSyncHandler(this);
    }
}
