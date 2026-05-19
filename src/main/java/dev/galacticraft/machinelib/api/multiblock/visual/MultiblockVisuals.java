/*
 * Copyright (c) 2021-2025 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.galacticraft.machinelib.api.multiblock.visual;

import dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf.StaticGltfMultiblockVisual;
import dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf.StaticGltfVisualTransform;
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
     * <p>The returned factory also implements {@link PreviewableMultiblockVisualFactory},
     * allowing GUI systems such as the schematic workbench to discover the correct
     * preview model id without guessing from the multiblock definition id.</p>
     *
     * @param modelId glTF visual model id
     * @param transform anchor-relative visual transform
     * @return static glTF visual factory
     */
    public static MultiblockVisualFactory staticGltfModel(
            final ResourceLocation modelId,
            final StaticGltfVisualTransform transform
    ) {
        return new StaticGltfVisualFactory(
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

    /**
     * Visual factory for static glTF multiblock visuals.
     *
     * <p>This record is used instead of a lambda so the factory can expose the glTF
     * model id for GUI previews. A lambda can create the visual, but it cannot be
     * inspected later by the schematic workbench.</p>
     *
     * @param modelId glTF visual model id
     * @param transform anchor-relative visual transform
     */
    private record StaticGltfVisualFactory(
            ResourceLocation modelId,
            StaticGltfVisualTransform transform
    ) implements MultiblockVisualFactory, PreviewableMultiblockVisualFactory {
        /**
         * Creates the runtime formed-multiblock visual.
         *
         * @param context visual creation context
         * @return static glTF multiblock visual
         */
        @Override
        public MultiblockVisual create(final MultiblockVisualContext context) {
            return new StaticGltfMultiblockVisual(
                    this.modelId,
                    this.transform
            );
        }

        /**
         * Gets the glTF model id used for GUI previews.
         *
         * @return preview glTF model id
         */
        @Override
        public ResourceLocation previewVisualModelId() {
            return this.modelId;
        }
    }
}