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
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MachineItemStorageImpl implements MachineItemStorage {
    private final SlotGroup<Item, ItemStack, ItemResourceSlot>[] groups;
    private final SlotGroupType[] types;
    private final Map<SlotGroupType, SlotGroup<Item, ItemStack, ItemResourceSlot>> typeToGroup;
    private final ResourceSlot<Item, ItemStack>[] clumpedSlots;
    private long modifications = 0;

    public MachineItemStorageImpl(SlotGroup<Item, ItemStack, ItemResourceSlot>[] groups) {
        this.groups = groups;
        this.typeToGroup = new HashMap<>(this.groups.length);
        this.types = new SlotGroupType[this.groups.length];
        int slots = 0;
        for (int i = 0; i < this.groups.length; i++) {
            SlotGroup<Item, ItemStack, ItemResourceSlot> group = this.groups[i];
            this.typeToGroup.put(group.getType(), group);
            this.types[i] = group.getType();
            slots += group.getSlots().length;
        }
        this.clumpedSlots = new ResourceSlot[slots];
        slots = 0;
        for (SlotGroup<Item, ItemStack, ItemResourceSlot> group : this.groups) {
            for (ResourceSlot<Item, ItemStack> slot : group.getSlots()) {
                this.clumpedSlots[slots++] = slot;
            }
        }
    }

    @Override
    public long getModifications() {
        return this.modifications;
    }

    @Override
    public int size() {
        return this.groups.length;
    }

    @Override
    public @NotNull SlotGroup<Item, ItemStack, ItemResourceSlot> getGroup(@NotNull SlotGroupType type) {
        SlotGroup<Item, ItemStack, ItemResourceSlot> group = this.typeToGroup.get(type);
        assert group != null;
        return group;
    }

    @Override
    public @NotNull SlotGroupType @NotNull [] getTypes() {
        return this.types;
    }

    @Override
    public ResourceSlot<Item, ItemStack>[] getSlots() {
        return this.clumpedSlots;
    }

    @NotNull
    @Override
    public Iterator<SlotGroup<Item, ItemStack, ItemResourceSlot>> iterator() {
        return Iterators.forArray(this.groups);
    }

    @Override
    public void revertModification() {
        this.modifications--;
    }

    @Override
    public void markModified() {
        this.modifications++;
    }

    @Override
    public @NotNull ListTag createTag() {
        ListTag tag = new ListTag();
        for (SlotGroup<Item, ItemStack, ItemResourceSlot> group : this.groups) {
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
        for (SlotGroup<Item, ItemStack, ItemResourceSlot> group : this.groups) {
            group.writePacket(buf);
        }
    }

    @Override
    public void readPacket(@NotNull FriendlyByteBuf buf) {
        for (SlotGroup<Item, ItemStack, ItemResourceSlot> group : this.groups) {
            group.readPacket(buf);
        }
    }

    @Override
    public @NotNull Container getCraftingView(@NotNull SlotGroupType type) {
        return ((Container) this.getGroup(type));
    }
}
