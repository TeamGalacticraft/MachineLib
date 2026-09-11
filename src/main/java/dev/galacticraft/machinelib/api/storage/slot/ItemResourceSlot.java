/*
 * Copyright (c) 2021-2026 Team Galacticraft
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

import com.mojang.datafixers.util.Pair;
import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.filter.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import dev.galacticraft.machinelib.impl.compat.vanilla.FakeRecipeHolder;
import dev.galacticraft.machinelib.impl.storage.slot.ItemResourceSlotImpl;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A resource slot that stores items.
 */
public interface ItemResourceSlot extends ResourceSlot<Item>, ContainerItemContext, FakeRecipeHolder {
    @Contract("_ -> new")
    static @NotNull Spec builder(TransferType transferType) {
        return new Spec(transferType);
    }

    @Contract("_, _, _ -> new")
    static @NotNull ItemResourceSlot create(@NotNull TransferType transferType, @Nullable ItemSlotDisplay display, @NotNull ResourceFilter<Item> filter) {
        return create(transferType, display, filter, 64);
    }

    @Contract("_, _, _, _ -> new")
    static @NotNull ItemResourceSlot create(@NotNull TransferType transferType, @Nullable ItemSlotDisplay display, @NotNull ResourceFilter<Item> filter, int capacity) {
        if (capacity < 0 || capacity > 64) throw new IllegalArgumentException();
        return new ItemResourceSlotImpl(transferType, display, filter, capacity);
    }

    /**
     * Consumes one item from the slot.
     * Similar to {@link #extractOne()} but will replace the item with its remainder.
     *
     * @return the item that was consumed, or {@code null} if the slot was empty
     */
    @Nullable
    Item consumeOne();

    /**
     * Consumes one item of the specified type from the slot.
     * Similar to {@link #extractOne(Object)} but will replace the item with its remainder.
     *
     * @param resource the item type to consume
     * @return {@code true} if the item was consumed, {@code false} otherwise
     */
    default boolean consumeOne(@NotNull Item resource) {
        return this.consumeOne(resource, null);
    }

    /**
     * Consumes one item of the specified type and components from the slot.
     * Similar to {@link #extractOne(Object, DataComponentPatch)} but will replace the item with its remainder.
     *
     * @param resource the item type to consume
     * @param components the components to match. If {@code null}, any components will match
     * @return {@code true} if the item was consumed, {@code false} otherwise
     */
    boolean consumeOne(@NotNull Item resource, @Nullable DataComponentPatch components);

    /**
     * Consumes the specified number of items from the slot.
     * Similar to {@link #extract(long)} but will replace the item with its remainder.
     *
     * @param amount the number of items to consume
     * @return the number of items that were actually consumed
     */
    long consume(long amount);

    /**
     * Consumes the specified number of items of the specified type from the slot.
     * Similar to {@link #extract(Object, long)} but will replace the item with its remainder.
     *
     * @param resource the item type to consume
     * @param amount the number of items to consume
     * @return the number of items that were actually consumed
     */
    default long consume(@NotNull Item resource, long amount) {
        return this.consume(resource, null, amount);
    }

    /**
     * Consumes the specified number of items of the specified type and components from the slot.
     * Similar to {@link #extract(Object, DataComponentPatch, long)} but will replace the item with its remainder.
     *
     * @param resource the item type to consume
     * @param components the components to match. If {@code null}, any components will match
     * @param amount the number of items to consume
     * @return the number of items that were actually consumed
     */
    long consume(@NotNull Item resource, @Nullable DataComponentPatch components, long amount);

    /**
     * {@return the display properties of this slot, or {@code null} if hidden}
     */
    @Nullable
    ItemSlotDisplay getDisplay();

    // required to merge ContainerItemContext#getAmount with ResourceSlot#getAmount
    @Override
    long getAmount();

    final class Spec {
        private final TransferType transferType;

        private boolean hidden = false;
        private int x = 0;
        private int y = 0;
        private @Nullable Pair<ResourceLocation, ResourceLocation> icon = null;

        private ResourceFilter<Item> filter = ResourceFilters.any();
        private int capacity = 64;

        @Contract(pure = true)
        private Spec(TransferType transferType) {
            this.transferType = transferType;
        }

        @Contract("_, _ -> this")
        public @NotNull Spec pos(int x, int y) {
            if (this.hidden) throw new UnsupportedOperationException("hidden");
            this.x = x;
            this.y = y;
            return this;
        }

        @Contract(value = "-> this", mutates = "this")
        public @NotNull Spec hidden() {
            this.hidden = true;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec x(int x) {
            if (this.hidden) throw new UnsupportedOperationException("hidden");
            this.x = x;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec y(int y) {
            if (this.hidden) throw new UnsupportedOperationException("hidden");
            this.y = y;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec icon(@Nullable Pair<ResourceLocation, ResourceLocation> icon) {
            if (this.hidden) throw new UnsupportedOperationException("hidden");
            this.icon = icon;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec filter(@NotNull ResourceFilter<Item> filter) {
            this.filter = filter;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec capacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        @Contract(pure = true)
        public @NotNull ItemResourceSlot create() {
            if (this.capacity <= 0) throw new IllegalArgumentException("capacity <= 0!");
            if (this.hidden) {
                if (this.x != 0 || this.y != 0 || this.icon != null)
                    throw new UnsupportedOperationException("Display prop while hidden");
            }

            return ItemResourceSlot.create(this.transferType, this.hidden ? null : ItemSlotDisplay.create(this.x, this.y, this.icon), this.filter, this.capacity);
        }
    }
}
