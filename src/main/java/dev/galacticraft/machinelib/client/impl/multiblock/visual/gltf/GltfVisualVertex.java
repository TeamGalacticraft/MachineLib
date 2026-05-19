package dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf;

import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * One transformed vertex in a MachineLib glTF visual model.
 *
 * @param position transformed model-space vertex position
 * @param normal transformed model-space normal
 * @param uv texture UV coordinate
 */
public record GltfVisualVertex(
        Vector3f position,
        Vector3f normal,
        Vector2f uv
) {

    /**
     * Creates a visual vertex.
     *
     * @param position transformed model-space vertex position
     * @param normal transformed model-space normal
     * @param uv texture UV coordinate
     */
    public GltfVisualVertex {
        position = new Vector3f(position);
        normal = new Vector3f(normal);
        uv = new Vector2f(uv);
    }

}