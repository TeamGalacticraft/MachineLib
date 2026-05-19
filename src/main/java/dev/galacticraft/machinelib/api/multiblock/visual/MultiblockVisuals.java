package dev.galacticraft.machinelib.api.multiblock.visual;

import dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf.StaticGltfMultiblockVisual;
import dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf.StaticGltfVisualTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/**
 * Utility factories for common MachineLib multiblock visual behaviours.
 */
public final class MultiblockVisuals {

    private static final MultiblockVisualFactory NONE = context -> null;

    private MultiblockVisuals() {

    }

    /**
     * Gets a visual factory that creates no visual.
     *
     * @return no-op visual factory
     */
    public static MultiblockVisualFactory none() {
        return NONE;
    }

    /**
     * Creates a factory for a static glTF visual model using the default
     * one-block identity transform.
     *
     * @param modelId glTF visual model id
     * @return static glTF visual factory
     */
    public static MultiblockVisualFactory staticGltfModel(final ResourceLocation modelId) {
        return staticGltfModel(
                modelId,
                StaticGltfVisualTransform.identity()
        );
    }

    /**
     * Creates a factory for a static glTF visual model using a full visual
     * transform.
     *
     * @param modelId glTF visual model id
     * @param transform anchor-relative visual transform
     * @return static glTF visual factory
     */
    public static MultiblockVisualFactory staticGltfModel(
            final ResourceLocation modelId,
            final StaticGltfVisualTransform transform
    ) {
        return context -> new StaticGltfMultiblockVisual(
                modelId,
                transform
        );
    }

    /**
     * Creates a static glTF visual model using an explicit model origin in pattern
     * space.
     *
     * @param modelId glTF visual model id
     * @param modelOriginInPattern pattern-space location of the glTF 0,0,0 point
     * @param modelForward pattern-local direction represented by model +Z
     * @param modelUp pattern-local direction represented by model +Y
     * @return static glTF visual factory
     */
    public static MultiblockVisualFactory staticGltfModel(
            final ResourceLocation modelId,
            final Vec3 modelOriginInPattern,
            final Direction modelForward,
            final Direction modelUp
    ) {
        return staticGltfModel(
                modelId,
                new StaticGltfVisualTransform(
                        modelOriginInPattern,
                        modelForward,
                        modelUp
                )
        );
    }

}