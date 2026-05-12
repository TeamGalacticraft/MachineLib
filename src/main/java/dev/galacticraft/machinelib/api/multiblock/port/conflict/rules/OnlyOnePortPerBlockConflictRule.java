package dev.galacticraft.machinelib.api.multiblock.port.conflict.rules;

import dev.galacticraft.machinelib.api.multiblock.port.ConfiguredMultiblockPort;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortFace;
import dev.galacticraft.machinelib.api.multiblock.port.conflict.AbstractMultiblockPortConflictRule;
import dev.galacticraft.machinelib.api.multiblock.port.conflict.MultiblockPortConflictContext;
import dev.galacticraft.machinelib.client.api.screen.port.PreviewPortOptionState;
import net.minecraft.core.BlockPos;

/**
 * Conflict rule that allows only one configured port per pattern-relative block.
 *
 * <p>If any other face on the same block already has a configured port, this rule
 * marks the candidate option as conflicting.</p>
 */
public final class OnlyOnePortPerBlockConflictRule extends AbstractMultiblockPortConflictRule {

    /**
     * Validates a non-clear configured port candidate.
     *
     * @param context validation context
     * @return validation state for the candidate option
     */
    @Override
    protected PreviewPortOptionState validatePort(final MultiblockPortConflictContext context) {
        final BlockPos candidateBlock = context.face().relativePos();

        for (final MultiblockPortFace otherFace : context.menu().exposedPortFaces()) {
            if (otherFace.equals(context.face())) {
                continue;
            }

            if (!otherFace.relativePos().equals(candidateBlock)) {
                continue;
            }

            final ConfiguredMultiblockPort otherPort = context.menu().configuredPortAt(otherFace)
                    .orElse(null);

            if (otherPort != null) {
                return PreviewPortOptionState.CONFLICT;
            }
        }

        return PreviewPortOptionState.VALID;
    }
}