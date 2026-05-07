package dev.galacticraft.machinelib.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Set;
import java.util.UUID;

/**
 * Context passed to runtime multiblock components.
 */
public interface MultiblockComponentContext {

    /**
     * Gets the server level containing the formed multiblock.
     *
     * @return server level
     */
    ServerLevel level();

    /**
     * Gets the formed multiblock origin.
     *
     * @return origin
     */
    BlockPos origin();

    /**
     * Gets the formed multiblock instance id.
     *
     * @return instance id
     */
    UUID instanceId();

    /**
     * Gets the formed multiblock orientation.
     *
     * @return orientation
     */
    MultiblockOrientation orientation();

    /**
     * Gets the multiblock definition.
     *
     * @return definition
     */
    MultiblockDefinition definition();

    /**
     * Gets all world positions occupied by the formed machine.
     *
     * @return immutable part position set
     */
    Set<BlockPos> partPositions();

    /**
     * Gets another component attached to the same formed machine.
     *
     * @param type component type
     * @return component, or {@code null}
     * @param <T> component type
     */
    <T extends MultiblockComponent> T component(Class<T> type);

}