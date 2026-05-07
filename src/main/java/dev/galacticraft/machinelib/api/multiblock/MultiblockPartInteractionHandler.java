package dev.galacticraft.machinelib.api.multiblock;

import net.minecraft.world.InteractionResult;

/**
 * Handles player interaction with a formed multiblock part.
 *
 * <p>Returning {@link InteractionResult#PASS} allows normal block interaction to
 * continue. Returning {@link InteractionResult#SUCCESS} or
 * {@link InteractionResult#CONSUME} claims the interaction for the multiblock.</p>
 */
@FunctionalInterface
public interface MultiblockPartInteractionHandler {

    /**
     * Handles interaction with a formed multiblock part.
     *
     * @param context interaction context
     * @return interaction result
     */
    InteractionResult usePart(MultiblockPartInteractionContext context);

}