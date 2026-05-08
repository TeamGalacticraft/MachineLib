package dev.galacticraft.machinelib.impl.multiblock.port_lookup;

import dev.galacticraft.machinelib.api.compat.transfer.ExposedStorage;
import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import dev.galacticraft.machinelib.api.multiblock.components.MultiblockPortComponent;
import dev.galacticraft.machinelib.api.multiblock.components.MultiblockStorageComponent;
import dev.galacticraft.machinelib.api.multiblock.port.ConfiguredMultiblockPort;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortFace;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortMode;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortType;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.impl.multiblock.FormedMultiblockMachine;
import dev.galacticraft.machinelib.impl.multiblock.MultiblockManager;
import dev.galacticraft.machinelib.impl.multiblock.MultiblockPart;
import dev.galacticraft.machinelib.impl.multiblock.MultiblockPortDebug;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Optional;

/**
 * World-side lookup helper for fluid access through configured multiblock ports.
 */
public final class MultiblockFluidPortLookup {

    private MultiblockFluidPortLookup() {

    }

    /**
     * Looks up fluid storage exposed by a configured multiblock fluid port.
     *
     * @param level server level containing the formed multiblock
     * @param pos world position of the queried multiblock part
     * @param side world-space access side
     * @return exposed fluid storage, or {@code null} when no valid fluid port exists
     */
    public static @Nullable Storage<FluidVariant> find(
            final ServerLevel level,
            final BlockPos pos,
            final Direction side
    ) {
        final FormedMultiblockMachine machine = MultiblockManager.get(level).getByPart(pos);

        if (machine == null || !machine.isFormed()) {
            MultiblockPortDebug.LOGGER.debug("[FLUID] No formed machine at {} side={}", pos, side);
            return null;
        }

        final MultiblockPortFace face = findPortFace(machine, pos, side);

        if (face == null) {
            MultiblockPortDebug.LOGGER.debug("[FLUID] Could not resolve port face at {} side={} machine={}", pos, side, machine.instanceId());
            return null;
        }

        final MultiblockPortComponent ports = machine.component(MultiblockPortComponent.class);

        if (ports == null) {
            MultiblockPortDebug.LOGGER.debug("[FLUID] Machine {} has no port component", machine.instanceId());
            return null;
        }

        final Optional<ConfiguredMultiblockPort> configuredPort = ports.portAt(face);

        if (configuredPort.isEmpty()) {
            MultiblockPortDebug.LOGGER.debug("[FLUID] No configured port at face={} machine={}", face, machine.instanceId());
            return null;
        }

        final ConfiguredMultiblockPort port = configuredPort.get();

        if (port.type() != MultiblockPortType.ITEM) {
            MultiblockPortDebug.LOGGER.debug("[FLUID] Port at face={} is {}, not ITEM", face, port.type());
            return null;
        }

        final MultiblockStorageComponent storage = machine.component(MultiblockStorageComponent.class);

        if (storage == null) {
            MultiblockPortDebug.LOGGER.debug("[FLUID] Machine {} has no storage component", machine.instanceId());
            return null;
        }

        final ExposedStorage<Fluid, FluidVariant> exposedStorage =
                storage.fluidStorage().getExposedStorage(
                        flowFor(port.mode()),
                        port.target()
                );

        if (exposedStorage == null) {
            MultiblockPortDebug.LOGGER.debug("[FLUID] No exposed storage for flow={} target={} machine={}", flowFor(port.mode()), port.target(), machine.instanceId());
            return null;
        }

        MultiblockPortDebug.LOGGER.debug("[FLUID] Exposed storage at {} side={} face={} port={}", pos, side, face, port);

        return new DirtyTrackingFluidStorage(exposedStorage, storage);
    }

    private static @Nullable MultiblockPortFace findPortFace(
            final FormedMultiblockMachine machine,
            final BlockPos pos,
            final Direction worldSide
    ) {
        for (final MultiblockPart part : machine.parts()) {
            if (!part.worldPos().equals(pos)) {
                continue;
            }

            final Direction localSide = inverseTransformDirection(machine.orientation(), worldSide);

            if (localSide == null) {
                return null;
            }

            return new MultiblockPortFace(part.originalRelativePos(), localSide);
        }

        return null;
    }

    private static @Nullable Direction inverseTransformDirection(
            final MultiblockOrientation orientation,
            final Direction worldSide
    ) {
        for (final Direction localSide : Direction.values()) {
            if (orientation.transformDirection(localSide) == worldSide) {
                return localSide;
            }
        }

        return null;
    }

    private static ResourceFlow flowFor(final MultiblockPortMode mode) {
        return switch (mode) {
            case INPUT -> ResourceFlow.INPUT;
            case OUTPUT -> ResourceFlow.OUTPUT;
            case BOTH -> ResourceFlow.BOTH;
        };
    }

    private record DirtyTrackingFluidStorage(
            ExposedStorage<Fluid, FluidVariant> delegate,
            MultiblockStorageComponent storage
    ) implements Storage<FluidVariant> {

        @Override
        public boolean supportsInsertion() {
            return this.delegate.supportsInsertion();
        }

        @Override
        public boolean supportsExtraction() {
            return this.delegate.supportsExtraction();
        }

        /**
         * Inserts fluid into the wrapped storage and marks the multiblock storage
         * component changed if any fluid was accepted.
         *
         * @param resource inserted fluid variant
         * @param maxAmount maximum amount to insert
         * @param transaction transaction context
         * @return inserted amount
         */
        @Override
        public long insert(
                final FluidVariant resource,
                final long maxAmount,
                final TransactionContext transaction
        ) {
            final long inserted = this.delegate.insert(resource, maxAmount, transaction);

            if (inserted > 0) {
                this.storage.setChanged();
            }

            return inserted;
        }

        /**
         * Extracts fluid from the wrapped storage and marks the multiblock storage
         * component changed if any fluid was removed.
         *
         * @param resource extracted fluid variant
         * @param maxAmount maximum amount to extract
         * @param transaction transaction context
         * @return extracted amount
         */
        @Override
        public long extract(
                final FluidVariant resource,
                final long maxAmount,
                final TransactionContext transaction
        ) {
            final long extracted = this.delegate.extract(resource, maxAmount, transaction);

            if (extracted > 0) {
                this.storage.setChanged();
            }

            return extracted;
        }

        @Override
        public @NotNull Iterator<StorageView<FluidVariant>> iterator() {
            return this.delegate.iterator();
        }

        @Override
        public long getVersion() {
            return this.delegate.getVersion();
        }
    }
}