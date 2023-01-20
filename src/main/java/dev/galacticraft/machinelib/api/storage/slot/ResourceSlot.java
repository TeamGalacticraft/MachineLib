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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// HAS FILTERS, BUT NO I/O CONSTRAINTS
// Resource must be comparable by identity
public interface ResourceSlot<Resource, Stack> extends MutableModifiable, Deserializable<CompoundTag> {
    // FILTER DOES NOT INCLUDE NULL/EMPTY
    @NotNull ResourceFilter<Resource> getFilter();

    @NotNull ResourceFilter<Resource> getExternalFilter();

    @NotNull SlotGroup<Resource, Stack, ? extends ResourceSlot<Resource, Stack>> getGroup();

    void _setGroup(SlotGroup<Resource, Stack, ? extends ResourceSlot<Resource, Stack>> group);

    @Nullable Resource getResource();

    long getAmount();

    @Nullable CompoundTag getTag();

    @Nullable CompoundTag copyTag();

    long getCapacity();

    long getRealCapacity();

    boolean isEmpty();

    boolean isFull();

    // tag CANNOT be mutated!
    @NotNull Stack createStack();

    // tag can be mutated
    @NotNull Stack copyStack();

    default boolean insertOne(@NotNull Resource resource) {
        return this.insertOne(resource, (TransactionContext) null);
    }

    boolean insertOne(@NotNull Resource resource, @Nullable TransactionContext context);

    default boolean insertOne(@NotNull Resource resource, @Nullable CompoundTag tag) {
        return this.insertOne(resource, tag, null);
    }

    boolean insertOne(@NotNull Resource resource, @Nullable CompoundTag tag, @Nullable TransactionContext context);

    default long insert(@NotNull Resource resource, long amount) {
        return this.insert(resource, amount, null);
    }

    long insert(@NotNull Resource resource, long amount, @Nullable TransactionContext context);

    default long insert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        return this.insert(resource, amount, null);
    }

    long insert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context);

    default boolean extractOne() {
        return this.extractOne((TransactionContext) null);
    }

    boolean extractOne(@Nullable TransactionContext context);

    default boolean extractOne(@Nullable Resource resource) {
        return this.extractOne(resource, (TransactionContext) null);
    }

    boolean extractOne(@Nullable Resource resource, @Nullable TransactionContext context);

    default boolean extractOne(@Nullable Resource resource, @Nullable CompoundTag tag) {
        return this.extractOne(resource, tag, null);
    }

    boolean extractOne(@Nullable Resource resource, @Nullable CompoundTag tag, @Nullable TransactionContext context);

    default long extract(long amount) {
        return this.extract(amount, null);
    }

    long extract(long amount, @Nullable TransactionContext context);

    default long extract(@Nullable Resource resource, long amount) {
        return this.extract(resource, amount, null);
    }

    long extract(@Nullable Resource resource, long amount, @Nullable TransactionContext context);

    default long extract(@Nullable Resource resource, @Nullable CompoundTag tag, long amount) {
        return this.extract(resource, tag, amount, null);
    }

    long extract(@Nullable Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context);

    boolean contains(@NotNull Resource resource);

    boolean contains(@NotNull Resource resource, @Nullable CompoundTag tag);

    void set(@Nullable Resource resource, @Nullable CompoundTag tag, long amount);

}
