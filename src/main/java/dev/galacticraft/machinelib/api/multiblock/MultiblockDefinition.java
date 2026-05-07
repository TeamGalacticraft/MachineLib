package dev.galacticraft.machinelib.api.multiblock;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;

import java.util.List;

/**
 * Immutable definition of a MachineLib multiblock.
 *
 * <p>A definition describes the structure pattern, formation rules, and optional
 * runtime behaviour for a formed multiblock.</p>
 */
public interface MultiblockDefinition {

    /**
     * Gets the unique registry id for this multiblock.
     *
     * @return multiblock id
     */
    ResourceLocation id();

    /**
     * Gets the multiblock structure pattern.
     *
     * @return pattern
     */
    MultiblockPattern pattern();

    /**
     * Gets the formation rules for this multiblock.
     *
     * @return immutable rule list
     */
    List<FormationRule> rules();

    /**
     * Handles interaction with one formed part of this multiblock.
     *
     * <p>The default implementation passes the interaction through to the
     * original clicked block. Definitions that want controller routing, menus,
     * ports, debug messages, or custom behaviour should override this method.</p>
     *
     * @param context interaction context
     * @return interaction result
     */
    default InteractionResult usePart(final MultiblockPartInteractionContext context) {
        return InteractionResult.PASS;
    }

}