package dev.galacticraft.machinelib.client.api.screen.port;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Simple software renderer for the temporary 3D port preview.
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
     * @param x widget x
     * @param y widget y
     * @param width widget width
     * @param height widget height
     */
    public static void render(
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
        graphics.fill(
                x,
                y,
                x + width,
                y + height,
                0xCC101010
        );

        final int centerX = x + width / 2;
        final int centerY = y + height / 2;

        final List<RenderCube> cubes = new ArrayList<>();

        for (final PreviewBlock block : scene.blocks()) {
            cubes.add(new RenderCube(
                    block,
                    camera.project(
                            PortPreviewCamera.centerOf(block.previewPos()),
                            centerX,
                            centerY
                    ).depth()
            ));
        }

        cubes.sort(Comparator.comparingDouble(RenderCube::depth).reversed());

        for (final RenderCube cube : cubes) {
            renderCube(
                    graphics,
                    camera,
                    cube.block(),
                    centerX,
                    centerY
            );
        }

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
     * Renders one simplified cube.
     *
     * @param graphics GUI graphics
     * @param camera camera
     * @param block preview block
     * @param centerX widget center x
     * @param centerY widget center y
     */
    private static void renderCube(
            final GuiGraphics graphics,
            final PortPreviewCamera camera,
            final PreviewBlock block,
            final int centerX,
            final int centerY
    ) {
        final Bounds bounds = boundsFor(
                camera,
                block.previewPos(),
                centerX,
                centerY
        );

        final int color = block.primary()
                ? 0xAA707070
                : 0x664A6D8A;

        graphics.fill(
                bounds.minX(),
                bounds.minY(),
                bounds.maxX(),
                bounds.maxY(),
                color
        );

        graphics.hLine(bounds.minX(), bounds.maxX(), bounds.minY(), 0xFF000000);
        graphics.hLine(bounds.minX(), bounds.maxX(), bounds.maxY(), 0xFF000000);
        graphics.vLine(bounds.minX(), bounds.minY(), bounds.maxY(), 0xFF000000);
        graphics.vLine(bounds.maxX(), bounds.minY(), bounds.maxY(), 0xFF000000);
    }

    /**
     * Renders one selectable port face overlay.
     *
     * @param graphics GUI graphics
     * @param camera camera
     * @param face preview face
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
     * Gets the projected bounds for one cube.
     *
     * @param camera camera
     * @param pos cube position
     * @param centerX widget center x
     * @param centerY widget center y
     * @return projected bounds
     */
    private static Bounds boundsFor(
            final PortPreviewCamera camera,
            final BlockPos pos,
            final int centerX,
            final int centerY
    ) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (int x = 0; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
                for (int z = 0; z <= 1; z++) {
                    final PortPreviewCamera.ProjectedPoint point = camera.project(
                            new Vec3(
                                    pos.getX() + x,
                                    pos.getY() + y,
                                    pos.getZ() + z
                            ),
                            centerX,
                            centerY
                    );

                    minX = Math.min(minX, (int) Math.round(point.x()));
                    minY = Math.min(minY, (int) Math.round(point.y()));
                    maxX = Math.max(maxX, (int) Math.round(point.x()));
                    maxY = Math.max(maxY, (int) Math.round(point.y()));
                }
            }
        }

        return new Bounds(
                minX,
                minY,
                maxX,
                maxY
        );
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

    private record RenderCube(
            PreviewBlock block,
            double depth
    ) {

    }

    private record Bounds(
            int minX,
            int minY,
            int maxX,
            int maxY
    ) {

    }
}