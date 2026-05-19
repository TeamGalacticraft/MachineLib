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

package dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.galacticraft.machinelib.api.multiblock.visual.MultiblockVisualContext;
import dev.galacticraft.machinelib.client.impl.multiblock.visual.ClientFormedMultiblockVisual;
import dev.galacticraft.machinelib.client.impl.render.MachineLibRenderTypes;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Static glTF multiblock visual renderer.
 *
 * <p>This renderer places a loaded glTF model relative to a declared anchor block
 * inside the formed multiblock. The model offset is local to that anchor and is
 * rotated by the multiblock orientation, so directional and non-standard
 * orientations render consistently.</p>
 */
public final class StaticGltfMultiblockVisual implements ClientFormedMultiblockVisual {

    private final ResourceLocation modelId;
    private final StaticGltfVisualTransform transform;

    /**
     * Creates a static glTF visual using the default origin anchor.
     *
     * @param modelId loaded visual model id
     */
    public StaticGltfMultiblockVisual(final ResourceLocation modelId) {
        this(
                modelId,
                StaticGltfVisualTransform.identity()
        );
    }

    /**
     * Creates a static glTF visual.
     *
     * @param modelId loaded visual model id
     * @param transform anchor-relative visual transform
     */
    public StaticGltfMultiblockVisual(
            final ResourceLocation modelId,
            final StaticGltfVisualTransform transform
    ) {
        this.modelId = modelId;
        this.transform = transform;
    }

    /**
     * Renders the loaded glTF model as camera-relative debug wireframe geometry.
     *
     * <p>This implementation transforms every model-space vertex manually into
     * world space using {@link StaticGltfVisualTransform#modelPointToWorld}. This
     * avoids pose-stack matrix ordering issues and guarantees the visual follows the
     * same orientation and pattern dimensions as the formed multiblock.</p>
     *
     * @param context immutable formed multiblock visual context
     * @param bounds world-space bounds of the formed multiblock instance
     * @param renderContext Fabric world render context
     * @param matrices active pose stack
     * @param cameraPos current camera position
     * @param tickDelta partial tick value
     */
    @Override
    public void render(
            final MultiblockVisualContext context,
            final AABB bounds,
            final WorldRenderContext renderContext,
            final PoseStack matrices,
            final Vec3 cameraPos,
            final float tickDelta
    ) {
        final GltfVisualModel model = GltfVisualModelManager.INSTANCE.get(this.modelId);

        if (model == null || renderContext.consumers() == null) {
            return;
        }

        matrices.pushPose();

        for (final GltfVisualTriangle triangle : model.mesh().triangles()) {
            final GltfVisualMaterial material = model.materials()
                    .get(Math.min(
                            triangle.materialIndex(),
                            model.materials().size() - 1
                    ));

            if (material.textureId() == null) {
                continue;
            }

            final VertexConsumer consumer = renderContext.consumers()
                    .getBuffer(material.translucent()
                            ? MachineLibRenderTypes.gltfTranslucentTriangles(material.textureId())
                            : MachineLibRenderTypes.gltfTriangles(material.textureId()));

            renderTriangle(
                    context,
                    matrices,
                    consumer,
                    cameraPos,
                    triangle
            );
        }

        matrices.popPose();
    }

    /**
     * Renders one glTF triangle into a triangle-based render buffer.
     *
     * @param context visual context
     * @param matrices active pose stack
     * @param consumer vertex consumer
     * @param cameraPos camera position
     * @param triangle triangle to render
     */
    private void renderTriangle(
            final MultiblockVisualContext context,
            final PoseStack matrices,
            final VertexConsumer consumer,
            final Vec3 cameraPos,
            final GltfVisualTriangle triangle
    ) {
        renderVertex(
                context,
                matrices,
                consumer,
                cameraPos,
                triangle.a()
        );
        renderVertex(
                context,
                matrices,
                consumer,
                cameraPos,
                triangle.b()
        );
        renderVertex(
                context,
                matrices,
                consumer,
                cameraPos,
                triangle.c()
        );
    }

    private void renderVertex(
            final MultiblockVisualContext context,
            final PoseStack matrices,
            final VertexConsumer consumer,
            final Vec3 cameraPos,
            final GltfVisualVertex vertex
    ) {
        final Vec3 worldPos = this.transform.modelPointToWorld(
                vertex.position(),
                context.origin(),
                context.orientation(),
                context.patternWidth(),
                context.patternHeight(),
                context.patternDepth()
        );

        final Vector3f normal = this.transform.modelNormalToWorld(
                vertex.normal(),
                context.orientation()
        );

        consumer.addVertex(
                        matrices.last(),
                        (float) (worldPos.x - cameraPos.x),
                        (float) (worldPos.y - cameraPos.y),
                        (float) (worldPos.z - cameraPos.z)
                )
                .setColor(255, 255, 255, 255)
                .setUv(
                        vertex.uv().x(),
                        vertex.uv().y()
                )
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(
                        matrices.last(),
                        normal.x(),
                        normal.y(),
                        normal.z()
                );
    }

    /**
     * Converts a world-space point into camera-relative render coordinates.
     *
     * @param point world-space point
     * @param cameraPos camera position
     * @return camera-relative point
     */
    private static Vector3f cameraRelative(
            final Vec3 point,
            final Vec3 cameraPos
    ) {
        return new Vector3f(
                (float) (point.x - cameraPos.x),
                (float) (point.y - cameraPos.y),
                (float) (point.z - cameraPos.z)
        );
    }

    /**
     * Emits one white debug line.
     *
     * @param matrices active pose stack
     * @param consumer line vertex consumer
     * @param from first point
     * @param to second point
     */
    private static void renderLine(
            final PoseStack matrices,
            final VertexConsumer consumer,
            final Vector3f from,
            final Vector3f to
    ) {
        final PoseStack.Pose pose = matrices.last();

        final float dx = to.x() - from.x();
        final float dy = to.y() - from.y();
        final float dz = to.z() - from.z();
        final float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (length <= 0.0F) {
            return;
        }

        consumer.addVertex(
                        pose,
                        from.x(),
                        from.y(),
                        from.z()
                )
                .setColor(255, 255, 255, 255)
                .setNormal(
                        pose,
                        dx / length,
                        dy / length,
                        dz / length
                );

        consumer.addVertex(
                        pose,
                        to.x(),
                        to.y(),
                        to.z()
                )
                .setColor(255, 255, 255, 255)
                .setNormal(
                        pose,
                        dx / length,
                        dy / length,
                        dz / length
                );
    }

}