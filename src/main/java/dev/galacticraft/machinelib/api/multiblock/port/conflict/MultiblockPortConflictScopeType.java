package dev.galacticraft.machinelib.api.multiblock.port.conflict;

/**
 * Scope type for a multiblock port conflict rule assignment.
 */
public enum MultiblockPortConflictScopeType {

    /**
     * Applies to every configurable port option on the multiblock.
     */
    MULTIBLOCK,

    /**
     * Applies to every option with a specific port type.
     */
    PORT_TYPE,

    /**
     * Applies to every port face on one pattern-relative block.
     */
    PORT_BLOCK,

    /**
     * Applies to one exact pattern-relative block face.
     */
    PORT_FACE
}