package dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Loaded static glTF visual model.
 *
 * @param id MachineLib visual model id
 * @param mesh transformed static mesh
 * @param materials render materials used by the mesh
 */
public record GltfVisualModel(
        ResourceLocation id,
        GltfVisualMesh mesh,
        List<GltfVisualMaterial> materials
) {

    /**
     * Creates a glTF visual model.
     */
    public GltfVisualModel {
        materials = List.copyOf(materials);
    }

}