package dev.galacticraft.machinelib.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Context passed to a multiblock menu factory when a formed multiblock opens a
 * menu.
 *
 * <p>This is similar to {@link MultiblockPartInteractionContext}, but is menu
 * specific and includes the menu sync id and player inventory through the
 * factory call instead of through this context object.</p>
 */
public interface MultiblockMenuContext {

    /**
     * Gets the level containing the formed multiblock.
     *
     * @return server level
     */
    ServerLevel level();

    /**
     * Gets the player opening the menu.
     *
     * @return server player
     */
    ServerPlayer player();

    /**
     * Gets the clicked world position.
     *
     * @return clicked part position
     */
    BlockPos clickedPos();

    /**
     * Gets the formed multiblock origin.
     *
     * @return multiblock origin
     */
    BlockPos origin();

    /**
     * Gets the formed multiblock instance id.
     *
     * @return instance id
     */
    UUID instanceId();

    /**
     * Gets the multiblock orientation.
     *
     * @return formed orientation
     */
    MultiblockOrientation orientation();

    /**
     * Gets the registered multiblock definition.
     *
     * @return multiblock definition
     */
    MultiblockDefinition definition();

    /**
     * Gets the original interaction context that caused the menu to open.
     *
     * @return interaction context
     */
    MultiblockPartInteractionContext interaction();

}