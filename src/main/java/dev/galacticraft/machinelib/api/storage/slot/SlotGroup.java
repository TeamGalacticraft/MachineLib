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

import dev.galacticraft.machinelib.api.storage.*;
import dev.galacticraft.machinelib.impl.storage.slot.CraftingSlotGroupImpl;
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

// FILTERS, has KNOWLEDGE of automation (I/O)
public interface SlotGroup<Resource, Stack, Slot extends ResourceSlot<Resource, Stack>> extends Iterable<Slot>, Modifiable, SlotProvider<Resource, Stack>, Deserializable<ListTag> {
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
    static <Slot extends ResourceSlot<Item, ItemStack>> @NotNull CraftingSlotGroup<Slot> crafting(@NotNull SlotGroupType type, @NotNull Slot... slots) {
        return new CraftingSlotGroupImpl<>(type, slots);
    }

    void _setParent(@NotNull MutableModifiable modifiable);

    @NotNull SlotGroupType getType();

    int size();

    boolean isEmpty();

    boolean isFull();

    @NotNull ResourceFilter<Resource> getExternalFilter(int slot);

    default long insert(@NotNull Resource resource, long amount) {
        return this.insert(resource, amount, null);
    }

    long insert(@NotNull Resource resource, long amount, @Nullable TransactionContext context);

    default long insert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        return this.insert(resource, tag, amount, null);
    }

    long insert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context);

    default long extract(@Nullable Resource resource, long amount) {
        return this.extract(resource, amount, null);
    }

    long extract(@Nullable Resource resource, long amount, @Nullable TransactionContext context);

    default long extract(@Nullable Resource resource, @Nullable CompoundTag tag, long amount) {
        return this.extract(resource, tag, amount, null);
    }

    long extract(@Nullable Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context);

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

    default boolean insertOne(int slot, @NotNull Resource resource) {
        return this.insertOne(slot, resource, (TransactionContext) null);
    }

    boolean insertOne(int slot, @NotNull Resource resource, @Nullable TransactionContext context);

    default boolean insertOne(int slot, @NotNull Resource resource, @Nullable CompoundTag tag) {
        return this.insertOne(slot, resource, tag, null);
    }

    boolean insertOne(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, @Nullable TransactionContext context);

    default long insert(int slot, @NotNull Resource resource, long amount) {
        return this.insert(slot, resource, amount, null);
    }

    long insert(int slot, @NotNull Resource resource, long amount, @Nullable TransactionContext context);

    default long insert(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        return this.insert(slot, resource, amount, null);
    }

    long insert(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context);

    default boolean extractOne(int slot) {
        return this.extractOne(slot, (TransactionContext) null);
    }

    boolean extractOne(int slot, @Nullable TransactionContext context);

    default boolean extractOne(int slot, @Nullable Resource resource) {
        return this.extractOne(slot, resource, (TransactionContext) null);
    }

    boolean extractOne(int slot, @Nullable Resource resource, @Nullable TransactionContext context);

    default boolean extractOne(int slot, @Nullable Resource resource, @Nullable CompoundTag tag) {
        return this.extractOne(slot, resource, tag, null);
    }

    boolean extractOne(int slot, @Nullable Resource resource, @Nullable CompoundTag tag, @Nullable TransactionContext context);

    default long extract(int slot, long amount) {
        return this.extract(slot, amount, null);
    }

    long extract(int slot, long amount, @Nullable TransactionContext context);

    default long extract(int slot, @Nullable Resource resource, long amount) {
        return this.extract(slot, resource, amount, null);
    }

    long extract(int slot, @Nullable Resource resource, long amount, @Nullable TransactionContext context);

    default long extract(int slot, @Nullable Resource resource, @Nullable CompoundTag tag, long amount) {
        return this.extract(slot, resource, tag, amount, null);
    }

    long extract(int slot, @Nullable Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context);

    boolean contains(int slot, @NotNull Resource resource);

    boolean contains(int slot, @NotNull Resource resource, @Nullable CompoundTag tag);

    // END SLOT METHODS

    class Builder<Resource, Stack, Slot extends ResourceSlot<Resource, Stack>> {
        private final @NotNull SlotGroupType type;
        private final List<Slot> slots = new ArrayList<>();

        private Builder(@NotNull SlotGroupType type) {
            this.type = type;
        }

        public @NotNull Builder<Resource, Stack, Slot> add(Slot slot) {
            this.slots.add(slot);
            return this;
        }

        public SlotGroup<Resource, Stack, Slot> build() {
            return SlotGroup.of(this.type, this.slots.toArray(new ResourceSlot[0]));
        }
    }
}
