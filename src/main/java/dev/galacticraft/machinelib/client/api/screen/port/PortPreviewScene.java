package dev.galacticraft.machinelib.client.api.screen.port;

import java.util.List;

/**
 * Backend data source for a reusable 3D port configuration preview.
 *
 * <p>The widget should only render blocks, render selectable faces, and forward
 * clicks back into this scene. The scene owns the machine-specific conversion
 * between preview-space faces and real menu configuration actions.</p>
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