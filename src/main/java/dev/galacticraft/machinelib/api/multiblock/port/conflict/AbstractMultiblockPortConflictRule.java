package dev.galacticraft.machinelib.api.multiblock.port.conflict;

import dev.galacticraft.machinelib.client.api.screen.port.PreviewPortOptionState;

/**
 * Base class for custom multiblock port conflict rules.
 *
 * <p>This class handles common safety checks, such as clear/no-port options.
 * Developers only need to implement {@link #validatePort(MultiblockPortConflictContext)}
 * for real configured port candidates.</p>
 */
public abstract class AbstractMultiblockPortConflictRule implements MultiblockPortConflictRule {

    /**
     * Validates one candidate port option.
     *
     * @param context validation context
     * @return validation state for the candidate option
     */
    @Override
    public final PreviewPortOptionState validate(final MultiblockPortConflictContext context) {
        if (context.option().clearsPort()) {
            return PreviewPortOptionState.VALID;
        }

        if (context.port() == null) {
            return PreviewPortOptionState.VALID;
        }

        return this.validatePort(context);
    }

    /**
     * Validates a non-clear configured port candidate.
     *
     * @param context validation context
     * @return validation state for the candidate option
     */
    protected abstract PreviewPortOptionState validatePort(MultiblockPortConflictContext context);
}