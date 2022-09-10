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

package dev.galacticraft.api.machine.storage;

import com.google.common.base.Preconditions;
import dev.galacticraft.api.block.entity.MachineBlockEntity;
import dev.galacticraft.api.machine.storage.display.ItemSlotDisplay;
import dev.galacticraft.api.machine.storage.io.ResourceType;
import dev.galacticraft.api.machine.storage.io.SlotGroup;
import dev.galacticraft.api.screen.MachineScreenHandler;
import dev.galacticraft.impl.machine.storage.MachineItemStorageImpl;
import dev.galacticraft.impl.machine.storage.empty.EmptyMachineItemStorage;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public interface MachineItemStorage extends ResourceStorage<Item, ItemVariant, ItemStack> {
    /**
     * Adds slots to a screen handler for display.
     * @param handler The screen handler to add slots to.
     * @param <M> The type of machine.
     */
    <M extends MachineBlockEntity> void addSlots(@NotNull MachineScreenHandler<M> handler);

    /**
     * Player-exposed inventory for screen handlers.
     * @return The player-exposed inventory.
     */
    @NotNull Container playerInventory();

    /**
     * Creates a sub-inventory of the given size starting at {@code 0}.
     * Read only.
     *
     * @param size The size of the sub-inventory.
     * @return The sub-inventory.
     */
    default @NotNull Container subInv(int size) {
        return this.subInv(0, size);
    }

    /**
     * Returns a sub inventory of the given size starting at the given index.
     * Read only.
     *
     * @param start The index to start at.
     * @param size  The size of the sub-inventory.
     * @return The sub-inventory.
     */
    @NotNull Container subInv(int start, int size);

    @Override
    default @NotNull ItemVariant createVariant(@NotNull Item type) {
        return ItemVariant.of(type);
    }

    @Override
    default @NotNull ResourceType getResource() {
        return ResourceType.ITEM;
    }

    /**
     * Returns the default empty storage.
     * @return The default empty storage.
     */
    static @NotNull MachineItemStorage empty() {
        return EmptyMachineItemStorage.INSTANCE;
    }

    /**
     * Creates a new item storage builder.
     * @return The new item storage builder.
     */
    @Contract(value = " -> new", pure = true)
    static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * A builder for creating a new item storage.
     */
    class Builder {
        private int size = 0;
        private final List<@NotNull SlotGroup> types = new ArrayList<>();
        private final List<@NotNull ItemSlotDisplay> displays = new ArrayList<>();
        private final List<@NotNull Predicate<ItemVariant>> filters = new ArrayList<>();
        private final IntList capacities = new IntArrayList();
        private final BooleanList insertion = new BooleanArrayList();

        public Builder() {}

        @Contract(value = " -> new", pure = true)
        public static @NotNull Builder create() {
            return new Builder();
        }

        /**
         * Adds a slot to the builder.
         * @param type The type of slot.
         * @param display The display for the slot.
         * @return The builder.
         */
        public @NotNull Builder addSlot(@NotNull SlotGroup type, @NotNull Predicate<ItemVariant> filter, boolean insertion, @NotNull ItemSlotDisplay display) {
            return this.addSlot(type, filter, insertion, 64, display);
        }

        /**
         * Adds a slot to the builder.
         * @param type The type of slot.
         * @param capacity The maximum count of items in the slot. Clamped to {@code 64} and cannot be negative.
         * @param display The display for the slot.
         * @return The builder.
         */
        public @NotNull Builder addSlot(@NotNull SlotGroup type, @NotNull Predicate<ItemVariant> filter, boolean insertion, int capacity, @NotNull ItemSlotDisplay display) {
            Preconditions.checkNotNull(type);
            Preconditions.checkNotNull(display);
            StoragePreconditions.notNegative(capacity);
            if (capacity > 64) {
                throw new IllegalArgumentException("Capacity cannot be greater than 64!");
            }

            this.size++;
            this.types.add(type);
            this.displays.add(display);
            this.filters.add(filter);
            this.insertion.add(insertion);
            this.capacities.add(capacity);
            return this;
        }

        /**
         * Builds the item storage.
         * @return The item storage.
         */
        @Contract(pure = true, value = " -> new")
        public @NotNull MachineItemStorage build() {
            if (this.size == 0) return empty();
            return new MachineItemStorageImpl(this.size, this.types.toArray(new SlotGroup[0]), this.filters.toArray(new Predicate[0]), this.insertion.toBooleanArray(), this.capacities.toIntArray(), this.displays.toArray(new ItemSlotDisplay[0]));
        }
    }
}
