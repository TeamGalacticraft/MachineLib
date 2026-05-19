package dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf;

import java.util.List;

/**
 * Static mesh data parsed from a glTF visual model.
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