package dev.galacticraft.machinelib.impl.multiblock;

/**
 * Describes the result of validating an already formed multiblock.
 *
 * <p>This is intentionally not a boolean because MachineLib must distinguish a
 * genuinely broken structure from a structure that cannot currently be inspected
 * because one or more of its chunks are unloaded.</p>
 */
public enum MultiblockValidationStatus {

    /**
     * The multiblock is fully loaded and every part still matches its predicate.
     */
    VALID,

    /**
     * The multiblock is fully loaded, but at least one part no longer matches.
     */
    INVALID,

    /**
     * At least one part position is in an unloaded chunk.
     *
     * <p>This should not delete persistent saved data.</p>
     */
    UNLOADED

}