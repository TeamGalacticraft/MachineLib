package dev.galacticraft.machinelib.client.api.screen.port;

import java.util.List;

/**
 * Cached client-side baked preview mesh.
 *
 * <p>This mesh contains static structure geometry only. Dynamic port overlays
 * are intentionally excluded because port configuration changes frequently and
 * should remain cheap to redraw.</p>
 *
 * @param key cache key used to identify the source block list
 * @param quads cached preview-space quads
 */
public record PortPreviewMesh(
        PortPreviewMeshKey key,
        List<PortPreviewQuad> quads
) {

}