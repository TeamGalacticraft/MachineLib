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

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Bounds-aware orbit camera used by the 3D port preview widget.
 */
public final class PortPreviewCamera {

    private float yaw = 35.0F;
    private float pitch = 25.0F;
    private float zoom = 18.0F;

    private Vec3 focus = new Vec3(0.5D, 0.5D, 0.5D);
    private PortPreviewBounds bounds = PortPreviewBounds.singleBlock();

    /**
     * Applies scene bounds and clamps the current focus/zoom to them.
     *
     * @param bounds scene bounds
     * @param viewWidth view width
     * @param viewHeight view height
     */
    public void applyBounds(
            final PortPreviewBounds bounds,
            final int viewWidth,
            final int viewHeight
    ) {
        this.bounds = bounds;
        this.focus = bounds.clamp(this.focus);

        final double largest = Math.max(1.0D, bounds.largestDimension());
        final float minZoom = this.minimumZoom(viewWidth, viewHeight, largest);
        final float maxZoom = this.maximumZoom(viewWidth, viewHeight);

        if (this.zoom < minZoom) {
            this.zoom = minZoom;
        }

        if (this.zoom > maxZoom) {
            this.zoom = maxZoom;
        }
    }

    /**
     * Centers the camera focus on the active bounds.
     */
    public void centerOnBounds() {
        this.focus = this.bounds.center();
    }

    /**
     * Rotates the preview camera around the current focus point.
     *
     * @param deltaX mouse delta x
     * @param deltaY mouse delta y
     */
    public void rotate(
            final double deltaX,
            final double deltaY
    ) {
        this.yaw += (float) deltaX * 0.7F;
        this.pitch += (float) deltaY * 0.7F;

        if (this.pitch < -80.0F) {
            this.pitch = -80.0F;
        } else if (this.pitch > 80.0F) {
            this.pitch = 80.0F;
        }
    }

    /**
     * Moves the camera focus point in preview-space.
     *
     * @param deltaX mouse delta x
     * @param deltaY mouse delta y
     */
    public void moveFocus(
            final double deltaX,
            final double deltaY
    ) {
        final double scale = 1.0D / Math.max(1.0D, this.zoom);

        final double yawRadians = Math.toRadians(this.yaw);
        final double cosYaw = Math.cos(yawRadians);
        final double sinYaw = Math.sin(yawRadians);

        final double worldRightX = cosYaw;
        final double worldRightZ = sinYaw;
        final double worldUpY = 1.0D;

        final double moveX = -deltaX * scale * 0.75D;
        final double moveY = deltaY * scale * 0.75D;

        this.focus = this.bounds.clamp(this.focus.add(
                worldRightX * moveX,
                worldUpY * moveY,
                worldRightZ * moveX
        ));
    }

    /**
     * Zooms the preview camera.
     *
     * @param amount scroll amount
     * @param viewWidth view width
     * @param viewHeight view height
     */
    public void zoom(
            final double amount,
            final int viewWidth,
            final int viewHeight
    ) {
        final double largest = Math.max(1.0D, this.bounds.largestDimension());
        final float minZoom = this.minimumZoom(viewWidth, viewHeight, largest);
        final float maxZoom = this.maximumZoom(viewWidth, viewHeight);

        this.zoom += (float) amount * 2.0F;

        if (this.zoom < minZoom) {
            this.zoom = minZoom;
        } else if (this.zoom > maxZoom) {
            this.zoom = maxZoom;
        }
    }

    /**
     * Projects a 3D preview-space point into widget-space coordinates.
     *
     * <p>This mirrors the preview mesh render transform:</p>
     *
     * <pre>
     * translate(center)
     * scale(zoom, -zoom, zoom)
     * rotateX(pitch)
     * rotateY(yaw)
     * translate(-focus)
     * </pre>
     *
     * <p>The previous version fixed one axis but applied pitch/yaw in the wrong
     * order for the actual mesh transform, causing the overlay to drift during
     * horizontal rotation.</p>
     *
     * @param point 3D point
     * @param centerX widget center x
     * @param centerY widget center y
     * @return projected point
     */
    public ProjectedPoint project(
            final Vec3 point,
            final int centerX,
            final int centerY
    ) {
        final Vec3 relative = point.subtract(this.focus);

        final double yawRadians = Math.toRadians(this.yaw);
        final double pitchRadians = Math.toRadians(this.pitch);

        final double sinYaw = Math.sin(yawRadians);
        final double cosYaw = Math.cos(yawRadians);
        final double sinPitch = Math.sin(pitchRadians);
        final double cosPitch = Math.cos(pitchRadians);

        final double xYaw = relative.x * cosYaw + relative.z * sinYaw;
        final double zYaw = -relative.x * sinYaw + relative.z * cosYaw;

        final double yPitch = relative.y * cosPitch - zYaw * sinPitch;
        final double zPitch = relative.y * sinPitch + zYaw * cosPitch;

        return new ProjectedPoint(
                centerX + xYaw * this.zoom,
                centerY - yPitch * this.zoom,
                zPitch
        );
    }

    /**
     * Converts a block position into its cube center.
     *
     * @param pos preview block position
     * @return cube center
     */
    public static Vec3 centerOf(final BlockPos pos) {
        return new Vec3(
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D
        );
    }

    /**
     * Calculates the minimum zoom level so the largest structure dimension still
     * fits inside the preview.
     *
     * @param viewWidth view width
     * @param viewHeight view height
     * @param largest largest scene dimension
     * @return minimum zoom
     */
    private float minimumZoom(
            final int viewWidth,
            final int viewHeight,
            final double largest
    ) {
        return (float) Math.max(
                1.0D,
                Math.min(viewWidth, viewHeight) / Math.max(1.0D, largest * 1.35D)
        );
    }

    public float yaw() {
        return this.yaw;
    }

    public float pitch() {
        return this.pitch;
    }

    public float zoom() {
        return this.zoom;
    }

    public Vec3 focus() {
        return this.focus;
    }

    /**
     * Calculates the maximum zoom level.
     *
     * @param viewWidth view width
     * @param viewHeight view height
     * @return maximum zoom
     */
    private float maximumZoom(
            final int viewWidth,
            final int viewHeight
    ) {
        return Math.max(
                24.0F,
                Math.min(viewWidth, viewHeight) * 0.75F
        );
    }

    /**
     * One projected point.
     *
     * @param x screen x
     * @param y screen y
     * @param depth projected depth
     */
    public record ProjectedPoint(
            double x,
            double y,
            double depth
    ) {

    }
}