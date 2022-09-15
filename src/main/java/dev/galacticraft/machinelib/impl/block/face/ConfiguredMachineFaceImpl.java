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

package dev.galacticraft.machinelib.impl.block.face;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.block.face.ConfiguredMachineFace;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.ResourceStorage;
import dev.galacticraft.machinelib.api.storage.exposed.ExposedStorage;
import dev.galacticraft.machinelib.api.storage.io.ConfiguredStorage;
import dev.galacticraft.machinelib.api.storage.io.ResourceFlow;
import dev.galacticraft.machinelib.api.storage.io.ResourceType;
import dev.galacticraft.machinelib.api.storage.io.StorageSelection;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.impl.Constant;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

@ApiStatus.Internal
public final class ConfiguredMachineFaceImpl implements ConfiguredMachineFace {
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
     * When it is an Integer, the face is locked to a single slot/
     * When it is a {@link SlotGroup}, the face is locked to all slots of that type.
     */
    private @Nullable StorageSelection selection = null;

    /**
     * The cached exposed storage of this face.
     */
    private @Nullable Object storage = null;

    public ConfiguredMachineFaceImpl(@NotNull ResourceType type, @NotNull ResourceFlow flow) {
        this.type = type;
        this.flow = flow;
    }

    @Override
    public void setOption(@NotNull ResourceType type, @NotNull ResourceFlow flow) {
        this.type = type;
        this.flow = flow;
        this.selection = null;
        this.storage = null;
    }

    @Override
    public void setSelection(@Nullable StorageSelection selection) {
        this.selection = selection;
        this.storage = null;
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
    public <T, V extends TransferVariant<T>> @Nullable ExposedStorage<T, V> getExposedStorage(@NotNull ResourceStorage<T, V, ?> storage) {
        if (this.getType().willAcceptResource(storage.getResource())) {
            if (this.storage == null) this.storage = storage.getExposedStorage(this.selection, this.flow);
            return (ExposedStorage<T, V>) this.storage;
        }
        return null;
    }

    @Override
    public EnergyStorage getExposedStorage(@NotNull MachineEnergyStorage storage) {
        if (this.getType().willAcceptResource(ResourceType.ENERGY) && this.selection == null) {
            if (this.storage == null) this.storage = storage.getExposedStorage(this.flow);
            return (EnergyStorage) this.storage;
        }
        return null;
    }

    @Override
    public int @NotNull [] getMatching(@Nullable ConfiguredStorage storage) { //SORTED
        if (storage == null) return new int[0];
        if (selection != null) {
            if (selection.isSlot()) {
                return new int[]{selection.getSlot()};
            } else {
                IntList types = new IntArrayList();
                SlotGroup[] slots = storage.getGroups();
                SlotGroup type = selection.getGroup();
                for (int i = 0; i < slots.length; i++) {
                    SlotGroup slot = slots[i];
                    if (slot.isAutomatable() && slot.equals(type)) {
                        if (this.flow != ResourceFlow.INPUT || storage.canExposedInsert(i)) {
                            types.add(i);
                        }
                    }
                }
                return types.toIntArray();
            }
        } else {
            return all(storage);
        }
    }

    private int @NotNull [] all(@NotNull ConfiguredStorage storage) {
        IntList types = new IntArrayList();
        SlotGroup[] slots = storage.getGroups();
        for (int i = 0; i < slots.length; i++) {
            SlotGroup slot = slots[i];
            if (slot.isAutomatable() && (this.flow != ResourceFlow.INPUT || storage.canExposedInsert(i))) {
                types.add(i);
            }
        }
        return types.toIntArray();
    }

    @Override
    public int @NotNull [] getMatchingWild(@Nullable ConfiguredStorage storage) {
        if (storage == null) return new int[0];
        return all(storage);
    }

    @Override
    @Contract(pure = true)
    public @NotNull SlotGroup @NotNull [] getMatchingGroups(@NotNull MachineBlockEntity machine) {
        ConfiguredStorage storage = machine.getStorage(this.getType());
        SlotGroup[] groups; // SLOT TYPE ALL
        if (storage != null) {
            groups = storage.getGroups();
        } else {
            groups = machine.getGroups();
        }

        int count = 0;
        for (SlotGroup group : groups) {
            if (group.isAutomatable()) count++;
        }
        if (count == 0) return new SlotGroup[0];
        int c = 0;
        SlotGroup[] out = new SlotGroup[count];
        for (SlotGroup group : groups) {
            if (group.isAutomatable()) out[c++] = group;
        }
        return out;
    }

    @Override
    public @NotNull CompoundTag writeNbt(@NotNull SlotGroup @NotNull [] groups) {
        CompoundTag nbt = new CompoundTag();
        nbt.putByte(Constant.Nbt.FLOW, (byte) this.flow.ordinal());
        nbt.putByte(Constant.Nbt.RESOURCE, (byte) this.type.ordinal());
        nbt.putBoolean(Constant.Nbt.MATCH, this.selection != null);
        if (this.selection != null) {
            nbt.putBoolean(Constant.Nbt.IS_SLOT_ID, this.selection.isSlot());
            if (this.selection.isSlot()) {
                nbt.putInt(Constant.Nbt.VALUE, this.selection.getSlot());
            } else {
                int idx = -1;
                for (int i = 0; i < groups.length; i++) {
                    if (groups[i] == this.selection.getGroup()) {
                        idx = i;
                        break;
                    }
                }
                if (idx == -1) throw new AssertionError();
                nbt.putInt(Constant.Nbt.VALUE, idx);
            }
        }
        return nbt;
    }

    @Override
    public void readNbt(@NotNull CompoundTag nbt, @NotNull SlotGroup @Nullable [] groups) {
        this.type = ResourceType.getFromOrdinal(nbt.getByte(Constant.Nbt.RESOURCE));
        this.flow = ResourceFlow.values()[nbt.getByte(Constant.Nbt.FLOW)];
        if (nbt.getBoolean(Constant.Nbt.MATCH)) {
            if (nbt.getBoolean(Constant.Nbt.IS_SLOT_ID)) {
                this.selection = StorageSelection.createSlot(nbt.getInt(Constant.Nbt.VALUE));
            } else {
                if (groups != null) { // should only be null for client rendering
                    this.selection = StorageSelection.createGroup(groups[nbt.getInt(Constant.Nbt.VALUE)]);
                } else {
                    this.selection = null;
                }
            }
        } else {
            this.selection = null;
        }
    }
}
