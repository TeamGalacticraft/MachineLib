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

package dev.galacticraft.machinelib.impl.multiblock.port_lookup;

import dev.galacticraft.machinelib.api.multiblock.components.MultiblockPortComponent;
import dev.galacticraft.machinelib.api.multiblock.components.MultiblockStorageComponent;
import dev.galacticraft.machinelib.api.multiblock.port.ConfiguredMultiblockPort;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortFace;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortMode;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortType;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.impl.multiblock.FormedMultiblockMachine;
import dev.galacticraft.machinelib.impl.multiblock.MultiblockManager;
import dev.galacticraft.machinelib.impl.multiblock.MultiblockPortDebug;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

/**
 * World-side lookup helper for energy access through configured multiblock ports.
 */
public final class MultiblockEnergyPortLookup {

    private MultiblockEnergyPortLookup() {

    }

    /**
     * Looks up energy storage exposed by a configured multiblock energy port.
     *
     * @param level server level containing the formed multiblock
     * @param pos world position of the queried multiblock part
     * @param side world-space access side
     * @return exposed energy storage, or {@code null} when no valid energy port exists
     */
    public static @Nullable EnergyStorage find(
            final ServerLevel level,
            final BlockPos pos,
            final Direction side
    ) {
        final FormedMultiblockMachine machine = MultiblockManager.get(level).getByPart(pos);

        if (machine == null || !machine.isFormed()) {
            MultiblockPortDebug.LOGGER.debug("[ENERGY] No formed machine at {} side={}", pos, side);
            return null;
        }

        final MultiblockPortFace face = machine.getPortFaceAtWorldSide(
                pos,
                side
        );

        if (face == null) {
            MultiblockPortDebug.LOGGER.debug("[ENERGY] Could not resolve port face at {} side={} machine={}", pos, side, machine.instanceId());
            return null;
        }

        final MultiblockPortComponent ports = machine.component(MultiblockPortComponent.class);

        if (ports == null) {
            MultiblockPortDebug.LOGGER.debug("[ENERGY] Machine {} has no port component", machine.instanceId());
            return null;
        }

        final ConfiguredMultiblockPort port = ports.portAt(face).orElse(null);

        if (port == null || port.type() != MultiblockPortType.ENERGY) {
            MultiblockPortDebug.LOGGER.debug("[ENERGY] Port at face={} is {}, not ITEM", face, port.type());
            return null;
        }

        final MultiblockStorageComponent storage = machine.component(MultiblockStorageComponent.class);

        if (storage == null) {
            MultiblockPortDebug.LOGGER.debug("[ENERGY] Machine {} has no storage component", machine.instanceId());
            return null;
        }

        final EnergyStorage exposedStorage =
                storage.energyStorage().getExposedStorage(
                        flowFor(port.mode()),
                        port.target()
                );

        if (exposedStorage == null) {
            MultiblockPortDebug.LOGGER.debug("[ENERGY] No exposed storage for flow={} target={} machine={}", flowFor(port.mode()), port.target(), machine.instanceId());
            return null;
        }

        MultiblockPortDebug.LOGGER.debug("[ENERGY] Exposed storage at {} side={} face={} port={}", pos, side, face, port);

        return new DirtyTrackingEnergyStorage(exposedStorage, storage);
    }

    private static ResourceFlow flowFor(final MultiblockPortMode mode) {
        return switch (mode) {
            case INPUT -> ResourceFlow.INPUT;
            case OUTPUT -> ResourceFlow.OUTPUT;
            case BOTH -> ResourceFlow.BOTH;
        };
    }

    private record DirtyTrackingEnergyStorage(
            EnergyStorage delegate,
            MultiblockStorageComponent storage
    ) implements EnergyStorage {

        @Override
        public boolean supportsInsertion() {
            return this.delegate.supportsInsertion();
        }

        @Override
        public long insert(
                final long maxAmount,
                final TransactionContext transaction
        ) {
            final long inserted = this.delegate.insert(maxAmount, transaction);

            if (inserted > 0) {
                this.storage.setChanged();
            }

            return inserted;
        }

        @Override
        public boolean supportsExtraction() {
            return this.delegate.supportsExtraction();
        }

        @Override
        public long extract(
                final long maxAmount,
                final TransactionContext transaction
        ) {
            final long extracted = this.delegate.extract(maxAmount, transaction);

            if (extracted > 0) {
                this.storage.setChanged();
            }

            return extracted;
        }

        @Override
        public long getAmount() {
            return this.delegate.getAmount();
        }

        @Override
        public long getCapacity() {
            return this.delegate.getCapacity();
        }
    }
}