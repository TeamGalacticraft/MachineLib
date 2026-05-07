package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.MultiblockDefinition;
import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

/**
 * Utility methods for checking whether multiblock structures are safe to inspect.
 *
 * <p>Persistent multiblocks may reference chunks that are not currently loaded.
 * MachineLib must not treat unloaded chunks as invalid structures, otherwise valid
 * saved multiblocks could be deleted during startup, dimension loading, player
 * movement, or ordinary chunk unloading.</p>
 */
public final class MultiblockChunkUtil {

    private MultiblockChunkUtil() {

    }

    /**
     * Checks whether the chunk containing a block position is currently loaded.
     *
     * @param level level to check
     * @param pos block position to inspect
     * @return {@code true} if the chunk containing {@code pos} is loaded
     */
    public static boolean isBlockLoaded(
            final ServerLevel level,
            final BlockPos pos
    ) {
        return level.hasChunkAt(pos);
    }

    /**
     * Checks whether every part position belongs to a currently loaded chunk.
     *
     * @param level level to check
     * @param parts rebuilt or runtime multiblock parts
     * @return {@code true} if all part chunks are currently loaded
     */
    public static boolean areAllPartsLoaded(
            final ServerLevel level,
            final List<MultiblockPart> parts
    ) {
        for (final MultiblockPart part : parts) {
            if (!isBlockLoaded(level, part.worldPos())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Rebuilds a structure's part list and checks whether all required chunks are
     * currently loaded.
     *
     * <p>This does not validate predicates. It only determines whether the world is
     * currently safe to inspect for this multiblock.</p>
     *
     * @param level level to check
     * @param origin saved or runtime origin
     * @param orientation saved or runtime orientation
     * @param definition multiblock definition
     * @return {@code true} if all required chunks are loaded
     */
    public static boolean isStructureLoaded(
            final ServerLevel level,
            final BlockPos origin,
            final MultiblockOrientation orientation,
            final MultiblockDefinition definition
    ) {
        final List<MultiblockPart> parts = MultiblockRuntimeBuilder.buildParts(
                level,
                origin,
                orientation,
                definition
        );

        return areAllPartsLoaded(level, parts);
    }

}