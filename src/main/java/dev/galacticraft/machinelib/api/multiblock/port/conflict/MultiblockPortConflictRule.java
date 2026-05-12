package dev.galacticraft.machinelib.api.multiblock.port.conflict;

import dev.galacticraft.machinelib.client.api.screen.port.PreviewPortOptionState;

/**
 * Validates one candidate multiblock port option for one selected port face.
 *
 * <p>Conflict rules are opt-in. MachineLib does not apply any conflict rules by
 * default. A multiblock definition must explicitly register conflict rules
 * through its builder for them to affect the port configuration UI.</p>
 */
@FunctionalInterface
public interface MultiblockPortConflictRule {

    /**
     * Validates one candidate port option.
     *
     * @param context validation context
     * @return validation state for the candidate option
     */
    PreviewPortOptionState validate(MultiblockPortConflictContext context);
}