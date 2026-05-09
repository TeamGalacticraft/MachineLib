package dev.galacticraft.machinelib.client.api.screen.port;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * One block rendered inside a 3D port preview scene.
 *
 * @param previewPos preview-space block position
 * @param state block state to render
 * @param primary whether this block is part of the configured machine itself
 */
public record PreviewBlock(
        BlockPos previewPos,
        BlockState state,
        boolean primary
) {

    public PreviewBlock {
        previewPos = previewPos.immutable();
    }

}