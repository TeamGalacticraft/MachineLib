package dev.galacticraft.machinelib.api.multiblock.visual;

import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.UUID;

/**
 * Immutable client-side context describing one synced formed multiblock visual.
 *
 * <p>This context is intentionally lightweight. It mirrors the data already sent
 * by the formed multiblock sync packet: the instance id, definition id, origin,
 * orientation, and occupied part positions. The server remains authoritative; the
 * client only uses this data for rendering and local interaction prediction.</p>
 *
 * <p>The context does not expose server-only runtime objects such as
 * {@code FormedMultiblockMachine}. Client visuals should treat the data in this
 * object as a render snapshot, not as authoritative gameplay state.</p>
 *
 * @param instanceId unique formed multiblock instance id
 * @param definitionId registered multiblock definition id
 * @param origin world-space origin of the formed multiblock
 * @param orientation orientation used when the multiblock formed
 * @param partPositions immutable world-space positions occupied by the formed multiblock
 */
public record MultiblockVisualContext(
        UUID instanceId,
        ResourceLocation definitionId,
        BlockPos origin,
        MultiblockOrientation orientation,
        List<BlockPos> partPositions
) {

    /**
     * Creates an immutable visual context.
     *
     * @param instanceId unique formed multiblock instance id
     * @param definitionId registered multiblock definition id
     * @param origin world-space origin of the formed multiblock
     * @param orientation orientation used when the multiblock formed
     * @param partPositions world-space positions occupied by the formed multiblock
     */
    public MultiblockVisualContext {
        origin = origin.immutable();
        partPositions = List.copyOf(partPositions.stream()
                .map(BlockPos::immutable)
                .toList());
    }

}