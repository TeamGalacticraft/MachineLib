package dev.galacticraft.machinelib.api.storage;

import com.google.common.base.Preconditions;
import dev.galacticraft.machinelib.api.storage.slot.MachineFluidTank;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.display.FluidTankDisplay;
import dev.galacticraft.machinelib.impl.storage.EmptyMachineFluidStorage;
import dev.galacticraft.machinelib.impl.storage.MachineFluidStorageImpl;
import dev.galacticraft.machinelib.impl.storage.ResourceFilter;
import dev.galacticraft.machinelib.impl.storage.slot.InternalChangeTracking;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface MachineFluidStorage extends SlottedFluidStorage, InternalChangeTracking {
    @Override
    MachineFluidTank getTank(int slot);

    @Contract(" -> new")
    static @NotNull Builder builder() {
        return new Builder();
    }

    @Contract(pure = true)
    static @NotNull MachineFluidStorage empty() {
        return EmptyMachineFluidStorage.INSTANCE;
    }

    class Builder {
        private int size = 0;
        private final List<SlotGroup> types = new ArrayList<>();
        private final List<FluidTankDisplay> displays = new ArrayList<>();
        private final List<ResourceFilter<Fluid>> filters = new ArrayList<>();
        private final LongList capacities = new LongArrayList();
        private final BooleanList insertion = new BooleanArrayList();

        private Builder() {}

        /**
         * Adds a slot to the builder.
         * @param type The type of slot.
         * @param capacity The maximum count of fluids in the slot. Clamped to {@code 64} and cannot be negative.
         * @param display The display for the slot.
         * @return The builder.
         */
        public @NotNull Builder addSlot(@NotNull SlotGroup type, @NotNull ResourceFilter<Fluid> filter, boolean insertion, long capacity, @NotNull FluidTankDisplay display) {
            Preconditions.checkNotNull(type);
            Preconditions.checkNotNull(display);
            if (capacity < 0) {
                throw new IllegalArgumentException("Capacity cannot be negative!");
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
         * Builds the fluid storage.
         * @return The fluid storage.
         */
        @Contract(pure = true, value = " -> new")
        public @NotNull MachineFluidStorage build() {
            if (this.size == 0) return empty();
            return new MachineFluidStorageImpl(this.size, this.types.toArray(new SlotGroup[0]), this.capacities.toLongArray(), this.filters.toArray(new ResourceFilter[0]), this.displays.toArray(new FluidTankDisplay[0]), this.insertion.toBooleanArray());
        }
    }
}
