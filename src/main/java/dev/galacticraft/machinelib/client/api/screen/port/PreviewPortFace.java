package dev.galacticraft.machinelib.client.api.screen.port;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

/**
 * One selectable face inside a 3D port preview scene.
 *
 * @param previewPos preview-space block position
 * @param previewFace preview-space face direction
 * @param label readable label
 * @param configured whether this face currently has a configured port
 */
public record PreviewPortFace(
        BlockPos previewPos,
        Direction previewFace,
        Component label,
        boolean configured
) {

    public PreviewPortFace {
        previewPos = previewPos.immutable();
    }

}