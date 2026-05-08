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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A resource slot that stores items.
 */
public interface ItemResourceSlot extends ResourceSlot<Item>, ContainerItemContext, FakeRecipeHolder {

    /**
     * Creates a mutable item slot specification.
     *
     * @param transferType transfer mode for the slot
     * @return new item slot specification
     */
    @Contract("_ -> new")
    static @NotNull Spec builder(final TransferType transferType) {
        return new Spec(transferType);
    }

    /**
     * Creates an item slot with default capacity.
     *
     * @param transferType transfer mode
     * @param display optional display data
     * @param filter external item filter
     * @return created slot
     */
    @Contract("_, _, _ -> new")
    static @NotNull ItemResourceSlot create(
            final @NotNull TransferType transferType,
            final @Nullable ItemSlotDisplay display,
            final @NotNull ResourceFilter<Item> filter
    ) {
        return create(
                transferType,
                display,
                filter,
                64
        );
    }

    /**
     * Creates an item slot without logical target metadata.
     *
     * @param transferType transfer mode
     * @param display optional display data
     * @param filter external item filter
     * @param capacity maximum item capacity
     * @return created slot
     */
    @Contract("_, _, _, _ -> new")
    static @NotNull ItemResourceSlot create(
            final @NotNull TransferType transferType,
            final @Nullable ItemSlotDisplay display,
            final @NotNull ResourceFilter<Item> filter,
            final int capacity
    ) {
        return create(
                transferType,
                display,
                filter,
                capacity,
                null,
                Set.of()
        );
    }

    /**
     * Creates an item slot with logical target metadata.
     *
     * <p>The id and groups are not persisted with item contents. They are static
     * storage-layout metadata created from the slot specification and used by
     * multiblock ports, automation, and UI configuration.</p>
     *
     * @param transferType transfer mode
     * @param display optional display data
     * @param filter external item filter
     * @param capacity maximum item capacity
     * @param id optional exact target id
     * @param groups logical target groups
     * @return created slot
     */
    @Contract("_, _, _, _, _, _ -> new")
    static @NotNull ItemResourceSlot create(
            final @NotNull TransferType transferType,
            final @Nullable ItemSlotDisplay display,
            final @NotNull ResourceFilter<Item> filter,
            final int capacity,
            final @Nullable ResourceLocation id,
            final @NotNull Set<ResourceLocation> groups
    ) {
        if (capacity < 0 || capacity > 64) {
            throw new IllegalArgumentException();
        }

        return new ItemResourceSlotImpl(
                transferType,
                display,
                filter,
                capacity,
                id,
                groups
        );
    }

    /**
     * Gets the stable logical target id for this slot.
     *
     * @return slot id, or {@code null} if unnamed
     */
    @Nullable
    ResourceLocation id();

    /**
     * Gets the logical target groups this slot belongs to.
     *
     * @return immutable group set
     */
    @NotNull
    Set<ResourceLocation> groups();

    /**
     * Consumes one item from the slot.
     *
     * @return consumed item, or {@code null} if empty
     */
    @Nullable
    Item consumeOne();

    /**
     * Consumes one item of the specified type.
     *
     * @param resource item type to consume
     * @return {@code true} if one item was consumed
     */
    default boolean consumeOne(final @NotNull Item resource) {
        return this.consumeOne(
                resource,
                null
        );
    }

    /**
     * Consumes one item of the specified type and components.
     *
     * @param resource item type to consume
     * @param components components to match, or {@code null} to ignore components
     * @return {@code true} if one item was consumed
     */
    boolean consumeOne(
            @NotNull Item resource,
            @Nullable DataComponentPatch components
    );

    /**
     * Consumes items from this slot, applying crafting remainder behavior.
     *
     * @param amount amount to consume
     * @return consumed amount
     */
    long consume(long amount);

    /**
     * Consumes items of the specified type.
     *
     * @param resource item type to consume
     * @param amount amount to consume
     * @return consumed amount
     */
    default long consume(
            final @NotNull Item resource,
            final long amount
    ) {
        return this.consume(
                resource,
                null,
                amount
        );
    }

    /**
     * Consumes items of the specified type and components.
     *
     * @param resource item type to consume
     * @param components components to match, or {@code null} to ignore components
     * @param amount amount to consume
     * @return consumed amount
     */
    long consume(
            @NotNull Item resource,
            @Nullable DataComponentPatch components,
            long amount
    );

    /**
     * Gets this slot's display data.
     *
     * @return display data, or {@code null} if hidden
     */
    @Nullable
    ItemSlotDisplay getDisplay();

    @Override
    long getAmount();

    /**
     * Mutable item slot specification.
     */
    final class Spec {

        private final TransferType transferType;

        private boolean hidden = false;
        private int x = 0;
        private int y = 0;
        private @Nullable Pair<ResourceLocation, ResourceLocation> icon = null;

        private ResourceFilter<Item> filter = ResourceFilters.any();
        private int capacity = 64;

        private @Nullable ResourceLocation id = null;
        private final Set<ResourceLocation> groups = new LinkedHashSet<>();

        @Contract(pure = true)
        private Spec(final TransferType transferType) {
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
            if (this.hidden) {
                throw new UnsupportedOperationException("hidden");
            }

            this.x = x;
            this.y = y;
            return this;
        }

        /**
         * Hides this slot from the UI.
         *
         * @return this specification
         */
        @Contract(value = "-> this", mutates = "this")
        public @NotNull Spec hidden() {
            this.hidden = true;
            return this;
        }

        /**
         * Sets the display x position.
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
         * Sets the display y position.
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
         * Sets the display icon.
         *
         * @param icon icon pair
         * @return this specification
         */
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec icon(final @Nullable Pair<ResourceLocation, ResourceLocation> icon) {
            if (this.hidden) {
                throw new UnsupportedOperationException("hidden");
            }

            this.icon = icon;
            return this;
        }

        /**
         * Sets the item filter.
         *
         * @param filter item filter
         * @return this specification
         */
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec filter(final @NotNull ResourceFilter<Item> filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Sets the slot capacity.
         *
         * @param capacity capacity
         * @return this specification
         */
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec capacity(final int capacity) {
            this.capacity = capacity;
            return this;
        }

        /**
         * Sets the exact logical target id for this slot.
         *
         * @param id slot id
         * @return this specification
         */
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Spec id(final ResourceLocation id) {
            this.id = id;
            return this;
        }

        /**
         * Adds this slot to one logical target group.
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
         * Adds this slot to several logical target groups.
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
         * Creates the item slot represented by this specification.
         *
         * @return created slot
         */
        @Contract(pure = true)
        public @NotNull ItemResourceSlot create() {
            if (this.capacity <= 0) {
                throw new IllegalArgumentException("capacity <= 0!");
            }

            if (this.hidden && (this.x != 0 || this.y != 0 || this.icon != null)) {
                throw new UnsupportedOperationException("Display prop while hidden");
            }

            return ItemResourceSlot.create(
                    this.transferType,
                    this.hidden ? null : ItemSlotDisplay.create(
                            this.x,
                            this.y,
                            this.icon
                    ),
                    this.filter,
                    this.capacity,
                    this.id,
                    this.groups
            );
        }
    }
}