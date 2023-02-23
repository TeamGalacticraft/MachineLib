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

import dev.galacticraft.machinelib.api.fluid.FluidStack;
import dev.galacticraft.machinelib.api.storage.MutableModifiable;
import dev.galacticraft.machinelib.api.storage.ResourceFilter;
import dev.galacticraft.machinelib.api.storage.SlotProvider;
import dev.galacticraft.machinelib.api.util.Deserializable;
import dev.galacticraft.machinelib.impl.storage.slot.FluidSlotGroupImpl;
import dev.galacticraft.machinelib.impl.storage.slot.ItemSlotGroupImpl;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

// FILTERS (non-strict), has indirect KNOWLEDGE of automation (I/O)
public interface SlotGroup<Resource, Stack, Slot extends ResourceSlot<Resource, Stack>> extends Iterable<Slot>, MutableModifiable, SlotProvider<Resource, Stack, Slot>, Deserializable<ListTag> {
    @Contract(value = " -> new", pure = true)
    static @NotNull Builder<Item, ItemStack, ItemResourceSlot> item() {
        return new Builder<>(SlotGroup::ofItem, ItemResourceSlot[]::new);
    }

    @Contract(value = " -> new", pure = true)
    static @NotNull Builder<Fluid, FluidStack, FluidResourceSlot> fluid() {
        return new Builder<>(SlotGroup::ofFluid, FluidResourceSlot[]::new);
    }

    @Contract("_ -> new")
    @SafeVarargs
    static <Slot extends ResourceSlot<Fluid, FluidStack>> @NotNull SlotGroup<Fluid, FluidStack, Slot> ofFluid(@NotNull Slot... slots) {
        return new FluidSlotGroupImpl<>(slots);
    }

    @Contract("_ -> new")
    @SafeVarargs
    static <Slot extends ResourceSlot<Item, ItemStack>> @NotNull ContainerSlotGroup<Slot> ofItem(@NotNull Slot... slots) {
        return new ItemSlotGroupImpl<>(slots);
    }

    @ApiStatus.Internal
    void _setParent(@NotNull MutableModifiable modifiable);

    int size();

    boolean isEmpty();

    boolean isFull();

    @NotNull ResourceFilter<Resource> getStrictFilter(int slot);

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

    long insertMatching(@NotNull Resource resource, long amount);

    long insertMatching(@NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    boolean containsAny(@NotNull Resource resource);

    boolean containsAny(@NotNull Resource resource, @Nullable CompoundTag tag);

    boolean canExtract(@NotNull Resource resource, long amount);

    boolean canExtract(@NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    long tryExtract(@NotNull Resource resource, long amount);

    long tryExtract(@NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    boolean extractOne(@NotNull Resource resource);

    boolean extractOne(@NotNull Resource resource, @Nullable CompoundTag tag);

    long extract(@NotNull Resource resource, long amount);

    long extract(@NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    // START SLOT METHODS

    @NotNull Slot getSlot(int slot);

    @Nullable ResourceFilter<Resource> getFilter(int slot);

    @Nullable Resource getResource(int slot);

    long getAmount(int slot);

    @Nullable CompoundTag getTag(int slot);

    @Nullable CompoundTag copyTag(int slot);

    long getCapacity(int slot);

    long getCapacityFor(int slot, @NotNull Resource resource);

    long getRealCapacity(int slot);

    boolean isEmpty(int slot);

    boolean isFull(int slot);

    @NotNull Stack createStack(int slot);

    @NotNull Stack copyStack(int slot);

    boolean canInsert(int slot, @NotNull Resource resource);

    boolean canInsert(int slot, @NotNull Resource resource, @Nullable CompoundTag tag);

    boolean canInsert(int slot, @NotNull Resource resource, long amount);

    boolean canInsert(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    boolean canInsertStack(int slot, @NotNull Stack stack);

    long tryInsert(int slot, @NotNull Resource resource, long amount);

    long tryInsert(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    long tryInsertStack(int slot, @NotNull Stack stack);

    long insert(int slot, @NotNull Resource resource, long amount);

    long insert(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    long insertStack(int slot, @NotNull Stack stack);

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

    // required for FAPI
    long insert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context);

    // required for FAPI
    long extract(@Nullable Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context);

    // END SLOT METHODS

    class Builder<Resource, Stack, Slot extends ResourceSlot<Resource, Stack>> {
        private final List<Supplier<Slot>> slots = new ArrayList<>();
        private final Function<Slot[], SlotGroup<Resource, Stack, Slot>> constructor;
        private final IntFunction<Slot[]> arrayProvider;

        private Builder(Function<Slot[], SlotGroup<Resource, Stack, Slot>> constructor, IntFunction<Slot[]> arrayProvider) {
            this.constructor = constructor;
            this.arrayProvider = arrayProvider;
        }

        public @NotNull Builder<Resource, Stack, Slot> add(@NotNull Supplier<Slot> slot) {
            this.slots.add(slot);
            return this;
        }

        public @NotNull SlotGroup<Resource, Stack, Slot> build() {
            if (this.slots.size() == 0) throw new IllegalArgumentException("no slots");
            return this.constructor.apply(this.slots.stream().map(Supplier::get).toArray(this.arrayProvider));
        }
    }
}
