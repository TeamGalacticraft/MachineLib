package dev.galacticraft.machinelib.client.api.screen.port;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;
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
     * Picks the nearest rendered port face under a screen point.
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
        double bestDistance = PICK_RADIUS * PICK_RADIUS;

        for (final PreviewPortFace face : scene.portFaces()) {
            final PortPreviewCamera.ProjectedPoint projected = camera.project(
                    faceCenter(
                            face.previewPos(),
                            face.previewFace()
                    ),
                    centerX,
                    centerY
            );

            final double deltaX = mouseX - projected.x();
            final double deltaY = mouseY - projected.y();
            final double distance = deltaX * deltaX + deltaY * deltaY;

            if (distance <= bestDistance) {
                bestDistance = distance;
                bestFace = face;
            }
        }

        return bestFace;
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
     * Renders projected port face overlays.
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
        final int centerX = x + width / 2;
        final int centerY = y + height / 2;

        for (final PreviewPortFace face : scene.portFaces()) {
            renderPortFace(
                    graphics,
                    camera,
                    face,
                    face.equals(hoveredFace),
                    face.equals(selectedFace),
                    centerX,
                    centerY
            );
        }
    }

    /**
     * Renders one selectable port face overlay.
     *
     * @param graphics GUI graphics
     * @param camera camera
     * @param face preview face
     * @param hovered hovered
     * @param selected selected
     * @param centerX widget center x
     * @param centerY widget center y
     */
    private static void renderPortFace(
            final GuiGraphics graphics,
            final PortPreviewCamera camera,
            final PreviewPortFace face,
            final boolean hovered,
            final boolean selected,
            final int centerX,
            final int centerY
    ) {
        final PortPreviewCamera.ProjectedPoint projected = camera.project(
                faceCenter(
                        face.previewPos(),
                        face.previewFace()
                ),
                centerX,
                centerY
        );

        final int halfSize = selected ? 6 : face.configured() ? 5 : 4;
        final int fillColor = face.configured()
                ? 0xCC37D65C
                : 0xCCB0B0B0;

        final int outlineColor = selected
                ? 0xFFFFD84D
                : hovered
                ? 0xFFFFFFFF
                : 0xFF000000;

        final int x = (int) Math.round(projected.x());
        final int y = (int) Math.round(projected.y());

        graphics.fill(
                x - halfSize,
                y - halfSize,
                x + halfSize,
                y + halfSize,
                fillColor
        );

        graphics.hLine(x - halfSize, x + halfSize, y - halfSize, outlineColor);
        graphics.hLine(x - halfSize, x + halfSize, y + halfSize, outlineColor);
        graphics.vLine(x - halfSize, y - halfSize, y + halfSize, outlineColor);
        graphics.vLine(x + halfSize, y - halfSize, y + halfSize, outlineColor);

        if (selected) {
            graphics.hLine(x - halfSize - 2, x + halfSize + 2, y - halfSize - 2, outlineColor);
            graphics.hLine(x - halfSize - 2, x + halfSize + 2, y + halfSize + 2, outlineColor);
            graphics.vLine(x - halfSize - 2, y - halfSize - 2, y + halfSize + 2, outlineColor);
            graphics.vLine(x + halfSize + 2, y - halfSize - 2, y + halfSize + 2, outlineColor);
        }
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
}