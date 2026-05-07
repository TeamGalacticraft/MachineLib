package dev.galacticraft.machinelib.client.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * Lightweight client-side data for one formed multiblock part.
 *
 * <p>This is not a validation object. The client only uses it to know that a
 * world position currently belongs to a server-confirmed formed multiblock.</p>
 */
public record ClientMultiblockPartData(
        UUID instanceId,
        ResourceLocation definitionId,
        BlockPos origin,
        MultiblockOrientation orientation,
        BlockPos worldPos
) {

}