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

package dev.galacticraft.machinelib.api.storage;

import dev.galacticraft.machinelib.api.menu.sync.MenuSynchronizable;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.util.Deserializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ResourceStorage<Resource, Slot extends ResourceSlot<Resource>> extends Iterable<Slot>, MutableModifiable, SlotProvider<Resource, Slot>, Deserializable<ListTag>, MenuSynchronizable {
    void setListener(Runnable listener);

    int size();

    Slot[] getSlots();

    // START SLOT METHODS

    @NotNull Slot getSlot(int slot);

    @Nullable ResourceFilter<Resource> getFilter(int slot);

    @NotNull ResourceFilter<Resource> getStrictFilter(int slot);

    @Nullable Resource getResource(int slot);

    long getAmount(int slot);

    @Nullable CompoundTag getTag(int slot);

    @Nullable CompoundTag copyTag(int slot);

    long getCapacity(int slot);

    long getCapacityFor(int slot, @NotNull Resource resource);

    long getRealCapacity(int slot);

    boolean isEmpty(int slot);

    boolean isFull(int slot);

    boolean canInsert(int slot, @NotNull Resource resource);

    boolean canInsert(int slot, @NotNull Resource resource, @Nullable CompoundTag tag);

    boolean canInsert(int slot, @NotNull Resource resource, long amount);

    boolean canInsert(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    long tryInsert(int slot, @NotNull Resource resource, long amount);

    long tryInsert(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    long insert(int slot, @NotNull Resource resource, long amount);

    long insert(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    boolean containsAny(int slot, @NotNull Resource resource);

    boolean containsAny(int slot, @NotNull Resource resource, @Nullable CompoundTag tag);

    boolean canExtract(int slot, long amount);

    boolean canExtract(int slot, @NotNull Resource resource, long amount);

    boolean canExtract(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    long tryExtract(int slot, long amount);

    long tryExtract(int slot, @Nullable Resource resource, long amount);

    long tryExtract(int slot, @Nullable Resource resource, @Nullable CompoundTag tag, long amount);

    boolean extractOne(int slot);

    boolean extractOne(int slot, @Nullable Resource resource);

    boolean extractOne(int slot, @Nullable Resource resource, @Nullable CompoundTag tag);

    long extract(int slot, long amount);

    long extract(int slot, @Nullable Resource resource, long amount);

    long extract(int slot, @Nullable Resource resource, @Nullable CompoundTag tag, long amount);

    // END SLOT METHODS
}
