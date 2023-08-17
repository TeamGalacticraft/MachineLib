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

package dev.galacticraft.machinelib.api.storage.slot;

import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.misc.Deserializable;
import dev.galacticraft.machinelib.api.misc.MutableModifiable;
import dev.galacticraft.machinelib.api.storage.StorageAccess;
import dev.galacticraft.machinelib.api.transfer.InputType;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// NO I/O CONSTRAINTS
// Resource must be comparable by identity
// FILTER DOES NOT INCLUDE NULL/EMPTY
public interface ResourceSlot<Resource> extends StorageAccess<Resource>, MutableModifiable, Deserializable<CompoundTag> {
    InputType inputType();

    @Nullable Resource getResource();

    long getAmount();

    @Nullable CompoundTag getTag();

    @Nullable CompoundTag copyTag();

    long getCapacity();

    long getCapacityFor(@NotNull Resource resource);

    long getRealCapacity();

    @NotNull ResourceFilter<Resource> getFilter();

    boolean contains(@NotNull Resource resource);

    boolean contains(@NotNull Resource resource, @Nullable CompoundTag tag);

    boolean canExtract(long amount);

    long tryExtract(long amount);

    @Nullable Resource extractOne();

    long extract(long amount);

    @Contract("null, !null, _ -> fail")
    void set(@Nullable Resource resource, @Nullable CompoundTag tag, long amount);
    void set(@Nullable Resource resource, long amount);

    @ApiStatus.Internal
    void _setParent(MutableModifiable parent);
}
