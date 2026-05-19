package dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf;

/**
 * One triangle in a loaded MachineLib glTF visual mesh.
 *
 * @param a first vertex
 * @param b second vertex
 * @param c third vertex
 * @param materialIndex material index used by this triangle
 */
public record GltfVisualTriangle(
        GltfVisualVertex a,
        GltfVisualVertex b,
        GltfVisualVertex c,
        int materialIndex
) {

}