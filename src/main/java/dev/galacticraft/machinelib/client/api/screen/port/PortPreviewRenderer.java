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

package dev.galacticraft.machinelib.client.api.screen.port;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.galacticraft.machinelib.impl.MachineLib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Renderer for the 3D port preview.
 *
 * <p>This renderer uses real client-side baked block models for the structure
 * preview, while port faces are still rendered as 2D projected overlays. This
 * keeps the scene entirely client-side and avoids adding any server rendering
 * data.</p>
 */
public final class PortPreviewRenderer {

    private static final int PICK_RADIUS = 8;

    private PortPreviewRenderer() {

    }

    /**
     * Renders a port preview scene.
     *
     * @param graphics GUI graphics
     * @param scene preview scene
     * @param camera preview camera
     * @param hoveredFace hovered face
     * @param selectedFace selected face
     * @param x widget x
     * @param y widget y
     * @param width widget width
     * @param height widget height
     */
    public static void render(
            final GuiGraphics graphics,
            final PortPreviewScene scene,
            final PortPreviewMesh mesh,
            final PortPreviewCamera camera,
            final PreviewPortFace hoveredFace,
            final PreviewPortFace selectedFace,
            final int x,
            final int y,
            final int width,
            final int height
    ) {
        graphics.fill(
                x,
                y,
                x + width,
                y + height,
                0xCC101010
        );

        renderMesh(
                graphics,
                mesh,
                camera,
                x,
                y,
                width,
                height
        );

        renderAdjacentBlocks(
                graphics,
                scene,
                camera,
                x,
                y,
                width,
                height
        );

        renderAdjacentBlockEntities(
                graphics,
                scene,
                camera,
                x,
                y,
                width,
                height
        );

        renderPortFaces(
                graphics,
                scene,
                camera,
                hoveredFace,
                selectedFace,
                x,
                y,
                width,
                height
        );
    }

    /**
     * Picks the front-most projected port face under a screen point.
     *
     * <p>This uses the same projected face corners as the overlay renderer, so the
     * clickable area matches the visible overlay exactly. If multiple projected
     * faces overlap, the face closest to the camera is selected.</p>
     *
     * @param scene preview scene
     * @param camera camera
     * @param x widget x
     * @param y widget y
     * @param width widget width
     * @param height widget height
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @return picked face, or {@code null}
     */
    public static PreviewPortFace pick(
            final PortPreviewScene scene,
            final PortPreviewCamera camera,
            final int x,
            final int y,
            final int width,
            final int height,
            final double mouseX,
            final double mouseY
    ) {
        final int centerX = x + width / 2;
        final int centerY = y + height / 2;

        PreviewPortFace bestFace = null;
        double bestDepth = Double.NEGATIVE_INFINITY;

        for (final PreviewPortFace face : scene.portFaces()) {
            if (!isFaceCameraVisible(camera, face)) {
                continue;
            }

            final ProjectedFace projected = projectFace(
                    camera,
                    face,
                    centerX,
                    centerY,
                    0.018D,
                    0.12D
            );

            if (!pointInProjectedQuad(
                    mouseX,
                    mouseY,
                    projected.p0(),
                    projected.p1(),
                    projected.p2(),
                    projected.p3()
            )) {
                continue;
            }

            if (projected.averageDepth() > bestDepth) {
                bestDepth = projected.averageDepth();
                bestFace = face;
            }
        }

        return bestFace;
    }

    /**
     * Checks whether a port face is generally facing the camera.
     *
     * <p>This prevents selecting rear-facing port overlays that are now hidden by
     * the 3D depth buffer.</p>
     *
     * @param camera preview camera
     * @param face preview face
     * @return true if the face can be selected
     */
    private static boolean isFaceCameraVisible(
            final PortPreviewCamera camera,
            final PreviewPortFace face
    ) {
        final Vec3 center = faceCenter(
                face.previewPos(),
                face.previewFace()
        );

        final Vec3 normal = new Vec3(
                face.previewFace().getStepX(),
                face.previewFace().getStepY(),
                face.previewFace().getStepZ()
        );

        final Vec3 cameraDirection = center.subtract(camera.focus()).normalize();

        return normal.dot(cameraDirection) < 0.15D;
    }

    /**
     * Checks whether a screen point is inside a projected quad.
     *
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param p0 first projected corner
     * @param p1 second projected corner
     * @param p2 third projected corner
     * @param p3 fourth projected corner
     * @return {@code true} if the point is inside the quad
     */
    private static boolean pointInProjectedQuad(
            final double mouseX,
            final double mouseY,
            final PortPreviewCamera.ProjectedPoint p0,
            final PortPreviewCamera.ProjectedPoint p1,
            final PortPreviewCamera.ProjectedPoint p2,
            final PortPreviewCamera.ProjectedPoint p3
    ) {
        return pointInTriangle(mouseX, mouseY, p0, p1, p2)
                || pointInTriangle(mouseX, mouseY, p0, p2, p3);
    }

    /**
     * Checks whether a screen point is inside a projected triangle.
     *
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param a first triangle point
     * @param b second triangle point
     * @param c third triangle point
     * @return {@code true} if inside
     */
    private static boolean pointInTriangle(
            final double mouseX,
            final double mouseY,
            final PortPreviewCamera.ProjectedPoint a,
            final PortPreviewCamera.ProjectedPoint b,
            final PortPreviewCamera.ProjectedPoint c
    ) {
        final double denominator = (b.y() - c.y()) * (a.x() - c.x()) + (c.x() - b.x()) * (a.y() - c.y());

        if (Math.abs(denominator) < 0.000001D) {
            return false;
        }

        final double alpha = ((b.y() - c.y()) * (mouseX - c.x()) + (c.x() - b.x()) * (mouseY - c.y())) / denominator;
        final double beta = ((c.y() - a.y()) * (mouseX - c.x()) + (a.x() - c.x()) * (mouseY - c.y())) / denominator;
        final double gamma = 1.0D - alpha - beta;

        return alpha >= 0.0D && beta >= 0.0D && gamma >= 0.0D;
    }

    /**
     * Renders the cached static preview mesh.
     *
     * <p>The mesh contains baked block model quads already translated into
     * preview-space. The renderer only applies the current camera transform and
     * emits the stored vertices to the GUI render buffer.</p>
     *
     * @param graphics GUI graphics
     * @param mesh cached static preview mesh
     * @param camera preview camera
     * @param x widget x
     * @param y widget y
     * @param width widget width
     * @param height widget height
     */
    private static void renderMesh(
            final GuiGraphics graphics,
            final PortPreviewMesh mesh,
            final PortPreviewCamera camera,
            final int x,
            final int y,
            final int width,
            final int height
    ) {
        final Minecraft minecraft = Minecraft.getInstance();
        final MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        final PoseStack poseStack = graphics.pose();
        final Vec3 focus = camera.focus();

        graphics.enableScissor(
                x,
                y,
                x + width,
                y + height
        );

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, false);
        Lighting.setupFor3DItems();

        poseStack.pushPose();

        poseStack.translate(
                x + width / 2.0D,
                y + height / 2.0D,
                200.0D
        );

        poseStack.scale(
                camera.zoom(),
                -camera.zoom(),
                camera.zoom()
        );

        poseStack.mulPose(Axis.XP.rotationDegrees(camera.pitch()));
        poseStack.mulPose(Axis.YP.rotationDegrees(camera.yaw()));

        poseStack.translate(
                -focus.x,
                -focus.y,
                -focus.z
        );

        final VertexConsumer consumer = buffer.getBuffer(RenderType.cutoutMipped());

        for (final PortPreviewQuad quad : mesh.quads()) {
            renderQuad(
                    poseStack,
                    consumer,
                    quad
            );
        }

        buffer.endBatch();

        poseStack.popPose();

        Lighting.setupForFlatItems();
        RenderSystem.disableDepthTest();
        graphics.disableScissor();
    }

    /**
     * Renders non-primary adjacent context blocks dynamically.
     *
     * <p>These blocks are not part of the cached static mesh because they represent
     * the surrounding world context rather than the machine itself. Rendering them
     * dynamically allows pipes, chests, cables, and nearby solid blocks to update
     * without forcing the primary machine mesh to rebuild.</p>
     *
     * @param graphics GUI graphics
     * @param scene preview scene
     * @param camera preview camera
     * @param x widget x
     * @param y widget y
     * @param width widget width
     * @param height widget height
     */
    private static void renderAdjacentBlocks(
            final GuiGraphics graphics,
            final PortPreviewScene scene,
            final PortPreviewCamera camera,
            final int x,
            final int y,
            final int width,
            final int height
    ) {
        final Minecraft minecraft = Minecraft.getInstance();
        final BlockRenderDispatcher blockRenderer = minecraft.getBlockRenderer();
        final MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        final PoseStack poseStack = graphics.pose();
        final Vec3 focus = camera.focus();

        graphics.enableScissor(
                x,
                y,
                x + width,
                y + height
        );

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        Lighting.setupFor3DItems();

        poseStack.pushPose();

        poseStack.translate(
                x + width / 2.0D,
                y + height / 2.0D,
                200.0D
        );

        poseStack.scale(
                camera.zoom(),
                -camera.zoom(),
                camera.zoom()
        );

        poseStack.mulPose(Axis.XP.rotationDegrees(camera.pitch()));
        poseStack.mulPose(Axis.YP.rotationDegrees(camera.yaw()));

        poseStack.translate(
                -focus.x,
                -focus.y,
                -focus.z
        );

        final float opacity = MachineLib.CONFIG.portPreviewAdjacentBlockOpacity();

        for (final PreviewBlock block : scene.blocks()) {
            if (block.primary() || block.state().isAir()) {
                continue;
            }

            poseStack.pushPose();

            poseStack.translate(
                    block.previewPos().getX(),
                    block.previewPos().getY(),
                    block.previewPos().getZ()
            );

            renderTransparentBlock(
                    poseStack,
                    buffer,
                    blockRenderer,
                    block,
                    opacity
            );

            poseStack.popPose();
        }

        buffer.endBatch();

        poseStack.popPose();

        Lighting.setupForFlatItems();
        RenderSystem.disableDepthTest();
        graphics.disableScissor();
    }

    /**
     * Renders adjacent block entities using their normal client-side block entity
     * renderers.
     *
     * <p>Some blocks, such as chests, do not expose their full visual shape through
     * baked model quads. Their important geometry is rendered by a
     * {@code BlockEntityRenderer}. These cannot be generically merged into the
     * cached baked mesh, so they are rendered dynamically as neighbour context.</p>
     *
     * @param graphics GUI graphics
     * @param scene preview scene
     * @param camera preview camera
     * @param x widget x
     * @param y widget y
     * @param width widget width
     * @param height widget height
     */
    private static void renderAdjacentBlockEntities(
            final GuiGraphics graphics,
            final PortPreviewScene scene,
            final PortPreviewCamera camera,
            final int x,
            final int y,
            final int width,
            final int height
    ) {
        final Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            return;
        }

        final MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        final PoseStack poseStack = graphics.pose();
        final Vec3 focus = camera.focus();

        graphics.enableScissor(x, y, x + width, y + height);

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        Lighting.setupFor3DItems();

        poseStack.pushPose();

        poseStack.translate(
                x + width / 2.0D,
                y + height / 2.0D,
                200.0D
        );

        poseStack.scale(
                camera.zoom(),
                -camera.zoom(),
                camera.zoom()
        );

        poseStack.mulPose(Axis.XP.rotationDegrees(camera.pitch()));
        poseStack.mulPose(Axis.YP.rotationDegrees(camera.yaw()));

        poseStack.translate(
                -focus.x,
                -focus.y,
                -focus.z
        );

        for (final PreviewBlock block : scene.blocks()) {
            if (block.primary()) {
                continue;
            }

            final BlockEntity blockEntity = minecraft.level.getBlockEntity(block.worldPos());

            if (blockEntity == null) {
                continue;
            }

            poseStack.pushPose();

            poseStack.translate(
                    block.previewPos().getX(),
                    block.previewPos().getY(),
                    block.previewPos().getZ()
            );

            minecraft.getBlockEntityRenderDispatcher().renderItem(
                    blockEntity,
                    poseStack,
                    buffer,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY
            );

            poseStack.popPose();
        }

        buffer.endBatch();

        poseStack.popPose();

        Lighting.setupForFlatItems();
        RenderSystem.disableDepthTest();
        graphics.disableScissor();
    }

    /**
     * Renders one adjacent block with reduced opacity.
     *
     * <p>This manually emits the block model's baked quads so alpha can be applied.
     * {@code renderSingleBlock} is not used here because it does not provide a
     * simple per-call opacity override for GUI preview rendering.</p>
     *
     * @param poseStack current pose stack
     * @param buffer render buffer
     * @param blockRenderer block renderer
     * @param block preview block
     * @param opacity opacity from {@code 0.0F} to {@code 1.0F}
     */
    private static void renderTransparentBlock(
            final PoseStack poseStack,
            final MultiBufferSource.BufferSource buffer,
            final BlockRenderDispatcher blockRenderer,
            final PreviewBlock block,
            final float opacity
    ) {
        final VertexConsumer consumer = buffer.getBuffer(RenderType.translucent());
        final int alpha = Math.max(
                0,
                Math.min(
                        255,
                        Math.round(opacity * 255.0F)
                )
        );

        final List<BakedQuad> quads = new java.util.ArrayList<>();
        final RandomSource random = RandomSource.create(42L);

        quads.addAll(blockRenderer.getBlockModel(block.state()).getQuads(
                block.state(),
                null,
                random
        ));

        for (final Direction direction : Direction.values()) {
            quads.addAll(blockRenderer.getBlockModel(block.state()).getQuads(
                    block.state(),
                    direction,
                    RandomSource.create(42L)
            ));
        }

        for (final BakedQuad quad : quads) {
            renderTransparentBakedQuad(
                    poseStack,
                    consumer,
                    quad,
                    alpha
            );
        }
    }

    /**
     * Emits one baked quad with a custom alpha value.
     *
     * @param poseStack current pose stack
     * @param consumer vertex consumer
     * @param quad baked quad
     * @param alpha alpha from {@code 0} to {@code 255}
     */
    private static void renderTransparentBakedQuad(
            final PoseStack poseStack,
            final VertexConsumer consumer,
            final BakedQuad quad,
            final int alpha
    ) {
        final PoseStack.Pose pose = poseStack.last();
        final int[] data = quad.getVertices();

        final float normalX = quad.getDirection().getStepX();
        final float normalY = quad.getDirection().getStepY();
        final float normalZ = quad.getDirection().getStepZ();

        for (int vertex = 0; vertex < 4; vertex++) {
            final int base = vertex * 8;

            consumer.addVertex(
                            pose,
                            Float.intBitsToFloat(data[base]),
                            Float.intBitsToFloat(data[base + 1]),
                            Float.intBitsToFloat(data[base + 2])
                    )
                    .setColor(
                            255,
                            255,
                            255,
                            alpha
                    )
                    .setUv(
                            Float.intBitsToFloat(data[base + 4]),
                            Float.intBitsToFloat(data[base + 5])
                    )
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(
                            pose,
                            normalX,
                            normalY,
                            normalZ
                    );
        }
    }

    /**
     * Emits one cached preview quad.
     *
     * @param poseStack current pose stack
     * @param consumer vertex consumer
     * @param quad cached preview quad
     */
    private static void renderQuad(
            final PoseStack poseStack,
            final VertexConsumer consumer,
            final PortPreviewQuad quad
    ) {
        final PoseStack.Pose pose = poseStack.last();
        final Vec3[] vertices = quad.vertices();

        final float normalX = quad.direction().getStepX();
        final float normalY = quad.direction().getStepY();
        final float normalZ = quad.direction().getStepZ();

        for (int i = 0; i < 4; i++) {
            final Vec3 vertex = vertices[i];

            consumer.addVertex(
                            pose,
                            (float) vertex.x,
                            (float) vertex.y,
                            (float) vertex.z
                    )
                    .setColor(255, 255, 255, 255)
                    .setUv(
                            quad.u()[i],
                            quad.v()[i]
                    )
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(
                            pose,
                            normalX,
                            normalY,
                            normalZ
                    );
        }
    }

    /**
     * Renders all port face overlays as real 3D quads attached to block faces.
     *
     * <p>The overlays are emitted into the 3D preview transform and depth-tested
     * against the preview mesh. This means hidden ports are naturally occluded by
     * blocks, and partially visible ports are clipped by real depth instead of being
     * drawn as flat screen-space UI.</p>
     */
    private static void renderPortFaces(
            final GuiGraphics graphics,
            final PortPreviewScene scene,
            final PortPreviewCamera camera,
            final PreviewPortFace hoveredFace,
            final PreviewPortFace selectedFace,
            final int x,
            final int y,
            final int width,
            final int height
    ) {
        final Minecraft minecraft = Minecraft.getInstance();
        final MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        final PoseStack poseStack = graphics.pose();
        final Vec3 focus = camera.focus();
        final RenderType renderType = RenderType.guiOverlay();

        graphics.enableScissor(x, y, x + width, y + height);

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();

        poseStack.pushPose();

        poseStack.translate(
                x + width / 2.0D,
                y + height / 2.0D,
                201.0D
        );

        poseStack.scale(
                camera.zoom(),
                -camera.zoom(),
                camera.zoom()
        );

        poseStack.mulPose(Axis.XP.rotationDegrees(camera.pitch()));
        poseStack.mulPose(Axis.YP.rotationDegrees(camera.yaw()));

        poseStack.translate(
                -focus.x,
                -focus.y,
                -focus.z
        );

        final VertexConsumer consumer = buffer.getBuffer(renderType);

        for (final PreviewPortFace face : scene.portFaces()) {
            emitPortFaceFill(
                    poseStack,
                    consumer,
                    face,
                    face.fillColor()
            );

            emitPortFaceOutline(
                    poseStack,
                    consumer,
                    face,
                    face.equals(selectedFace) && face.equals(hoveredFace)
                            ? brightenOutline(face.outlineColor(), 2.1F)
                            : face.equals(selectedFace)
                            ? brightenOutline(face.outlineColor(), 1.45F)
                            : face.equals(hoveredFace)
                            ? brightenOutline(face.outlineColor(), 1.8F)
                            : face.outlineColor()
            );
        }

        buffer.endBatch(renderType);

        poseStack.popPose();

        RenderSystem.disableDepthTest();
        graphics.disableScissor();
    }

    /**
     * Projects one port face into screen-space.
     *
     * @param camera preview camera
     * @param face preview port face
     * @param centerX screen-space projection center x
     * @param centerY screen-space projection center y
     * @param outwardOffset outward face offset
     * @param inset face inset
     * @return projected face
     */
    private static ProjectedFace projectFace(
            final PortPreviewCamera camera,
            final PreviewPortFace face,
            final int centerX,
            final int centerY,
            final double outwardOffset,
            final double inset
    ) {
        final Vec3[] corners = faceCorners(
                face.previewPos(),
                face.previewFace(),
                outwardOffset,
                inset
        );

        final PortPreviewCamera.ProjectedPoint p0 = camera.project(corners[0], centerX, centerY);
        final PortPreviewCamera.ProjectedPoint p1 = camera.project(corners[1], centerX, centerY);
        final PortPreviewCamera.ProjectedPoint p2 = camera.project(corners[2], centerX, centerY);
        final PortPreviewCamera.ProjectedPoint p3 = camera.project(corners[3], centerX, centerY);

        return new ProjectedFace(
                p0,
                p1,
                p2,
                p3,
                (p0.depth() + p1.depth() + p2.depth() + p3.depth()) * 0.25D
        );
    }

    /**
     * Emits one projected translucent port fill quad.
     *
     * @param poseStack pose stack
     * @param consumer vertex consumer
     * @param face projected face
     * @param argb ARGB fill colour
     */
    private static void emitProjectedPortFill(
            final PoseStack poseStack,
            final VertexConsumer consumer,
            final ProjectedFace face,
            final int argb
    ) {
        if (((argb >> 24) & 255) <= 0) {
            return;
        }

        emitProjectedQuad(
                poseStack,
                consumer,
                face.p0(),
                face.p1(),
                face.p2(),
                face.p3(),
                argb
        );

        emitProjectedQuad(
                poseStack,
                consumer,
                face.p3(),
                face.p2(),
                face.p1(),
                face.p0(),
                argb
        );
    }

    /**
     * Emits a bold projected outline around a port face.
     *
     * @param poseStack pose stack
     * @param consumer vertex consumer
     * @param face projected face
     * @param argb ARGB outline colour
     */
    private static void emitProjectedPortOutline(
            final PoseStack poseStack,
            final VertexConsumer consumer,
            final ProjectedFace face,
            final int argb
    ) {
        final double thickness = 3.0D;

        emitProjectedLineQuad(poseStack, consumer, face.p0(), face.p1(), thickness, argb);
        emitProjectedLineQuad(poseStack, consumer, face.p1(), face.p2(), thickness, argb);
        emitProjectedLineQuad(poseStack, consumer, face.p2(), face.p3(), thickness, argb);
        emitProjectedLineQuad(poseStack, consumer, face.p3(), face.p0(), thickness, argb);
    }

    /**
     * Emits a projected screen-space quad.
     *
     * @param poseStack pose stack
     * @param consumer vertex consumer
     * @param a first corner
     * @param b second corner
     * @param c third corner
     * @param d fourth corner
     * @param argb ARGB colour
     */
    private static void emitProjectedQuad(
            final PoseStack poseStack,
            final VertexConsumer consumer,
            final PortPreviewCamera.ProjectedPoint a,
            final PortPreviewCamera.ProjectedPoint b,
            final PortPreviewCamera.ProjectedPoint c,
            final PortPreviewCamera.ProjectedPoint d,
            final int argb
    ) {
        emitProjectedVertex(poseStack, consumer, a, argb);
        emitProjectedVertex(poseStack, consumer, b, argb);
        emitProjectedVertex(poseStack, consumer, c, argb);
        emitProjectedVertex(poseStack, consumer, d, argb);
    }

    /**
     * Emits a thick screen-space line as a quad.
     *
     * @param poseStack pose stack
     * @param consumer vertex consumer
     * @param from first point
     * @param to second point
     * @param thickness line thickness in pixels
     * @param argb ARGB colour
     */
    private static void emitProjectedLineQuad(
            final PoseStack poseStack,
            final VertexConsumer consumer,
            final PortPreviewCamera.ProjectedPoint from,
            final PortPreviewCamera.ProjectedPoint to,
            final double thickness,
            final int argb
    ) {
        final double deltaX = to.x() - from.x();
        final double deltaY = to.y() - from.y();
        final double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (length < 0.0001D) {
            return;
        }

        final double normalX = -deltaY / length * thickness * 0.5D;
        final double normalY = deltaX / length * thickness * 0.5D;
        final double depth = Math.min(from.depth(), to.depth()) - 0.001D;

        emitProjectedVertex(
                poseStack,
                consumer,
                new PortPreviewCamera.ProjectedPoint(from.x() + normalX, from.y() + normalY, depth),
                argb
        );
        emitProjectedVertex(
                poseStack,
                consumer,
                new PortPreviewCamera.ProjectedPoint(to.x() + normalX, to.y() + normalY, depth),
                argb
        );
        emitProjectedVertex(
                poseStack,
                consumer,
                new PortPreviewCamera.ProjectedPoint(to.x() - normalX, to.y() - normalY, depth),
                argb
        );
        emitProjectedVertex(
                poseStack,
                consumer,
                new PortPreviewCamera.ProjectedPoint(from.x() - normalX, from.y() - normalY, depth),
                argb
        );
    }

    /**
     * Emits one projected screen-space vertex.
     *
     * @param poseStack pose stack
     * @param consumer vertex consumer
     * @param point projected point
     * @param argb ARGB colour
     */
    private static void emitProjectedVertex(
            final PoseStack poseStack,
            final VertexConsumer consumer,
            final PortPreviewCamera.ProjectedPoint point,
            final int argb
    ) {
        final PoseStack.Pose pose = poseStack.last();

        final int alpha = (argb >> 24) & 255;
        final int red = (argb >> 16) & 255;
        final int green = (argb >> 8) & 255;
        final int blue = argb & 255;

        consumer.addVertex(
                        pose,
                        (float) point.x(),
                        (float) point.y(),
                        0.0F
                )
                .setColor(red, green, blue, alpha)
                .setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 0.0F, 1.0F);
    }

    /**
     * Brightens an ARGB outline colour while preserving alpha.
     *
     * @param argb original colour
     * @param factor brightness multiplier
     * @return brightened ARGB colour
     */
    private static int brightenOutline(
            final int argb,
            final float factor
    ) {
        final int alpha = (argb >> 24) & 255;
        final int red = Math.min(255, Math.round(((argb >> 16) & 255) * factor));
        final int green = Math.min(255, Math.round(((argb >> 8) & 255) * factor));
        final int blue = Math.min(255, Math.round((argb & 255) * factor));

        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    /**
     * Emits the translucent filled overlay for one port face.
     *
     * @param poseStack current pose stack
     * @param consumer vertex consumer
     * @param face preview port face
     * @param argb overlay ARGB color
     */
    private static void emitPortFaceFill(
            final PoseStack poseStack,
            final VertexConsumer consumer,
            final PreviewPortFace face,
            final int argb
    ) {
        if (((argb >> 24) & 255) <= 0) {
            return;
        }

        final Vec3[] corners = faceCorners(
                face.previewPos(),
                face.previewFace(),
                0.018D,
                0.12D
        );

        emitQuad(
                poseStack,
                consumer,
                corners[0],
                corners[1],
                corners[2],
                corners[3],
                face.previewFace(),
                argb
        );
    }

    /**
     * Emits a bold filled-quad outline around one port face overlay.
     *
     * <p>This intentionally avoids {@link RenderType#lines()} because line buffers
     * are fragile inside GUI rendering. Each edge is instead drawn as a very thin
     * quad on the same face plane.</p>
     *
     * @param poseStack current pose stack
     * @param consumer vertex consumer
     * @param face preview port face
     * @param argb outline ARGB color
     */
    private static void emitPortFaceOutline(
            final PoseStack poseStack,
            final VertexConsumer consumer,
            final PreviewPortFace face,
            final int argb
    ) {
        final Vec3[] outer = faceCorners(face.previewPos(), face.previewFace(), 0.024D, 0.055D);
        final Vec3[] inner = faceCorners(face.previewPos(), face.previewFace(), 0.026D, 0.115D);

        emitQuad(poseStack, consumer, outer[0], outer[1], inner[1], inner[0], face.previewFace(), argb);
        emitQuad(poseStack, consumer, outer[1], outer[2], inner[2], inner[1], face.previewFace(), argb);
        emitQuad(poseStack, consumer, outer[2], outer[3], inner[3], inner[2], face.previewFace(), argb);
        emitQuad(poseStack, consumer, outer[3], outer[0], inner[0], inner[3], face.previewFace(), argb);
    }

    /**
     * Emits one coloured quad.
     *
     * @param poseStack current pose stack
     * @param consumer vertex consumer
     * @param a first vertex
     * @param b second vertex
     * @param c third vertex
     * @param d fourth vertex
     * @param face face normal direction
     * @param argb ARGB colour
     */
    private static void emitQuad(
            final PoseStack poseStack,
            final VertexConsumer consumer,
            final Vec3 a,
            final Vec3 b,
            final Vec3 c,
            final Vec3 d,
            final Direction face,
            final int argb
    ) {
        final PoseStack.Pose pose = poseStack.last();

        final int alpha = (argb >> 24) & 255;
        final int red = (argb >> 16) & 255;
        final int green = (argb >> 8) & 255;
        final int blue = argb & 255;

        final float normalX = face.getStepX();
        final float normalY = face.getStepY();
        final float normalZ = face.getStepZ();

        emitVertex(pose, consumer, a, red, green, blue, alpha, normalX, normalY, normalZ);
        emitVertex(pose, consumer, b, red, green, blue, alpha, normalX, normalY, normalZ);
        emitVertex(pose, consumer, c, red, green, blue, alpha, normalX, normalY, normalZ);
        emitVertex(pose, consumer, d, red, green, blue, alpha, normalX, normalY, normalZ);
    }

    /**
     * Emits one coloured vertex.
     *
     * @param pose current pose
     * @param consumer vertex consumer
     * @param vertex vertex position
     * @param red red channel
     * @param green green channel
     * @param blue blue channel
     * @param alpha alpha channel
     * @param normalX normal x
     * @param normalY normal y
     * @param normalZ normal z
     */
    private static void emitVertex(
            final PoseStack.Pose pose,
            final VertexConsumer consumer,
            final Vec3 vertex,
            final int red,
            final int green,
            final int blue,
            final int alpha,
            final float normalX,
            final float normalY,
            final float normalZ
    ) {
        consumer.addVertex(
                        pose,
                        (float) vertex.x,
                        (float) vertex.y,
                        (float) vertex.z
                )
                .setColor(red, green, blue, alpha)
                .setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(
                        pose,
                        normalX,
                        normalY,
                        normalZ
                );
    }

    /**
     * Builds the four corners of a cube face in preview-space.
     *
     * @param pos preview block position
     * @param face face direction
     * @param outwardOffset outward offset used to avoid z-fighting
     * @return four face corners
     */
    private static Vec3[] faceCorners(
            final BlockPos pos,
            final Direction face,
            final double outwardOffset
    ) {
        return faceCorners(
                pos,
                face,
                outwardOffset,
                0.08D
        );
    }

    /**
     * Builds the four corners of a cube face in preview-space.
     *
     * <p>The inset controls how close the rectangle gets to the block edge. Smaller
     * values make a larger rectangle; larger values make a smaller rectangle.</p>
     *
     * @param pos preview block position
     * @param face face direction
     * @param outwardOffset outward offset used to avoid z-fighting
     * @param inset inset from the block edge
     * @return four face corners
     */
    private static Vec3[] faceCorners(
            final BlockPos pos,
            final Direction face,
            final double outwardOffset,
            final double inset
    ) {
        final double min = inset;
        final double max = 1.0D - inset;

        final double x0 = pos.getX();
        final double y0 = pos.getY();
        final double z0 = pos.getZ();

        return switch (face) {
            case NORTH -> new Vec3[]{
                    new Vec3(x0 + max, y0 + min, z0 - outwardOffset),
                    new Vec3(x0 + min, y0 + min, z0 - outwardOffset),
                    new Vec3(x0 + min, y0 + max, z0 - outwardOffset),
                    new Vec3(x0 + max, y0 + max, z0 - outwardOffset)
            };
            case SOUTH -> new Vec3[]{
                    new Vec3(x0 + min, y0 + min, z0 + 1.0D + outwardOffset),
                    new Vec3(x0 + max, y0 + min, z0 + 1.0D + outwardOffset),
                    new Vec3(x0 + max, y0 + max, z0 + 1.0D + outwardOffset),
                    new Vec3(x0 + min, y0 + max, z0 + 1.0D + outwardOffset)
            };
            case WEST -> new Vec3[]{
                    new Vec3(x0 - outwardOffset, y0 + min, z0 + min),
                    new Vec3(x0 - outwardOffset, y0 + min, z0 + max),
                    new Vec3(x0 - outwardOffset, y0 + max, z0 + max),
                    new Vec3(x0 - outwardOffset, y0 + max, z0 + min)
            };
            case EAST -> new Vec3[]{
                    new Vec3(x0 + 1.0D + outwardOffset, y0 + min, z0 + max),
                    new Vec3(x0 + 1.0D + outwardOffset, y0 + min, z0 + min),
                    new Vec3(x0 + 1.0D + outwardOffset, y0 + max, z0 + min),
                    new Vec3(x0 + 1.0D + outwardOffset, y0 + max, z0 + max)
            };
            case DOWN -> new Vec3[]{
                    new Vec3(x0 + min, y0 - outwardOffset, z0 + max),
                    new Vec3(x0 + max, y0 - outwardOffset, z0 + max),
                    new Vec3(x0 + max, y0 - outwardOffset, z0 + min),
                    new Vec3(x0 + min, y0 - outwardOffset, z0 + min)
            };
            case UP -> new Vec3[]{
                    new Vec3(x0 + min, y0 + 1.0D + outwardOffset, z0 + min),
                    new Vec3(x0 + max, y0 + 1.0D + outwardOffset, z0 + min),
                    new Vec3(x0 + max, y0 + 1.0D + outwardOffset, z0 + max),
                    new Vec3(x0 + min, y0 + 1.0D + outwardOffset, z0 + max)
            };
        };
    }

    /**
     * Gets the center point of one cube face.
     *
     * @param pos cube position
     * @param face face direction
     * @return face center point
     */
    private static Vec3 faceCenter(
            final BlockPos pos,
            final Direction face
    ) {
        return new Vec3(
                pos.getX() + 0.5D + face.getStepX() * 0.51D,
                pos.getY() + 0.5D + face.getStepY() * 0.51D,
                pos.getZ() + 0.5D + face.getStepZ() * 0.51D
        );
    }

    /**
     * One projected port face.
     *
     * @param p0 first projected corner
     * @param p1 second projected corner
     * @param p2 third projected corner
     * @param p3 fourth projected corner
     * @param averageDepth average projected depth
     */
    private record ProjectedFace(
            PortPreviewCamera.ProjectedPoint p0,
            PortPreviewCamera.ProjectedPoint p1,
            PortPreviewCamera.ProjectedPoint p2,
            PortPreviewCamera.ProjectedPoint p3,
            double averageDepth
    ) {

    }

    /**
     * One port face paired with its projected position.
     *
     * @param face logical preview face
     * @param projected projected face
     */
    private record RenderedPortFace(
            PreviewPortFace face,
            ProjectedFace projected
    ) {

    }
}