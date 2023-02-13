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

import dev.galacticraft.machinelib.api.storage.Deserializable;
import dev.galacticraft.machinelib.api.storage.MutableModifiable;
import dev.galacticraft.machinelib.api.storage.ResourceFilter;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// NO I/O CONSTRAINTS
// Resource must be comparable by identity
public interface ResourceSlot<Resource, Stack> extends MutableModifiable, Deserializable<CompoundTag> {
    // FILTER DOES NOT INCLUDE NULL/EMPTY
    @NotNull ResourceFilter<Resource> getFilter();

    @NotNull ResourceFilter<Resource> getStrictFilter();

    void _setParent(MutableModifiable parent);

    @Nullable Resource getResource();

    long getAmount();

    @Nullable CompoundTag getTag();

    @Nullable CompoundTag copyTag();

    long getCapacity();

    long getCapacityFor(@NotNull Resource resource);

    long getRealCapacity();

    boolean isEmpty();

    boolean isFull();

    // tag CANNOT be mutated!
    @NotNull Stack createStack();

    // tag can be mutated
    @NotNull Stack copyStack();

    boolean canInsert(@NotNull Resource resource);

    boolean canInsert(@NotNull Resource resource, @Nullable CompoundTag tag);

    boolean canInsert(@NotNull Resource resource, long amount);

    boolean canInsert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    boolean canInsertStack(@NotNull Stack stack);

    long tryInsert(@NotNull Resource resource, long amount);

    long tryInsert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    long tryInsertStack(@NotNull Stack stack);

    long insert(@NotNull Resource resource, long amount);

    long insert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    long insertStack(@NotNull Stack stack);

    boolean contains(@NotNull Resource resource);

    boolean contains(@NotNull Resource resource, @Nullable CompoundTag tag);

    boolean canExtract(long amount);

    boolean canExtract(@NotNull Resource resource, long amount);

    boolean canExtract(@NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    long tryExtract(long amount);

    long tryExtract(@Nullable Resource resource, long amount);

    long tryExtract(@Nullable Resource resource, @Nullable CompoundTag tag, long amount);

    boolean extractOne();

    boolean extractOne(@Nullable Resource resource);

    boolean extractOne(@Nullable Resource resource, @Nullable CompoundTag tag);

    long extract(long amount);

    long extract(@Nullable Resource resource, long amount);

    long extract(@Nullable Resource resource, @Nullable CompoundTag tag, long amount);

    // required for FAPI
    long insert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context);

    // required for FAPI
    long extract(@Nullable Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context);

    @Contract("null, !null, _ -> fail")
    void set(@Nullable Resource resource, @Nullable CompoundTag tag, long amount);
    void set(@Nullable Resource resource, long amount);
}
