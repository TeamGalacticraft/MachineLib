package dev.galacticraft.machinelib.client.api.screen.port;

import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Backend data source for a reusable 3D port configuration preview.
 */
public interface PortPreviewScene {

    /**
     * Gets blocks to render in the preview.
     *
     * @return preview block list
     */
    List<PreviewBlock> blocks();

    /**
     * Gets selectable/configurable faces.
     *
     * @return preview port face list
     */
    List<PreviewPortFace> portFaces();

    /**
     * Gets the clamped camera bounds.
     *
     * @return preview bounds
     */
    default PortPreviewBounds bounds() {
        return PortPreviewBounds.fromPrimaryBlocks(this.blocks());
    }

    /**
     * Gets the preview title.
     *
     * @return title
     */
    default Component title() {
        return Component.literal("Port Config");
    }

    /**
     * Cycles the configuration for a selected face.
     *
     * @param face selected preview face
     * @param reverse whether to cycle backward
     */
    void cyclePort(PreviewPortFace face, boolean reverse);

    /**
     * Removes the configuration from a selected face.
     *
     * @param face selected preview face
     */
    void removePort(PreviewPortFace face);
}