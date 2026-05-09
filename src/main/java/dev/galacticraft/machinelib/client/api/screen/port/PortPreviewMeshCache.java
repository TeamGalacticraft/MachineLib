package dev.galacticraft.machinelib.client.api.screen.port;

import java.util.List;

/**
 * Small per-widget cache for static preview geometry.
 *
 * <p>The cache belongs to the widget/renderer rather than the scene because the
 * scene should remain logical data. If the scene's block list changes, the mesh
 * key changes and the cache rebuilds automatically.</p>
 */
public final class PortPreviewMeshCache {

    private PortPreviewMesh mesh;

    /**
     * Gets a cached mesh for the current scene.
     *
     * <p>The scene block list is still queried so a stable key can be calculated,
     * but the expensive baked quad extraction is only performed when the block
     * list changes.</p>
     *
     * @param scene preview scene
     * @return cached mesh
     */
    public PortPreviewMesh get(final PortPreviewScene scene) {
        final List<PreviewBlock> blocks = scene.blocks();
        final PortPreviewMeshKey key = PortPreviewMeshKey.create(blocks);

        if (this.mesh != null && this.mesh.key().equals(key)) {
            return this.mesh;
        }

        this.mesh = PortPreviewMeshBuilder.build(
                key,
                blocks
        );

        return this.mesh;
    }

    /**
     * Clears the cached mesh.
     */
    public void clear() {
        this.mesh = null;
    }
}