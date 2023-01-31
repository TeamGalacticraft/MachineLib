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

package dev.galacticraft.machinelib.impl.storage;

import com.google.common.collect.Iterators;
import dev.galacticraft.machinelib.api.fluid.FluidStack;
import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
import dev.galacticraft.machinelib.impl.menu.sync.ResourceStorageSyncHandler;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MachineFluidStorageImpl implements MachineFluidStorage {
    private final SlotGroup<Fluid, FluidStack, FluidResourceSlot>[] groups;
    private final SlotGroupType[] types;
    private final Map<SlotGroupType, SlotGroup<Fluid, FluidStack, FluidResourceSlot>> typeToGroup;
    private final FluidResourceSlot[] clumpedSlots;
    private TransactionContext cachedTransaction = null;
    private long modifications = 0;
    private Runnable listener;

    public MachineFluidStorageImpl(SlotGroup<Fluid, FluidStack, FluidResourceSlot>[] groups) {
        this.groups = groups;
        this.typeToGroup = new HashMap<>(this.groups.length);
        this.types = new SlotGroupType[this.groups.length];
        int slots = 0;
        for (int i = 0; i < this.groups.length; i++) {
            SlotGroup<Fluid, FluidStack, FluidResourceSlot> group = this.groups[i];
            group._setParent(this);
            this.typeToGroup.put(group.getType(), group);
            this.types[i] = group.getType();
            slots += group.getSlots().length;
        }
        this.clumpedSlots = new FluidResourceSlot[slots];
        slots = 0;
        for (SlotGroup<Fluid, FluidStack, FluidResourceSlot> group : this.groups) {
            for (FluidResourceSlot slot : group.getSlots()) {
                this.clumpedSlots[slots++] = slot;
            }
        }
    }

    @Override
    public long getModifications() {
        return this.modifications;
    }

    @Override
    public int groups() {
        return this.groups.length;
    }

    @Override
    public @NotNull SlotGroup<Fluid, FluidStack, FluidResourceSlot> getGroup(@NotNull SlotGroupType type) {
        SlotGroup<Fluid, FluidStack, FluidResourceSlot> group = this.typeToGroup.get(type);
        assert group != null;
        return group;
    }

    @Override
    public @NotNull FluidResourceSlot getSlot(@NotNull SlotGroupType type) {
        SlotGroup<Fluid, FluidStack, FluidResourceSlot> group = this.getGroup(type);
        assert group.size() == 1;
        return group.getSlot(0);
    }

    @Override
    public @NotNull SlotGroupType @NotNull [] getTypes() {
        return this.types;
    }

    @Override
    public void setListener(Runnable listener) {
        this.listener = listener;
    }

    @Override
    public FluidResourceSlot[] getSlots() {
        return this.clumpedSlots;
    }

    @NotNull
    @Override
    public Iterator<SlotGroup<Fluid, FluidStack, FluidResourceSlot>> iterator() {
        return Iterators.forArray(this.groups);
    }

    @Override
    public void revertModification() {
        this.modifications--;
    }

    @Override
    public void markModified(@Nullable TransactionContext context) {
        if (context != null) {
            this.modifications++;
            context.addCloseCallback((context1, result) -> {
                if (result.wasAborted()) {
                    this.modifications--;
                } else {
                    if (this.listener != null) {
                        TransactionContext outer = context1.nestingDepth() != 0 ? context1.getOpenTransaction(0) : context1;
                        if (this.cachedTransaction != outer) {
                            this.cachedTransaction = outer;
                            context.addOuterCloseCallback((result1) -> {
                                if (result1.wasCommitted()) this.listener.run();
                            });
                        }
                    }
                }
            });
        } else {
            this.markModified();
        }
    }

    @Override
    public void markModified() {
        this.modifications++;
        if (this.listener != null) this.listener.run();
    }

    @Override
    public @NotNull ListTag createTag() {
        ListTag tag = new ListTag();
        for (SlotGroup<Fluid, FluidStack, FluidResourceSlot> group : this.groups) {
            tag.add(group.createTag());
        }
        return tag;
    }

    @Override
    public void readTag(@NotNull ListTag tag) {
        for (int i = 0; i < tag.size(); i++) {
            this.groups[i].readTag(tag.getList(i));
        }
    }

    @Override
    public void writePacket(@NotNull FriendlyByteBuf buf) {
        for (SlotGroup<Fluid, FluidStack, FluidResourceSlot> group : this.groups) {
            group.writePacket(buf);
        }
    }

    @Override
    public void readPacket(@NotNull FriendlyByteBuf buf) {
        for (SlotGroup<Fluid, FluidStack, FluidResourceSlot> group : this.groups) {
            group.readPacket(buf);
        }
    }

    @Override
    public @Nullable MenuSyncHandler createSyncHandler() {
        return new ResourceStorageSyncHandler<>(this);
    }
}
