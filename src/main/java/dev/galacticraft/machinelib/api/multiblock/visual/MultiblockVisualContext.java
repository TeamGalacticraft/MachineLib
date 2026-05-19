package dev.galacticraft.machinelib.api.multiblock.visual;

import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.UUID;

/**
 * Immutable client-side context describing one synced formed multiblock visual.
 *
 * @param instanceId unique formed multiblock instance id
 * @param definitionId registered multiblock definition id
 * @param origin world-space origin of the formed multiblock
 * @param orientation orientation used when the multiblock formed
 * @param partPositions immutable world-space positions occupied by the formed multiblock
 * @param patternWidth unrotated multiblock pattern width
 * @param patternHeight unrotated multiblock pattern height
 * @param patternDepth unrotated multiblock pattern depth
 */
public record MultiblockVisualContext(
        UUID instanceId,
        ResourceLocation definitionId,
        BlockPos origin,
        MultiblockOrientation orientation,
        List<BlockPos> partPositions,
        int patternWidth,
        int patternHeight,
        int patternDepth
) {

    /**
     * Creates an immutable visual context.
     */
    public MultiblockVisualContext {
        origin = origin.immutable();
        partPositions = List.copyOf(partPositions.stream()
                .map(BlockPos::immutable)
                .toList());

        if (patternWidth <= 0 || patternHeight <= 0 || patternDepth <= 0) {
            throw new IllegalArgumentException("Multiblock visual pattern dimensions must be positive.");
        }
    }

}