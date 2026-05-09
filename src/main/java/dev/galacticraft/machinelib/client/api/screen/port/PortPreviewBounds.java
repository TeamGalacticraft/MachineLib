package dev.galacticraft.machinelib.client.api.screen.port;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Clamped preview-space bounds for a port preview scene.
 *
 * <p>The bounds describe the real configurable machine shape, not necessarily
 * every extra neighbour block rendered for context. Camera focus movement is
 * clamped to these bounds.</p>
 *
 * @param minX minimum x bound
 * @param minY minimum y bound
 * @param minZ minimum z bound
 * @param maxX maximum x bound
 * @param maxY maximum y bound
 * @param maxZ maximum z bound
 */
public record PortPreviewBounds(
        double minX,
        double minY,
        double minZ,
        double maxX,
        double maxY,
        double maxZ
) {

    /**
     * Creates default one-block bounds.
     *
     * @return one-block bounds
     */
    public static PortPreviewBounds singleBlock() {
        return new PortPreviewBounds(
                0.0D,
                0.0D,
                0.0D,
                1.0D,
                1.0D,
                1.0D
        );
    }

    /**
     * Creates bounds around all primary preview blocks.
     *
     * @param blocks preview blocks
     * @return calculated bounds
     */
    public static PortPreviewBounds fromPrimaryBlocks(final List<PreviewBlock> blocks) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (final PreviewBlock block : blocks) {
            if (!block.primary()) {
                continue;
            }

            final BlockPos pos = block.previewPos();

            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX() + 1);
            maxY = Math.max(maxY, pos.getY() + 1);
            maxZ = Math.max(maxZ, pos.getZ() + 1);
        }

        if (minX == Integer.MAX_VALUE) {
            return singleBlock();
        }

        return new PortPreviewBounds(
                minX,
                minY,
                minZ,
                maxX,
                maxY,
                maxZ
        );
    }

    /**
     * Gets the center of the bounds.
     *
     * @return center point
     */
    public Vec3 center() {
        return new Vec3(
                (this.minX + this.maxX) * 0.5D,
                (this.minY + this.maxY) * 0.5D,
                (this.minZ + this.maxZ) * 0.5D
        );
    }

    /**
     * Gets the largest bound dimension.
     *
     * @return largest dimension
     */
    public double largestDimension() {
        return Math.max(
                this.maxX - this.minX,
                Math.max(
                        this.maxY - this.minY,
                        this.maxZ - this.minZ
                )
        );
    }

    /**
     * Clamps a focus point to stay inside the bounds.
     *
     * @param point unclamped point
     * @return clamped point
     */
    public Vec3 clamp(final Vec3 point) {
        return new Vec3(
                clamp(point.x, this.minX, this.maxX),
                clamp(point.y, this.minY, this.maxY),
                clamp(point.z, this.minZ, this.maxZ)
        );
    }

    /**
     * Clamps a value.
     *
     * @param value value
     * @param min minimum
     * @param max maximum
     * @return clamped value
     */
    private static double clamp(
            final double value,
            final double min,
            final double max
    ) {
        return Math.max(
                min,
                Math.min(max, value)
        );
    }
}