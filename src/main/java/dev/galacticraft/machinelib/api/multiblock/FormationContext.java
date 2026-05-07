package dev.galacticraft.machinelib.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public interface FormationContext {

    ServerLevel level();

    BlockPos origin();

    MultiblockOrientation orientation();

    MultiblockDefinition definition();

}