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

package dev.galacticraft.machinelib.impl.storage;

import dev.galacticraft.machinelib.api.compat.transfer.ExposedStorage;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortTarget;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.impl.compat.transfer.ExposedFluidSlotImpl;
import dev.galacticraft.machinelib.impl.compat.transfer.ExposedStorageImpl;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Default fluid storage implementation.
 */
public class MachineFluidStorageImpl extends ResourceStorageImpl<Fluid, FluidResourceSlot> implements MachineFluidStorage {

    public static final MachineFluidStorageImpl EMPTY = new MachineFluidStorageImpl(new FluidResourceSlot[0]);

    private final ExposedStorage<Fluid, FluidVariant>[] exposedStorages = new ExposedStorage[3];

    /**
     * Creates fluid storage from concrete slots.
     *
     * @param slots fluid slots
     */
    public MachineFluidStorageImpl(final @NotNull FluidResourceSlot @NotNull [] slots) {
        super(slots);

        for (int i = 0; i < 3; i++) {
            this.exposedStorages[i] = this.createExposedStorage(ResourceFlow.values()[i]);
        }
    }

    /**
     * Creates exposed storage for the given flow using all tanks.
     *
     * @param flow resource flow
     * @return exposed storage, or {@code null} if no tank supports the flow
     */
    protected @Nullable ExposedStorage<Fluid, FluidVariant> createExposedStorage(final @NotNull ResourceFlow flow) {
        final ExposedFluidSlotImpl[] slots = new ExposedFluidSlotImpl[this.size()];
        boolean support = false;

        for (int i = 0; i < slots.length; i++) {
            slots[i] = new ExposedFluidSlotImpl(
                    this.getSlots()[i],
                    flow
            );

            support |= slots[i].supportsInsertion() || slots[i].supportsExtraction();
        }

        return support ? new ExposedStorageImpl<>(
                this,
                slots
        ) : null;
    }

    @Override
    public @Nullable ExposedStorage<Fluid, FluidVariant> getExposedStorage(final @NotNull ResourceFlow flow) {
        return this.exposedStorages[flow.ordinal()];
    }

    @Override
    public @Nullable ExposedStorage<Fluid, FluidVariant> getExposedStorage(
            final @NotNull ResourceFlow flow,
            final @NotNull MultiblockPortTarget target
    ) {
        final List<ExposedFluidSlotImpl> exposedSlots = new ArrayList<>();

        for (final FluidResourceSlot slot : this.getSlots()) {
            if (!matchesTarget(
                    slot,
                    target
            )) {
                continue;
            }

            final ExposedFluidSlotImpl exposedSlot = new ExposedFluidSlotImpl(
                    slot,
                    flow
            );

            if (exposedSlot.supportsInsertion() || exposedSlot.supportsExtraction()) {
                exposedSlots.add(exposedSlot);
            }
        }

        if (exposedSlots.isEmpty()) {
            return null;
        }

        return new ExposedStorageImpl<>(
                this,
                exposedSlots.toArray(ExposedFluidSlotImpl[]::new)
        );
    }

    /**
     * Checks whether a fluid slot matches a port target.
     *
     * @param slot fluid slot
     * @param target port target
     * @return {@code true} if the target matches
     */
    private static boolean matchesTarget(
            final FluidResourceSlot slot,
            final MultiblockPortTarget target
    ) {
        if (target.group()) {
            return slot.groups().contains(target.id());
        }

        return target.id().equals(slot.id());
    }
}