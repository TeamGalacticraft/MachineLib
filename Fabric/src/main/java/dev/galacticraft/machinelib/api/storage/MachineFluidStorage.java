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

package dev.galacticraft.machinelib.api.storage;

import com.google.common.base.Preconditions;
import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.screen.MachineMenu;
import dev.galacticraft.machinelib.api.storage.io.ResourceFlow;
import dev.galacticraft.machinelib.api.storage.io.ResourceType;
import dev.galacticraft.machinelib.api.storage.io.StorageSelection;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.fluid.FluidStack;
import dev.galacticraft.machinelib.impl.storage.MachineFluidStorageImpl;
import dev.galacticraft.machinelib.impl.storage.empty.EmptyMachineFluidStorage;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Simple fluid storage for machines.
 * The flow of fluid is not restricted by this storage.
 * If you need to expose a storage that restricts fluid flow/types see {@link ResourceStorage#getExposedStorage(StorageSelection, ResourceFlow)}
 *
 * @see ResourceStorage
 * @see net.fabricmc.fabric.api.transfer.v1.storage.Storage
 */
public interface MachineFluidStorage extends ResourceStorage<Fluid, FluidVariant, FluidStack> {
    /**
     * Returns whether the given slot allows gases to be stored.
     * @param slot The slot to check.
     * @return Whether the fluid storage allows gases to be stored.
     * @see net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes#isLighterThanAir(FluidVariant)
     */
    boolean allowsGases(int slot);

    /**
     * Adds tanks to a screen handler for display.
     * @param handler The screen handler to add tanks to.
     * @param <M> The type of machine.
     */
    <M extends MachineBlockEntity> void addTanks(MachineMenu<M> handler);

    @Override
    @NotNull
    default FluidVariant createVariant(@NotNull Fluid fluid) {
        return FluidVariant.of(fluid);
    }

    @Override
    @ApiStatus.NonExtendable
    default @NotNull ResourceType getResource() {
        return ResourceType.FLUID;
    }

    /**
     * Returns the default empty fluid storage.
     * @return The default empty fluid storage.
     */
    static @NotNull MachineFluidStorage empty() {
        return EmptyMachineFluidStorage.INSTANCE;
    }

    /**
     * Creates a new fluid storage builder.
     * @return The fluid storage builder.
     */
    @Contract(value = " -> new", pure = true)
    static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * A builder for creating fluid storages.
     */
    class Builder {
        private int size = 0;
        private final List<@NotNull SlotGroup> groups = new ArrayList<>();
        private final List<@NotNull TankDisplay> displays = new ArrayList<>();
        private final List<@NotNull Predicate<FluidVariant>> filters = new ArrayList<>();
        private final LongList capacities = new LongArrayList();
        private final BooleanList gases = new BooleanArrayList();
        private final BooleanList insertion = new BooleanArrayList();

        public Builder() {}

        /**
         * Creates a new builder.
         * @return The new builder.
         */
        @Contract(value = " -> new", pure = true)
        public static @NotNull Builder create() {
            return new Builder();
        }

        /**
         * Adds a tank to the storage.
         * @param type The type of tank.
         * @param capacity The capacity of the tank.
         * @param display The display for the tank.
         * @return The builder.
         */
        public @NotNull Builder addTank(@NotNull SlotGroup type, long capacity, @NotNull TankDisplay display) {
            return this.addTank(type, capacity, Constant.Filter.always(), true, display, false);
        }

        /**
         * Adds a tank to the storage.
         * @param type The type of tank.
         * @param capacity The capacity of the tank.
         * @param display The display for the tank.
         * @return The builder.
         */
        public @NotNull Builder addTank(@NotNull SlotGroup type, long capacity, @NotNull TankDisplay display, boolean allowGases) {
            return this.addTank(type, capacity, Constant.Filter.always(), true, display, allowGases);
        }

        /**
         * Adds a tank to the storage.
         * @param type The type of tank.
         * @param capacity The capacity of the tank.
         * @param display The display for the tank.
         * @return The builder.
         */
        public @NotNull Builder addTank(@NotNull SlotGroup type, long capacity, boolean insertion, @NotNull TankDisplay display) {
            return this.addTank(type, capacity, Constant.Filter.always(), insertion, display, false);
        }

        /**
         * Adds a tank to the storage.
         * @param type The type of tank.
         * @param capacity The capacity of the tank.
         * @param display The display for the tank.
         * @return The builder.
         */
        public @NotNull Builder addTank(@NotNull SlotGroup type, long capacity, @NotNull Predicate<FluidVariant> filter, @NotNull TankDisplay display) {
            return this.addTank(type, capacity, filter, true, display, false);
        }

        /**
         * Adds a tank to the storage.
         * @param type The type of tank.
         * @param capacity The capacity of the tank.
         * @param display The display for the tank.
         * @return The builder.
         */
        public @NotNull Builder addTank(@NotNull SlotGroup type, long capacity, @NotNull Predicate<FluidVariant> filter, @NotNull TankDisplay display, boolean allowGases) {
            return this.addTank(type, capacity, filter, true, display, allowGases);
        }

        /**
         * Adds a tank to the storage.
         * @param type The type of tank.
         * @param capacity The capacity of the tank.
         * @param display The display for the tank.
         * @return The builder.
         */
        public @NotNull Builder addTank(@NotNull SlotGroup type, long capacity, @NotNull Predicate<FluidVariant> filter, boolean insertion, @NotNull TankDisplay display) {
            return this.addTank(type, capacity, filter, insertion, display, false);
        }

        /**
         * Adds a tank to the storage.
         * @param type The type of tank.
         * @param capacity The capacity of the tank.
         * @param display The display for the tank.
         * @param allowsGases Whether the tank allows gases.
         * @return The builder.
         */
        public @NotNull Builder addTank(@NotNull SlotGroup type, long capacity, @NotNull Predicate<FluidVariant> filter, boolean insertion, @NotNull TankDisplay display, boolean allowsGases) {
            Preconditions.checkNotNull(type);
            Preconditions.checkNotNull(display);
            StoragePreconditions.notNegative(capacity);
            this.size++;
            this.groups.add(type);
            this.displays.add(display);
            this.filters.add(filter);
            this.capacities.add(capacity);
            this.gases.add(allowsGases);
            this.insertion.add(insertion);
            return this;
        }

        /**
         * Builds the machine fluid storage.
         * @return The machine fluid storage.
         */
        @Contract(pure = true, value = " -> new")
        public @NotNull MachineFluidStorage build() {
            if (this.size == 0) return empty();
            return new MachineFluidStorageImpl(this.size, this.groups.toArray(new SlotGroup[0]), this.capacities.toLongArray(), this.filters.toArray(new Predicate[0]), this.insertion.toBooleanArray(), this.gases.toBooleanArray(), this.displays.toArray(new TankDisplay[0]));
        }
    }
}
