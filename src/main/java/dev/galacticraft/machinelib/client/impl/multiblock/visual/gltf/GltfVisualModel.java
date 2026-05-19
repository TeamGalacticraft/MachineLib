package dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf;

import net.minecraft.resources.ResourceLocation;

/**
 * Loaded static glTF visual model.
 *
 * <p>The current implementation supports embedded base64 {@code .gltf} files
 * exported by Blockbench and renders them as debug wireframe triangles. Later
 * stages can extend this same object with materials, textures, render layers,
 * and translucent sorting metadata.</p>
 *
 * @param id MachineLib visual model id
 * @param mesh transformed static mesh
 */
public record GltfVisualModel(
        ResourceLocation id,
        GltfVisualMesh mesh
) {

}