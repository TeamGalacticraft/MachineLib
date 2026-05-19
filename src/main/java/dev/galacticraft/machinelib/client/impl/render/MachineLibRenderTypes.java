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