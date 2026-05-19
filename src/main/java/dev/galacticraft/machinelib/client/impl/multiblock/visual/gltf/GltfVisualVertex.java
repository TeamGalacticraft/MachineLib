package dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf;

import org.joml.Vector3f;

/**
 * One transformed vertex in a MachineLib glTF visual model.
 *
 * <p>This first implementation stores only position data because the initial
 * renderer is a debug wireframe renderer. UVs, normals, material ids, and
 * translucent render metadata can be added once the geometry loading path is
 * proven correct.</p>
 *
 * @param position transformed model-space vertex position
 */
public record GltfVisualVertex(
        Vector3f position
) {

    /**
     * Creates a glTF visual vertex.
     *
     * @param position transformed model-space vertex position
     */
    public GltfVisualVertex {
        position = new Vector3f(position);
    }

}