package dev.galacticraft.machinelib.api.multiblock.port.conflict.rules;

import dev.galacticraft.machinelib.api.multiblock.port.ConfiguredMultiblockPort;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortFace;
import dev.galacticraft.machinelib.api.multiblock.port.conflict.AbstractMultiblockPortConflictRule;
import dev.galacticraft.machinelib.api.multiblock.port.conflict.MultiblockPortConflictContext;
import dev.galacticraft.machinelib.client.api.screen.port.PreviewPortOptionState;

/**
 * Conflict rule that allows only one matching type/mode/target route per multiblock.
 *
 * <p>This does not prevent multiple item ports, multiple input ports, or multiple
 * ports targeting the same group individually. It only conflicts when another
 * configured face already uses the exact same type, mode, and target.</p>
 */
public final class OnlyOneUniquePortPerMultiblockConflictRule extends AbstractMultiblockPortConflictRule {

    /**
     * Validates a non-clear configured port candidate.
     *
     * @param context validation context
     * @return validation state for the candidate option
     */
    @Override
    protected PreviewPortOptionState validatePort(final MultiblockPortConflictContext context) {
        final ConfiguredMultiblockPort candidate = context.port();

        for (final MultiblockPortFace otherFace : context.menu().exposedPortFaces()) {
            if (otherFace.equals(context.face())) {
                continue;
            }

            final ConfiguredMultiblockPort otherPort = context.menu().configuredPortAt(otherFace)
                    .orElse(null);

            if (otherPort == null) {
                continue;
            }

            if (otherPort.type() == candidate.type()
                    && otherPort.mode() == candidate.mode()
                    && otherPort.target().equals(candidate.target())) {
                return PreviewPortOptionState.CONFLICT;
            }
        }

        return PreviewPortOptionState.VALID;
    }
}