package dev.galacticraft.machinelib.client.api.screen.port;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Small orbit camera used by the 3D port preview widget.
 */
public final class PortPreviewCamera {

    private float yaw = 35.0F;
    private float pitch = 25.0F;
    private float zoom = 22.0F;

    /**
     * Rotates the preview camera.
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
     * Zooms the preview camera.
     *
     * @param amount scroll amount
     */
    public void zoom(final double amount) {
        this.zoom += (float) amount * 2.0F;

        if (this.zoom < 8.0F) {
            this.zoom = 8.0F;
        } else if (this.zoom > 60.0F) {
            this.zoom = 60.0F;
        }
    }

    /**
     * Projects a 3D preview-space point into widget-space coordinates.
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
        final double yawRadians = Math.toRadians(this.yaw);
        final double pitchRadians = Math.toRadians(this.pitch);

        final double sinYaw = Math.sin(yawRadians);
        final double cosYaw = Math.cos(yawRadians);
        final double sinPitch = Math.sin(pitchRadians);
        final double cosPitch = Math.cos(pitchRadians);

        final double xYaw = point.x * cosYaw - point.z * sinYaw;
        final double zYaw = point.x * sinYaw + point.z * cosYaw;

        final double yPitch = point.y * cosPitch - zYaw * sinPitch;
        final double zPitch = point.y * sinPitch + zYaw * cosPitch;

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