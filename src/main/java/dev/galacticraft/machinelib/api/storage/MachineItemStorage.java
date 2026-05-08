package dev.galacticraft.machinelib.api.storage;

import dev.galacticraft.machinelib.api.compat.transfer.ExposedStorage;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortTarget;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import dev.galacticraft.machinelib.impl.storage.MachineItemStorageImpl;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents item storage in a machine or formed multiblock.
 */
public interface MachineItemStorage extends ResourceStorage<Item, ItemResourceSlot>, Container {

    /**
     * Creates item storage from concrete slots.
     *
     * @param slots item slots
     * @return created storage
     */
    static @NotNull MachineItemStorage create(final ItemResourceSlot @NotNull ... slots) {
        if (slots.length == 0) {
            return empty();
        }

        return new MachineItemStorageImpl(slots);
    }

    /**
     * Creates an item storage specification from slot specifications.
     *
     * @param slots slot specifications
     * @return storage specification
     */
    static @NotNull MachineItemStorage.Spec spec(final ItemResourceSlot.Spec @NotNull ... slots) {
        if (slots.length == 0) {
            throw new IllegalArgumentException("Cannot create a storage with no slots");
        }

        return new Spec(List.of(slots));
    }

    /**
     * Creates an empty mutable item storage specification.
     *
     * @return storage specification
     */
    @Contract(" -> new")
    static @NotNull MachineItemStorage.Spec builder() {
        return new Spec();
    }

    /**
     * Gets the shared empty item storage.
     *
     * @return empty item storage
     */
    @Contract(pure = true)
    static @NotNull MachineItemStorage empty() {
        return MachineItemStorageImpl.EMPTY;
    }

    @Override
    @Nullable
    ExposedStorage<Item, ItemVariant> getExposedStorage(@NotNull ResourceFlow flow);

    /**
     * Gets exposed item storage for an exact logical target or group.
     *
     * @param flow allowed transfer flow
     * @param target port target
     * @return exposed storage, or {@code null} if no matching slots can expose that flow
     */
    @Nullable
    ExposedStorage<Item, ItemVariant> getExposedStorage(
            @NotNull ResourceFlow flow,
            @NotNull MultiblockPortTarget target
    );

    boolean consumeOne(@NotNull Item resource);

    boolean consumeOne(
            @NotNull Item resource,
            @Nullable DataComponentPatch components
    );

    long consume(
            @NotNull Item resource,
            long amount
    );

    long consume(
            @NotNull Item resource,
            @Nullable DataComponentPatch components,
            long amount
    );

    /**
     * Mutable item storage specification.
     */
    final class Spec {

        private final List<ItemResourceSlot.Spec> slots;

        private Spec() {
            this(new ArrayList<>());
        }

        private Spec(final List<ItemResourceSlot.Spec> slots) {
            this.slots = slots;
        }

        /**
         * Adds a slot specification.
         *
         * @param slot slot specification
         * @return this specification
         */
        @Contract("_ -> this")
        public @NotNull MachineItemStorage.Spec add(final ItemResourceSlot.Spec slot) {
            this.slots.add(slot);
            return this;
        }

        /**
         * Adds a 3x3 grid of item slots.
         *
         * @param type transfer type
         * @param xOffset x offset
         * @param yOffset y offset
         * @return this specification
         */
        @Contract("_, _, _ -> this")
        public @NotNull MachineItemStorage.Spec add3x3Grid(
                final TransferType type,
                final int xOffset,
                final int yOffset
        ) {
            return this.addGrid(
                    type,
                    xOffset,
                    yOffset,
                    3,
                    3
            );
        }

        /**
         * Adds a rectangular grid of item slots.
         *
         * @param type transfer type
         * @param xOffset x offset
         * @param yOffset y offset
         * @param width grid width
         * @param height grid height
         * @return this specification
         */
        @Contract("_, _, _, _, _ -> this")
        public @NotNull MachineItemStorage.Spec addGrid(
                final TransferType type,
                final int xOffset,
                final int yOffset,
                final int width,
                final int height
        ) {
            assert width > 0 && height > 0;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    this.add(ItemResourceSlot.builder(type).pos(
                            x * 18 + xOffset,
                            y * 18 + yOffset
                    ));
                }
            }

            return this;
        }

        /**
         * Creates item storage from this specification.
         *
         * @return created item storage
         */
        public MachineItemStorage create() {
            if (this.slots.isEmpty()) {
                return empty();
            }

            final ItemResourceSlot[] slots = new ItemResourceSlot[this.slots.size()];

            for (int i = 0; i < this.slots.size(); i++) {
                slots[i] = this.slots.get(i).create();
            }

            return new MachineItemStorageImpl(slots);
        }
    }
}