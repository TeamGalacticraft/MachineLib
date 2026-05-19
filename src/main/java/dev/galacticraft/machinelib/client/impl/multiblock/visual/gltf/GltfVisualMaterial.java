package dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf;

import net.minecraft.resources.ResourceLocation;

/**
 * Render material used by a loaded glTF visual model.
 *
 * @param textureId registered Minecraft texture id
 * @param translucent whether this material should render as translucent
 */
public record GltfVisualMaterial(
        ResourceLocation textureId,
        boolean translucent
) {

}