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

import com.google.common.collect.ImmutableList;
import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.block.face.MachineIOFace;
import dev.galacticraft.machinelib.api.fluid.FluidStack;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.ResourceStorage;
import dev.galacticraft.machinelib.api.storage.io.ResourceFlow;
import dev.galacticraft.machinelib.api.storage.io.ResourceType;
import dev.galacticraft.machinelib.api.storage.io.StorageSelection;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
import dev.galacticraft.machinelib.api.transfer.exposed.ExposedStorage;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.MachineLib;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.List;
import java.util.Objects;

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
    /**
     * The filter of this face.
     * <p>
     * When it is null, the face is not filtering.
     */
    private @Nullable StorageSelection selection = null;

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
        this.selection = null;

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
    public @Nullable StorageSelection getSelection() {
        return this.selection;
    }

    @Override
    public void setSelection(@Nullable StorageSelection selection) {
        this.selection = selection;
        this.cachedItemStorage = null;
        this.cachedFluidStorage = null;
        this.cachedEnergyStorage = null;
    }

    @Override
    public @Nullable ExposedStorage<Item, ItemVariant> getExposedItemStorage(@NotNull ResourceStorage<Item, ItemStack, ? extends ResourceSlot<Item, ItemStack>, ? extends SlotGroup<Item, ItemStack, ? extends ResourceSlot<Item, ItemStack>>> storage) {
        if (this.getType().willAcceptResource(ResourceType.ITEM)) {
            if (this.cachedItemStorage == null) {
                this.cachedItemStorage = ExposedStorage.createItem(storage, this.selection, this.flow);
            }
            return this.cachedItemStorage;
        } else {
            assert this.cachedItemStorage == null;
        }
        return null;
    }

    @Override
    public @Nullable ExposedStorage<Fluid, FluidVariant> getExposedFluidStorage(@NotNull ResourceStorage<Fluid, FluidStack, ? extends ResourceSlot<Fluid, FluidStack>, ? extends SlotGroup<Fluid, FluidStack, ? extends ResourceSlot<Fluid, FluidStack>>> storage) {
        if (this.getType().willAcceptResource(ResourceType.FLUID)) {
            if (this.cachedFluidStorage == null) {
                this.cachedFluidStorage = ExposedStorage.createFluid(storage, this.selection, this.flow);
            }
            return this.cachedFluidStorage;
        } else {
            assert this.cachedFluidStorage == null;
        }
        return null;
    }

    @Override
    public @Nullable EnergyStorage getExposedEnergyStorage(@NotNull MachineEnergyStorage storage) {
        if (this.getType().willAcceptResource(ResourceType.ENERGY) && this.selection == null) {
            if (this.cachedEnergyStorage == null) {
                this.cachedEnergyStorage = storage.getExposedStorage(this.flow);
            }
            return this.cachedEnergyStorage;
        }
        return null;
    }

    @Override
    public List<SlotGroupType> getFlowMatchingGroups(MachineBlockEntity machine) {
        List<SlotGroupType> groups = this.type.getStorageGroups(machine);
        if (groups == null) return null;
        for (SlotGroupType type : ImmutableList.copyOf(groups)) { //co-mod
            ResourceFlow flow = type.inputType().getExternalFlow();
            if (flow == null || !this.flow.canFlowIn(flow)) {
                groups.remove(type);
            }
        }
        return groups;
    }

    @Override
    public @NotNull CompoundTag createTag() {
        CompoundTag tag = new CompoundTag();
        tag.putByte(Constant.Nbt.FLOW, (byte) this.flow.ordinal());
        tag.putByte(Constant.Nbt.RESOURCE, (byte) this.type.ordinal());
        if (this.selection != null) {
            tag.putString(Constant.Nbt.GROUP, Objects.requireNonNull(MachineLib.SLOT_GROUP_TYPE_REGISTRY.getKey(this.selection.getGroup())).toString());
            if (this.selection.isSlot()) tag.putInt(Constant.Nbt.SLOT, this.selection.getSlot());
        }

        return tag;
    }

    @Override
    public void readTag(@NotNull CompoundTag tag) {
        this.type = ResourceType.getFromOrdinal(tag.getByte(Constant.Nbt.RESOURCE));
        this.flow = ResourceFlow.getFromOrdinal(tag.getByte(Constant.Nbt.FLOW));
        if (tag.contains(Constant.Nbt.GROUP, CompoundTag.TAG_STRING)) {
            SlotGroupType group = MachineLib.SLOT_GROUP_TYPE_REGISTRY.get(new ResourceLocation(tag.getString(Constant.Nbt.GROUP)));
            assert group != null;
            if (tag.contains(Constant.Nbt.SLOT, Tag.TAG_INT)) {
                this.selection = StorageSelection.create(group);
            } else {
                this.selection = StorageSelection.create(group, tag.getInt(Constant.Nbt.SLOT));
            }
        }

        this.cachedItemStorage = null;
        this.cachedFluidStorage = null;
        this.cachedEnergyStorage = null;
    }

    @Override
    public void writePacket(@NotNull FriendlyByteBuf buf) {
        buf.writeByte(this.type.ordinal()).writeByte(this.flow.ordinal()).writeByte(this.selection != null ? this.selection.isSlot() ? 2 : 1 : 0);
        if (this.selection != null) {
            buf.writeUtf(Objects.requireNonNull(MachineLib.SLOT_GROUP_TYPE_REGISTRY.getKey(this.selection.getGroup())).toString());
            if (this.selection.isSlot()) {
                buf.writeInt(this.selection.getSlot());
            }
        }
    }

    @Override
    public void readPacket(@NotNull FriendlyByteBuf buf) {
        this.type = ResourceType.getFromOrdinal(buf.readByte());
        this.flow = ResourceFlow.getFromOrdinal(buf.readByte());
        byte b = buf.readByte();
        if (b != 0) {
            SlotGroupType group = MachineLib.SLOT_GROUP_TYPE_REGISTRY.get(new ResourceLocation(buf.readUtf()));
            assert group != null;
            if (b == 2) {
                int slot = buf.readInt();
                this.selection = StorageSelection.create(group, slot);
            } else {
                this.selection = StorageSelection.create(group);
            }
        } else {
            this.selection = null;
        }

        this.cachedItemStorage = null;
        this.cachedFluidStorage = null;
        this.cachedEnergyStorage = null;
    }
}
