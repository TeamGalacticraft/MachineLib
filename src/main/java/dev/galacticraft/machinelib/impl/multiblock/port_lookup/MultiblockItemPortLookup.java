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

import dev.galacticraft.machinelib.api.compat.transfer.ExposedStorage;
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
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Optional;

/**
 * World-side lookup helper for item access through configured multiblock ports.
 *
 * <p>This class bridges external Fabric transfer lookups into formed
 * multiblocks. It does not own storage itself. Instead, it resolves the clicked
 * world position and access side into a configured {@link MultiblockPortFace},
 * validates that the configured port is an item port, and exposes the formed
 * machine's {@link MultiblockStorageComponent} item storage through the
 * matching resource flow.</p>
 *
 * <p>Port target ids/groups are intentionally not resolved here yet. Until the
 * storage identifier system exists, a valid item port exposes the full
 * multiblock item storage with the port's configured flow restriction.</p>
 */
public final class MultiblockItemPortLookup {

    private MultiblockItemPortLookup() {

    }

    /**
     * Looks up item storage exposed by a configured multiblock item port.
     *
     * <p>The supplied {@code pos} must be the world position of a formed
     * multiblock part. The supplied {@code side} is the world-space side being
     * accessed by an external block, pipe, hopper, or API lookup. This method
     * converts that world-space side into the multiblock's unrotated
     * pattern-local port face before checking the configured port component.</p>
     *
     * @param level server level containing the formed multiblock
     * @param pos world position of the queried multiblock part
     * @param side world-space access side
     * @return exposed item storage, or {@code null} when no valid item port exists
     */
    public static @Nullable Storage<ItemVariant> find(
            final ServerLevel level,
            final BlockPos pos,
            final Direction side
    ) {
        final FormedMultiblockMachine machine =
                MultiblockManager.get(level).getByPart(pos);

        if (machine == null || !machine.isFormed()) {
            MultiblockPortDebug.LOGGER.debug("[ITEM] No formed machine at {} side={}", pos, side);
            return null;
        }

        final MultiblockPortFace face = machine.getPortFaceAtWorldSide(
                pos,
                side
        );

        if (face == null) {
            MultiblockPortDebug.LOGGER.debug("[ITEM] Could not resolve port face at {} side={} machine={}", pos, side, machine.instanceId());
            return null;
        }

        final MultiblockPortComponent ports =
                machine.component(MultiblockPortComponent.class);

        if (ports == null) {
            MultiblockPortDebug.LOGGER.debug("[ITEM] Machine {} has no port component", machine.instanceId());
            return null;
        }

        final Optional<ConfiguredMultiblockPort> configuredPort =
                ports.portAt(face);

        if (configuredPort.isEmpty()) {
            MultiblockPortDebug.LOGGER.debug("[ITEM] No configured port at face={} machine={}", face, machine.instanceId());
            return null;
        }

        final ConfiguredMultiblockPort port = configuredPort.get();

        if (port.type() != MultiblockPortType.ITEM) {
            MultiblockPortDebug.LOGGER.debug("[ITEM] Port at face={} is {}, not ITEM", face, port.type());
            return null;
        }

        final MultiblockStorageComponent storage =
                machine.component(MultiblockStorageComponent.class);

        if (storage == null) {
            MultiblockPortDebug.LOGGER.debug("[ITEM] Machine {} has no storage component", machine.instanceId());
            return null;
        }

        final ResourceFlow flow = flowFor(port.mode());
        final ExposedStorage<Item, ItemVariant> exposedStorage =
                storage.itemStorage().getExposedStorage(
                        flowFor(port.mode()),
                        port.target()
                );

        if (exposedStorage == null) {
            MultiblockPortDebug.LOGGER.debug("[ITEM] No exposed storage for flow={} target={} machine={}", flowFor(port.mode()), port.target(), machine.instanceId());
            return null;
        }

        MultiblockPortDebug.LOGGER.debug("[ITEM] Exposed storage at {} side={} face={} port={}", pos, side, face, port);

        return new DirtyTrackingItemStorage(
                exposedStorage,
                storage
        );
    }

    /**
     * Converts a configured port mode into the matching MachineLib resource flow.
     *
     * @param mode configured port mode
     * @return equivalent resource flow
     */
    private static ResourceFlow flowFor(final MultiblockPortMode mode) {
        return switch (mode) {
            case INPUT -> ResourceFlow.INPUT;
            case OUTPUT -> ResourceFlow.OUTPUT;
            case BOTH -> ResourceFlow.BOTH;
        };
    }

    /**
     * Storage wrapper that marks the multiblock storage component dirty when an
     * external transfer changes item contents.
     *
     * <p>The underlying MachineLib storage has no block entity parent when owned
     * by a formed multiblock, so normal block-entity dirty propagation cannot be
     * used. This wrapper marks the multiblock storage component changed after any
     * successful insert or extract operation.</p>
     */
    private record DirtyTrackingItemStorage(
            ExposedStorage<Item, ItemVariant> delegate,
            MultiblockStorageComponent storage
    ) implements Storage<ItemVariant> {

        @Override
        public boolean supportsInsertion() {
            return this.delegate.supportsInsertion();
        }

        @Override
        public boolean supportsExtraction() {
            return this.delegate.supportsExtraction();
        }

        /**
         * Inserts items into the wrapped storage and marks the multiblock storage
         * component changed if any items were accepted.
         *
         * @param resource inserted item variant
         * @param maxAmount maximum amount to insert
         * @param transaction transaction context
         * @return inserted amount
         */
        @Override
        public long insert(
                final ItemVariant resource,
                final long maxAmount,
                final TransactionContext transaction
        ) {
            final long inserted = this.delegate.insert(
                    resource,
                    maxAmount,
                    transaction
            );

            if (inserted > 0) {
                this.storage.setChanged();
            }

            return inserted;
        }

        /**
         * Extracts items from the wrapped storage and marks the multiblock storage
         * component changed if any items were removed.
         *
         * @param resource extracted item variant
         * @param maxAmount maximum amount to extract
         * @param transaction transaction context
         * @return extracted amount
         */
        @Override
        public long extract(
                final ItemVariant resource,
                final long maxAmount,
                final TransactionContext transaction
        ) {
            final long extracted = this.delegate.extract(
                    resource,
                    maxAmount,
                    transaction
            );

            if (extracted > 0) {
                this.storage.setChanged();
            }

            return extracted;
        }

        @Override
        public @NotNull Iterator<StorageView<ItemVariant>> iterator() {
            return this.delegate.iterator();
        }

        @Override
        public long getVersion() {
            return this.delegate.getVersion();
        }
    }
}