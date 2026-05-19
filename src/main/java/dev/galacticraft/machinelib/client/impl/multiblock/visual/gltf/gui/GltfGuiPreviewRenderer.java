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

package dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf.GltfVisualModel;
import dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf.GltfVisualTriangle;
import dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf.GltfVisualVertex;
import dev.galacticraft.machinelib.client.impl.render.MachineLibRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;


/**
 * Optimized renderer for drawing MachineLib glTF visual models inside GUI panels.
 *
 * <p>This renderer reuses the loaded glTF mesh data and cached material grouping. It does not
 * allocate a new mesh, does not rebuild model geometry per frame, and does not perform any
 * resource lookup while emitting triangles. Only the current GUI pose matrix changes per frame.</p>
 *
 * <p>The renderer intentionally uses a simple full-bright triangle pipeline for previews. The
 * preview should be fast, sharp, and predictable rather than physically accurate.</p>
 */
public final class GltfGuiPreviewRenderer {
    private static final float DEFAULT_PITCH = 28.0F;
    private static final float DEFAULT_YAW = 35.0F;
    private static final float DEFAULT_FILL = 0.82F;
    private static final float DEFAULT_Z_SCALE = 1.0F;

    private GltfGuiPreviewRenderer() {
    }

    /**
     * Renders a glTF model into a GUI preview rectangle using a fixed canonical preview angle.
     *
     * <p>The preview is intentionally not animated. It points the model's canonical forward
     * direction toward the viewer, with a slight yaw angle so the shape remains readable.</p>
     *
     * @param graphics active GUI graphics
     * @param model model to render
     * @param x preview x
     * @param y preview y
     * @param width preview width
     * @param height preview height
     * @param tickDelta frame tick delta
     */
    public static void render(
            final @NotNull GuiGraphics graphics,
            final @NotNull GltfVisualModel model,
            final int x,
            final int y,
            final int width,
            final int height,
            final float tickDelta
    ) {
        render(
                graphics,
                model,
                x,
                y,
                width,
                height,
                -65.0F,
                25.0F,
                DEFAULT_FILL
        );
    }

    /**
     * Renders a glTF model into a GUI preview rectangle.
     *
     * @param graphics active GUI graphics
     * @param model model to render
     * @param x preview x
     * @param y preview y
     * @param width preview width
     * @param height preview height
     * @param yawDegrees yaw rotation in degrees
     * @param pitchDegrees pitch rotation in degrees
     * @param fill fraction of preview area to fill
     */
    public static void render(
            final @NotNull GuiGraphics graphics,
            final @NotNull GltfVisualModel model,
            final int x,
            final int y,
            final int width,
            final int height,
            final float yawDegrees,
            final float pitchDegrees,
            final float fill
    ) {
        if (width <= 2 || height <= 2) {
            return;
        }

        final GltfGuiPreviewCache.CachedModel cached = GltfGuiPreviewCache.get(model);

        graphics.enableScissor(
                x,
                y,
                x + width,
                y + height
        );

        RenderSystem.enableDepthTest();

        final PoseStack pose = graphics.pose();
        pose.pushPose();

        pose.translate(
                x + width * 0.5F,
                y + height * 0.58F,
                240.0F
        );

        final float scale = Math.min(width, height) * fill / cached.bounds().maxDimension();

        pose.scale(scale, -scale, scale);
        pose.mulPose(new org.joml.Quaternionf().rotationX((float) Math.toRadians(pitchDegrees)));
        pose.mulPose(new org.joml.Quaternionf().rotationY((float) Math.toRadians(yawDegrees)));
        pose.translate(
                -cached.bounds().center().x(),
                -cached.bounds().center().y(),
                -cached.bounds().center().z()
        );

        final MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();

        for (final GltfGuiPreviewCache.MaterialGroup group : cached.groups()) {
            if (group.material().textureId() == null || group.triangles().isEmpty()) {
                continue;
            }

            final VertexConsumer consumer = buffers.getBuffer(group.material().translucent()
                    ? MachineLibRenderTypes.gltfTranslucentTriangles(group.material().textureId())
                    : MachineLibRenderTypes.gltfTriangles(group.material().textureId()));

            for (final GltfVisualTriangle triangle : group.triangles()) {
                renderTriangle(pose, consumer, triangle);
            }
        }

        buffers.endBatch();

        pose.popPose();

        RenderSystem.disableDepthTest();
        graphics.disableScissor();
    }

    /**
     * Emits one triangle.
     *
     * @param pose pose stack
     * @param consumer vertex consumer
     * @param triangle triangle
     */
    private static void renderTriangle(
            final PoseStack pose,
            final VertexConsumer consumer,
            final GltfVisualTriangle triangle
    ) {
        renderVertex(pose, consumer, triangle.a());
        renderVertex(pose, consumer, triangle.b());
        renderVertex(pose, consumer, triangle.c());
    }

    /**
     * Emits one vertex.
     *
     * @param pose pose stack
     * @param consumer vertex consumer
     * @param vertex vertex
     */
    private static void renderVertex(
            final PoseStack pose,
            final VertexConsumer consumer,
            final GltfVisualVertex vertex
    ) {
        final PoseStack.Pose current = pose.last();
        final Matrix4f positionMatrix = current.pose();
        final Matrix3f normalMatrix = current.normal();

        final Vector3f normal = new Vector3f(vertex.normal())
                .mul(normalMatrix)
                .normalize();

        final float shade = calculateShade(normal);
        final int color = Math.max(120, Math.min(255, Math.round(255.0F * shade)));

        consumer.addVertex(
                        current,
                        vertex.position().x(),
                        vertex.position().y(),
                        vertex.position().z() * DEFAULT_Z_SCALE
                )
                .setColor(color, color, color, 255)
                .setUv(vertex.uv().x(), vertex.uv().y())
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(current, normal.x(), normal.y(), normal.z());
    }

    /**
     * MAX light on everything
     *
     * @param normal transformed normal
     * @return brightness multiplier
     */
    private static float calculateShade(final Vector3f normal) {
//        final Vector3f light = new Vector3f(0.0F, 0.0F, -1.0F).normalize();
//        final float frontLight = Math.max(0.0F, normal.dot(light));

        return 1F;
    }
}