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
import dev.galacticraft.machinelib.api.storage.SlotProvider;
import dev.galacticraft.machinelib.impl.storage.slot.ContainerSlotGroupImpl;
import dev.galacticraft.machinelib.impl.storage.slot.SlotGroupImpl;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// FILTERS (non-strict), has indirect KNOWLEDGE of automation (I/O)
public interface SlotGroup<Resource, Stack, Slot extends ResourceSlot<Resource, Stack>> extends Iterable<Slot>, MutableModifiable, SlotProvider<Resource, Stack, Slot>, Deserializable<ListTag> {
    @Contract(value = "_ -> new", pure = true)
    static @NotNull <Resource, Stack, Slot extends ResourceSlot<Resource, Stack>> Builder<Resource, Stack, Slot> create(@NotNull SlotGroupType type) {
        return new Builder<>(type);
    }

//    @Contract(value = "_, _ -> new", pure = true)
//    static <Resource, Stack, Slot extends ResourceSlot<Resource, Stack>> @NotNull SlotGroup<Resource, Stack, Slot> singleton(@NotNull SlotGroupType type, @NotNull Slot slot) {
//        return new SingletonResourceSlotGroupImpl<>(type, slot);
//    }

    @Contract("_, _ -> new")
    @SafeVarargs
    static <Resource, Stack, Slot extends ResourceSlot<Resource, Stack>> @NotNull SlotGroup<Resource, Stack, Slot> of(@NotNull SlotGroupType type, @NotNull Slot... slots) {
//        if (slots.length == 1) return new SingletonResourceSlotGroupImpl<>(type, slots[0]);
        return new SlotGroupImpl<>(type, slots);
    }

    @Contract("_, _ -> new")
    @SafeVarargs
    static <Slot extends ResourceSlot<Item, ItemStack>> @NotNull ContainerSlotGroup<Slot> crafting(@NotNull SlotGroupType type, @NotNull Slot... slots) {
        return new ContainerSlotGroupImpl<>(type, slots);
    }

    void _setParent(@NotNull MutableModifiable modifiable);

    @NotNull SlotGroupType getType();

    int size();

    boolean isEmpty();

    boolean isFull();

    @NotNull ResourceFilter<Resource> getStrictFilter(int slot);

    boolean canInsertOne(@NotNull Resource resource);

    boolean canInsertOne(@NotNull Resource resource, @Nullable CompoundTag tag);

    boolean canInsert(@NotNull Resource resource, long amount);

    boolean canInsert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    long tryInsert(@NotNull Resource resource, long amount);

    long tryInsert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    boolean insertOne(@NotNull Resource resource);

    boolean insertOne(@NotNull Resource resource, @Nullable CompoundTag tag);

    long insert(@NotNull Resource resource, long amount);

    long insert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount);

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

    long getRealCapacity(int slot);

    @NotNull Stack createStack(int slot);

    @NotNull Stack copyStack(int slot);

    boolean canInsertOne(int slot, @NotNull Resource resource);

    boolean canInsertOne(int slot, @NotNull Resource resource, @Nullable CompoundTag tag);

    boolean canInsert(int slot, @NotNull Resource resource, long amount);

    boolean canInsert(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    long tryInsert(int slot, @NotNull Resource resource, long amount);

    long tryInsert(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    boolean insertOne(int slot, @NotNull Resource resource);

    boolean insertOne(int slot, @NotNull Resource resource, @Nullable CompoundTag tag);

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

    // required for FAPI
    long insert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context);

    // required for FAPI
    long extract(@Nullable Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context);

    // END SLOT METHODS

    class Builder<Resource, Stack, Slot extends ResourceSlot<Resource, Stack>> {
        private final @NotNull SlotGroupType type;
        private final List<Slot> slots = new ArrayList<>();

        private Builder(@NotNull SlotGroupType type) {
            this.type = type;
        }

        public @NotNull Builder<Resource, Stack, Slot> add(Slot slot) {
            if (slot == null) return this;
            this.slots.add(slot);
            return this;
        }

        public @Nullable SlotGroup<Resource, Stack, Slot> build() {
            if (this.slots.isEmpty()) return null;
            if (this.slots.get(0) instanceof ItemResourceSlot) {
                return (SlotGroup<Resource, Stack, Slot>) crafting(this.type, this.slots.toArray(new ItemResourceSlot[0]));
            } else if (this.slots.get(0) instanceof FluidResourceSlot) {
                return (SlotGroup<Resource, Stack, Slot>) of(this.type, this.slots.toArray(new FluidResourceSlot[0]));
            } else {
                throw new UnsupportedOperationException();
            }
//            return SlotGroup.of(this.type, this.slots.toArray(new Slot[0]));
        }
    }
}
