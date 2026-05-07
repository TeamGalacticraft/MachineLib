package dev.galacticraft.machinelib.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * Data sent from server to client when opening a multiblock menu.
 *
 * @param instanceId formed multiblock instance id
 * @param definitionId multiblock definition id
 * @param origin formed multiblock origin
 * @param clickedPos clicked part position
 * @param orientation formed orientation
 */
public record MultiblockMenuOpeningData(
        UUID instanceId,
        ResourceLocation definitionId,
        BlockPos origin,
        BlockPos clickedPos,
        MultiblockOrientation orientation
) {

}