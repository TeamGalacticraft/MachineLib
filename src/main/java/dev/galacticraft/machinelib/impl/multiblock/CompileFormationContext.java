package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.FormationContext;
import dev.galacticraft.machinelib.api.multiblock.MultiblockDefinition;
import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class CompileFormationContext implements FormationContext {

    private final MultiblockOrientation orientation;
    private final MultiblockDefinition definition;

    public CompileFormationContext(
            final MultiblockOrientation orientation,
            final MultiblockDefinition definition
    ) {
        this.orientation = orientation;
        this.definition = definition;
    }

    @Override
    public ServerLevel level() {
        return null;
    }

    @Override
    public BlockPos origin() {
        return BlockPos.ZERO;
    }

    @Override
    public MultiblockOrientation orientation() {
        return this.orientation;
    }

    @Override
    public MultiblockDefinition definition() {
        return this.definition;
    }

}