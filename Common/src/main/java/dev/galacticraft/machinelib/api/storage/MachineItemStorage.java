package dev.galacticraft.machinelib.api.storage;

import com.google.common.base.Preconditions;
import dev.galacticraft.machinelib.api.storage.slot.MachineItemSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.impl.storage.EmptyMachineItemStorage;
import dev.galacticraft.machinelib.impl.storage.MachineItemStorageImpl;
import dev.galacticraft.machinelib.impl.storage.ResourceFilter;
import dev.galacticraft.machinelib.impl.storage.slot.InternalChangeTracking;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface MachineItemStorage extends SlottedItemStorage, InternalChangeTracking {
    @Override
    MachineItemSlot getSlot(int slot);

    @Contract(" -> new")
    static @NotNull Builder builder() {
        return new Builder();
    }

    @Contract(pure = true)
    static @NotNull MachineItemStorage empty() {
        return EmptyMachineItemStorage.INSTANCE;
    }

    class Builder {
        private int size = 0;
        private final List<SlotGroup> types = new ArrayList<>();
        private final List<ItemSlotDisplay> displays = new ArrayList<>();
        private final List<ResourceFilter<Item>> filters = new ArrayList<>();
        private final IntList capacities = new IntArrayList();
        private final BooleanList insertion = new BooleanArrayList();

        private Builder() {}

        /**
         * Adds a slot to the builder.
         * @param type The type of slot.
         * @param display The display for the slot.
         * @return The builder.
         */
        public @NotNull Builder addSlot(@NotNull SlotGroup type, @NotNull ResourceFilter<Item> filter, boolean insertion, @NotNull ItemSlotDisplay display) {
            return this.addSlot(type, filter, insertion, 64, display);
        }

        /**
         * Adds a slot to the builder.
         * @param type The type of slot.
         * @param capacity The maximum count of items in the slot. Clamped to {@code 64} and cannot be negative.
         * @param display The display for the slot.
         * @return The builder.
         */
        public @NotNull Builder addSlot(@NotNull SlotGroup type, @NotNull ResourceFilter<Item> filter, boolean insertion, int capacity, @NotNull ItemSlotDisplay display) {
            Preconditions.checkNotNull(type);
            Preconditions.checkNotNull(display);
            if (capacity < 0 || capacity > 64) {
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
            return new MachineItemStorageImpl(this.size, this.types.toArray(new SlotGroup[0]), this.capacities.toIntArray(), this.filters.toArray(new ResourceFilter[0]), this.displays.toArray(new ItemSlotDisplay[0]), this.insertion.toBooleanArray());
        }
    }
}
