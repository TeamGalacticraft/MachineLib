package dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf;

/**
 * One triangle in a loaded MachineLib glTF visual mesh.
 *
 * <p>glTF triangle primitives are converted into explicit triangle objects so
 * the renderer and later mesh baker do not need to understand glTF accessors,
 * buffer views, or index formats.</p>
 *
 * @param a first vertex
 * @param b second vertex
 * @param c third vertex
 */
public record GltfVisualTriangle(
        GltfVisualVertex a,
        GltfVisualVertex b,
        GltfVisualVertex c
) {

}