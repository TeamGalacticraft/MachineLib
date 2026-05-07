package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import dev.galacticraft.machinelib.api.multiblock.MultiblockSlotPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public final class MultiblockPart {

    private final BlockPos worldPos;
    private final BlockPos originalRelativePos;
    private final BlockPos transformedRelativePos;
    private final MultiblockSlotPredicate predicate;

    public MultiblockPart(
            final BlockPos worldPos,
            final BlockPos originalRelativePos,
            final BlockPos transformedRelativePos,
            final MultiblockSlotPredicate predicate
    ) {
        this.worldPos = worldPos.immutable();
        this.originalRelativePos = originalRelativePos.immutable();
        this.transformedRelativePos = transformedRelativePos.immutable();
        this.predicate = predicate;
    }

    public BlockPos worldPos() {
        return this.worldPos;
    }

    public BlockPos originalRelativePos() {
        return this.originalRelativePos;
    }

    public BlockPos transformedRelativePos() {
        return this.transformedRelativePos;
    }

    public MultiblockSlotPredicate predicate() {
        return this.predicate;
    }

    public MultiblockPartData createData(
            final UUID instanceId,
            final ResourceLocation definitionId,
            final BlockPos origin,
            final MultiblockOrientation orientation
    ) {
        return new MultiblockPartData(
                instanceId,
                definitionId,
                origin,
                this.worldPos,
                this.originalRelativePos,
                this.transformedRelativePos,
                orientation
        );
    }

}