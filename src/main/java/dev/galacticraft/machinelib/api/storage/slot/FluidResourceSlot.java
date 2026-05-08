/*
 * Copyright (c) 2021-2025 Team Galacticraft
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
import dev.galacticraft.machinelib.api.filter.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import dev.galacticraft.machinelib.impl.storage.slot.FluidResourceSlotImpl;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A resource slot that stores fluids.
 */
public interface FluidResourceSlot extends ResourceSlot<Fluid> {

    /**
     * Creates a mutable fluid slot specification.
     *
     * @param transferType transfer mode
     * @return new fluid slot specification
     */
    @Contract("_ -> new")
    static @NotNull Spec builder(final @NotNull TransferType transferType) {
        return new Spec(transferType);
    }

    /**
     * Creates a fluid slot without logical target metadata.
     *
     * @param transferType transfer mode
     * @param display optional tank display
     * @param capacity fluid capacity
     * @param filter fluid filter
     * @return created fluid slot
     */
    @Contract("_, _, _, _ -> new")
    static @NotNull FluidResourceSlot create(
            final @NotNull TransferType transferType,
            final @Nullable TankDisplay display,
            final long capacity,
            final @NotNull ResourceFilter<Fluid> filter
    ) {
        return create(
                transferType,
                display,
                capacity,
                filter,
                null,
                Set.of()
        );
    }

    /**
     * Creates a fluid slot with logical target metadata.
     *
     * @param transferType transfer mode
     * @param display optional tank display
     * @param capacity fluid capacity
     * @param filter fluid filter
     * @param id optional exact tank id
     * @param groups logical tank groups
     * @return created fluid slot
     */
    @Contract("_, _, _, _, _, _ -> new")
    static @NotNull FluidResourceSlot create(
            final @NotNull TransferType transferType,
            final @Nullable TankDisplay display,
            final long capacity,
            final @NotNull ResourceFilter<Fluid> filter,
            final @Nullable ResourceLocation id,
            final @NotNull Set<ResourceLocation> groups
    ) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity < 0");
        }

        return new FluidResourceSlotImpl(
                transferType,
                display,
                capacity,
                filter,
                id,
                groups
        );
    }

    /**
     * Gets the stable logical target id for this tank.
     *
     * @return tank id, or {@code null} if unnamed
     */
    @Nullable
    ResourceLocation id();

    /**
     * Gets the logical target groups this tank belongs to.
     *
     * @return immutable group set
     */
    @NotNull
    Set<ResourceLocation> groups();

    /**
     * Checks whether the tank is hidden from the UI.
     *
     * @return {@code true} if hidden
     */
    boolean isHidden();

    /**
     * Gets the tank display metadata.
     *
     * @return display metadata, or {@code null} if hidden
     */
    @Nullable
    TankDisplay getDisplay();

    /**
     * Mutable fluid slot specification.
     */
    final class Spec {

        private final TransferType transferType;

        private boolean hidden = false;
        private boolean marked = true;

        private int x = 0;
        private int y = 0;
        private int width = 16;
        private int height = 48;

        private ResourceFilter<Fluid> filter = ResourceFilters.any();
        private long capacity = FluidConstants.BUCKET;

        private @Nullable ResourceLocation id = null;
        private final Set<ResourceLocation> groups = new LinkedHashSet<>();

        @Contract(pure = true)
        private Spec(final @NotNull TransferType transferType) {
            this.transferType = transferType;
        }

        /**
         * Sets the display position.
         *
         * @param x x position
         * @param y y position
         * @return this specification
         */
        @Contract("_, _ -> this")
        public @NotNull Spec pos(
                final int x,
                final int y
        ) {
            this.x(x);
            this.y(y);
            return this;
        }

        /**
         * Hides this tank from the UI.
         *
         * @return this specification
         */
        @Contract(value = "-> this", mutates = "this")
        public @NotNull Spec hidden() {
            this.hidden = true;
            return this;
        }

        /**
         * Removes the visual marker from the tank display.
         *
         * @return this specification
         */
        @Contract(value = "-> this", mutates = "this")
        public @NotNull Spec unmarked() {
            this.marked = false;
            return this;
        }

        /**
         * Sets display x position.
         *
         * @param x x position
         * @return this specification
         */
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec x(final int x) {
            if (this.hidden) {
                throw new UnsupportedOperationException("hidden");
            }

            this.x = x;
            return this;
        }

        /**
         * Sets display y position.
         *
         * @param y y position
         * @return this specification
         */
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec y(final int y) {
            if (this.hidden) {
                throw new UnsupportedOperationException("hidden");
            }

            this.y = y;
            return this;
        }

        /**
         * Sets display width.
         *
         * @param width width
         * @return this specification
         */
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec width(final int width) {
            if (this.hidden) {
                throw new UnsupportedOperationException("hidden");
            }

            this.width = width;
            return this;
        }

        /**
         * Sets display height.
         *
         * @param height height
         * @return this specification
         */
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec height(final int height) {
            if (this.hidden) {
                throw new UnsupportedOperationException("hidden");
            }

            this.height = height;
            return this;
        }

        /**
         * Sets the fluid filter.
         *
         * @param filter fluid filter
         * @return this specification
         */
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec filter(final @NotNull ResourceFilter<Fluid> filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Sets tank capacity.
         *
         * @param capacity capacity
         * @return this specification
         */
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec capacity(final long capacity) {
            this.capacity = capacity;
            return this;
        }

        /**
         * Sets the exact logical target id for this tank.
         *
         * @param id tank id
         * @return this specification
         */
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec id(final ResourceLocation id) {
            this.id = id;
            return this;
        }

        /**
         * Adds this tank to one logical target group.
         *
         * @param group group id
         * @return this specification
         */
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec group(final ResourceLocation group) {
            this.groups.add(group);
            return this;
        }

        /**
         * Adds this tank to several logical target groups.
         *
         * @param groups group ids
         * @return this specification
         */
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec groups(final ResourceLocation... groups) {
            this.groups.addAll(List.of(groups));
            return this;
        }

        /**
         * Creates the fluid slot represented by this specification.
         *
         * @return created fluid slot
         */
        @Contract(pure = true)
        public @NotNull FluidResourceSlot create() {
            if (this.capacity <= 0) {
                throw new IllegalArgumentException("capacity <= 0!");
            }

            if (this.height < 0) {
                throw new IllegalArgumentException("sizeY is negative");
            }

            if (this.hidden && (this.x != 0 || this.y != 0 || this.width != 16 || this.height != 48)) {
                throw new UnsupportedOperationException("Display properties changed while hidden!");
            }

            return FluidResourceSlot.create(
                    this.transferType,
                    this.hidden ? null : TankDisplay.create(
                            this.x,
                            this.y,
                            this.width,
                            this.height,
                            this.marked
                    ),
                    this.capacity,
                    this.filter,
                    this.id,
                    this.groups
            );
        }
    }
}