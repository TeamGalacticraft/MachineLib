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
 *
 * <p>This intentionally renders simplified projected cubes instead of full baked
 * block models. It is designed as a stable first step before replacing the
 * internals with true block-model rendering and ray picking.</p>
 */
public final class PortPreviewRenderer {

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
                    centerX,
                    centerY
            );
        }
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

        graphics.hLine(
                bounds.minX(),
                bounds.maxX(),
                bounds.minY(),
                0xFF000000
        );
        graphics.hLine(
                bounds.minX(),
                bounds.maxX(),
                bounds.maxY(),
                0xFF000000
        );
        graphics.vLine(
                bounds.minX(),
                bounds.minY(),
                bounds.maxY(),
                0xFF000000
        );
        graphics.vLine(
                bounds.maxX(),
                bounds.minY(),
                bounds.maxY(),
                0xFF000000
        );
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
            final int centerX,
            final int centerY
    ) {
        final Vec3 point = faceCenter(
                face.previewPos(),
                face.previewFace()
        );

        final PortPreviewCamera.ProjectedPoint projected = camera.project(
                point,
                centerX,
                centerY
        );

        final int halfSize = face.configured() ? 5 : 4;
        final int color = face.configured()
                ? 0xCC37D65C
                : 0xCCB0B0B0;

        final int x = (int) Math.round(projected.x());
        final int y = (int) Math.round(projected.y());

        graphics.fill(
                x - halfSize,
                y - halfSize,
                x + halfSize,
                y + halfSize,
                color
        );

        graphics.hLine(
                x - halfSize,
                x + halfSize,
                y - halfSize,
                0xFF000000
        );
        graphics.hLine(
                x - halfSize,
                x + halfSize,
                y + halfSize,
                0xFF000000
        );
        graphics.vLine(
                x - halfSize,
                y - halfSize,
                y + halfSize,
                0xFF000000
        );
        graphics.vLine(
                x + halfSize,
                y - halfSize,
                y + halfSize,
                0xFF000000
        );
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

                    minX = Math.min(
                            minX,
                            (int) Math.round(point.x())
                    );
                    minY = Math.min(
                            minY,
                            (int) Math.round(point.y())
                    );
                    maxX = Math.max(
                            maxX,
                            (int) Math.round(point.x())
                    );
                    maxY = Math.max(
                            maxY,
                            (int) Math.round(point.y())
                    );
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