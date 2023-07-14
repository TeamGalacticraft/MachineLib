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
import dev.galacticraft.machinelib.api.storage.ResourceStorage;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.impl.Constant;
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
public final class MachineIOFaceImpl implements MachineIOFace {
    /**
     * The type of resource that this face is configured to accept.
     */
    private @NotNull ResourceType type;
    /**
     * The flow direction of this face.
     */
    private @NotNull ResourceFlow flow;

    private @Nullable ExposedStorage<Item, ItemVariant> cachedItemStorage = null;
    private @Nullable ExposedStorage<Fluid, FluidVariant> cachedFluidStorage = null;
    private @Nullable EnergyStorage cachedEnergyStorage = null;

    public MachineIOFaceImpl(@NotNull ResourceType type, @NotNull ResourceFlow flow) {
        this.type = type;
        this.flow = flow;
    }

    @Override
    public void setOption(@NotNull ResourceType type, @NotNull ResourceFlow flow) {
        this.type = type;
        this.flow = flow;

        this.cachedItemStorage = null;
        this.cachedFluidStorage = null;
        this.cachedEnergyStorage = null;
    }

    @Override
    public @NotNull ResourceType getType() {
        return this.type;
    }

    @Override
    public @NotNull ResourceFlow getFlow() {
        return this.flow;
    }

    @Override
    public @Nullable ExposedStorage<Item, ItemVariant> getExposedItemStorage(@NotNull ResourceStorage<Item, ? extends ResourceSlot<Item>> storage) {
        if (this.getType().willAcceptResource(ResourceType.ITEM)) {
            if (this.cachedItemStorage == null) {
                this.cachedItemStorage = ExposedStorage.createItem(storage, this.flow);
            }
            return this.cachedItemStorage;
        } else {
            assert this.cachedItemStorage == null;
        }
        return null;
    }

    @Override
    public @Nullable ExposedStorage<Fluid, FluidVariant> getExposedFluidStorage(@NotNull ResourceStorage<Fluid, ? extends ResourceSlot<Fluid>> storage) {
        if (this.getType().willAcceptResource(ResourceType.FLUID)) {
            if (this.cachedFluidStorage == null) {
                this.cachedFluidStorage = ExposedStorage.createFluid(storage, this.flow);
            }
            return this.cachedFluidStorage;
        } else {
            assert this.cachedFluidStorage == null;
        }
        return null;
    }

    @Override
    public @Nullable EnergyStorage getExposedEnergyStorage(@NotNull MachineEnergyStorage storage) {
        if (this.getType().willAcceptResource(ResourceType.ENERGY)) {
            if (this.cachedEnergyStorage == null) {
                this.cachedEnergyStorage = storage.getExposedStorage(this.flow);
            }
            return this.cachedEnergyStorage;
        }
        return null;
    }

    @Override
    public @NotNull CompoundTag createTag() {
        CompoundTag tag = new CompoundTag();
        tag.putByte(Constant.Nbt.FLOW, (byte) this.flow.ordinal());
        tag.putByte(Constant.Nbt.RESOURCE, (byte) this.type.ordinal());

        return tag;
    }

    @Override
    public void readTag(@NotNull CompoundTag tag) {
        this.type = ResourceType.getFromOrdinal(tag.getByte(Constant.Nbt.RESOURCE));
        this.flow = ResourceFlow.getFromOrdinal(tag.getByte(Constant.Nbt.FLOW));

        this.cachedItemStorage = null;
        this.cachedFluidStorage = null;
        this.cachedEnergyStorage = null;
    }

    @Override
    public void writePacket(@NotNull FriendlyByteBuf buf) {
        buf.writeByte(this.type.ordinal()).writeByte(this.flow.ordinal());
    }

    @Override
    public void readPacket(@NotNull FriendlyByteBuf buf) {
        this.type = ResourceType.getFromOrdinal(buf.readByte());
        this.flow = ResourceFlow.getFromOrdinal(buf.readByte());

        this.cachedItemStorage = null;
        this.cachedFluidStorage = null;
        this.cachedEnergyStorage = null;
    }

    @Contract(" -> new")
    @Override
    public @NotNull MenuSyncHandler createSyncHandler() {
        return new MachineIOFaceSyncHandler(this);
    }
}
