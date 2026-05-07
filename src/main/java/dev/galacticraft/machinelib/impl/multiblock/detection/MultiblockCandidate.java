package dev.galacticraft.machinelib.impl.multiblock.detection;

import dev.galacticraft.machinelib.api.multiblock.MultiblockDefinition;
import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public record MultiblockCandidate(
        ServerLevel level,
        BlockPos origin,
        MultiblockOrientation orientation,
        MultiblockDefinition definition
) {

}