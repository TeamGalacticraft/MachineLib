package dev.galacticraft.machinelib.api.multiblock.port.conflict;

import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * Scope describing where a conflict rule applies.
 *
 * @param type scope type
 * @param portType port type for {@link MultiblockPortConflictScopeType#PORT_TYPE}
 * @param relativePos pattern-relative block position for block/face scopes
 * @param face local face for {@link MultiblockPortConflictScopeType#PORT_FACE}
 */
public record MultiblockPortConflictScope(
        MultiblockPortConflictScopeType type,
        MultiblockPortType portType,
        BlockPos relativePos,
        Direction face
) {

    /**
     * Creates a scope that applies to the entire multiblock.
     *
     * @return multiblock-wide scope
     */
    public static MultiblockPortConflictScope multiblock() {
        return new MultiblockPortConflictScope(
                MultiblockPortConflictScopeType.MULTIBLOCK,
                null,
                null,
                null
        );
    }

    /**
     * Creates a scope that applies to every option of one port type.
     *
     * @param portType port type
     * @return port type scope
     */
    public static MultiblockPortConflictScope portType(final MultiblockPortType portType) {
        return new MultiblockPortConflictScope(
                MultiblockPortConflictScopeType.PORT_TYPE,
                portType,
                null,
                null
        );
    }

    /**
     * Creates a scope that applies to every configurable face on one block.
     *
     * @param relativePos pattern-relative block position
     * @return block scope
     */
    public static MultiblockPortConflictScope portBlock(final BlockPos relativePos) {
        return new MultiblockPortConflictScope(
                MultiblockPortConflictScopeType.PORT_BLOCK,
                null,
                relativePos.immutable(),
                null
        );
    }

    /**
     * Creates a scope that applies to one exact block face.
     *
     * @param relativePos pattern-relative block position
     * @param face local face direction
     * @return face scope
     */
    public static MultiblockPortConflictScope portFace(
            final BlockPos relativePos,
            final Direction face
    ) {
        return new MultiblockPortConflictScope(
                MultiblockPortConflictScopeType.PORT_FACE,
                null,
                relativePos.immutable(),
                face
        );
    }

    /**
     * Checks whether this scope applies to a validation context.
     *
     * @param context validation context
     * @return {@code true} if this scope applies
     */
    public boolean matches(final MultiblockPortConflictContext context) {
        return switch (this.type) {
            case MULTIBLOCK -> true;
            case PORT_TYPE -> context.port() != null && context.port().type() == this.portType;
            case PORT_BLOCK -> context.face().relativePos().equals(this.relativePos);
            case PORT_FACE -> context.face().relativePos().equals(this.relativePos)
                    && context.face().face() == this.face;
        };
    }
}