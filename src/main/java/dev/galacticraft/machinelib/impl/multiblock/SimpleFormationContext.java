package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.FormationContext;
import dev.galacticraft.machinelib.api.multiblock.MultiblockDefinition;
import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class SimpleFormationContext implements FormationContext {

    private final ServerLevel level;
    private final BlockPos origin;
    private final MultiblockOrientation orientation;
    private final MultiblockDefinition definition;

    public SimpleFormationContext(
            final ServerLevel level,
            final BlockPos origin,
            final MultiblockOrientation orientation,
            final MultiblockDefinition definition
    ) {
        this.level = level;
        this.origin = origin.immutable();
        this.orientation = orientation;
        this.definition = definition;
    }

    @Override
    public ServerLevel level() {
        return this.level;
    }

    @Override
    public BlockPos origin() {
        return this.origin;
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