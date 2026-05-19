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

package dev.galacticraft.machinelib.client.impl.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Custom render types used by MachineLib client renderers.
 */
public final class MachineLibRenderTypes extends RenderType {

    private MachineLibRenderTypes(
            final String name,
            final VertexFormat format,
            final VertexFormat.Mode mode,
            final int bufferSize,
            final boolean affectsCrumbling,
            final boolean sortOnUpload,
            final Runnable setupState,
            final Runnable clearState
    ) {
        super(
                name,
                format,
                mode,
                bufferSize,
                affectsCrumbling,
                sortOnUpload,
                setupState,
                clearState
        );
    }

    /**
     * Creates a textured triangle render type for static glTF visuals.
     *
     * @param texture texture id
     * @return triangle render type
     */
    public static RenderType gltfTriangles(final ResourceLocation texture) {
        return create(
                "machinelib_gltf_triangles",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.TRIANGLES,
                256,
                false,
                false,
                CompositeState.builder()
                        .setShaderState(RENDERTYPE_ENTITY_CUTOUT_SHADER)
                        .setTextureState(new TextureStateShard(
                                texture,
                                false,
                                false
                        ))
                        .setTransparencyState(NO_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .createCompositeState(true)
        );
    }

    /**
     * Creates a textured translucent triangle render type for static glTF visuals.
     *
     * @param texture texture id
     * @return translucent triangle render type
     */
    public static RenderType gltfTranslucentTriangles(final ResourceLocation texture) {
        return create(
                "machinelib_gltf_translucent_triangles",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.TRIANGLES,
                256,
                false,
                true,
                CompositeState.builder()
                        .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                        .setTextureState(new TextureStateShard(
                                texture,
                                false,
                                false
                        ))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .createCompositeState(true)
        );
    }

}