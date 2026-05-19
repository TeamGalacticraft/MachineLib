package dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf;

import java.util.List;

/**
 * Static mesh data parsed from a glTF visual model.
 *
 * <p>This is a renderer-ready representation, not a raw glTF representation.
 * All node transforms are already applied and all indexed geometry has been
 * expanded into triangles.</p>
 *
 * @param triangles immutable triangle list
 */
public record GltfVisualMesh(
        List<GltfVisualTriangle> triangles
) {

    /**
     * Creates a visual mesh.
     *
     * @param triangles triangle list
     */
    public GltfVisualMesh {
        triangles = List.copyOf(triangles);
    }

}