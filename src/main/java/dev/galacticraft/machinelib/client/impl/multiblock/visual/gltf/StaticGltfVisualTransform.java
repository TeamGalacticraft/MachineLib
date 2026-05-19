package dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf;

import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Placement transform for a static glTF multiblock visual.
 *
 * <p>The model origin is the position, in unrotated MachineLib pattern space,
 * where the glTF model's {@code 0,0,0} point should be placed. This avoids
 * ambiguity between discrete block-cell anchors and continuous model geometry.</p>
 *
 * @param modelOriginInPattern pattern-space location of the glTF model origin
 * @param modelForward pattern-local direction represented by model +Z
 * @param modelUp pattern-local direction represented by model +Y
 */
public record StaticGltfVisualTransform(
        Vec3 modelOriginInPattern,
        Direction modelForward,
        Direction modelUp
) {

    /**
     * Creates a static glTF visual transform.
     */
    public StaticGltfVisualTransform {
        if (modelForward.getAxis() == modelUp.getAxis()) {
            throw new IllegalArgumentException("Model forward and model up cannot share an axis.");
        }
    }

    /**
     * Creates the default origin transform.
     *
     * @return default transform
     */
    public static StaticGltfVisualTransform identity() {
        return new StaticGltfVisualTransform(
                Vec3.ZERO,
                Direction.SOUTH,
                Direction.UP
        );
    }

    /**
     * Converts one glTF model-space vertex into world space.
     *
     * @param modelPosition glTF model-space vertex
     * @param origin formed multiblock origin
     * @param orientation formed multiblock orientation
     * @param width unrotated pattern width
     * @param height unrotated pattern height
     * @param depth unrotated pattern depth
     * @return world-space vertex
     */
    public Vec3 modelPointToWorld(
            final Vector3f modelPosition,
            final BlockPos origin,
            final MultiblockOrientation orientation,
            final int width,
            final int height,
            final int depth
    ) {
        final Vec3 patternPoint = this.modelPointToPattern(modelPosition);

        return this.patternPointToWorld(
                patternPoint,
                origin,
                orientation,
                width,
                height,
                depth
        );
    }

    /**
     * Converts a model-space point into unrotated MachineLib pattern space.
     *
     * @param modelPosition model-space point
     * @return pattern-space point
     */
    private Vec3 modelPointToPattern(final Vector3f modelPosition) {
        final Direction modelRight = cross(
                this.modelUp,
                this.modelForward
        );

        final Vec3 right = directionVector(modelRight);
        final Vec3 up = directionVector(this.modelUp);
        final Vec3 forward = directionVector(this.modelForward);

        return new Vec3(
                this.modelOriginInPattern.x
                        + right.x * modelPosition.x()
                        + up.x * modelPosition.y()
                        + forward.x * modelPosition.z(),
                this.modelOriginInPattern.y
                        + right.y * modelPosition.x()
                        + up.y * modelPosition.y()
                        + forward.y * modelPosition.z(),
                this.modelOriginInPattern.z
                        + right.z * modelPosition.x()
                        + up.z * modelPosition.y()
                        + forward.z * modelPosition.z()
        );
    }

    /**
     * Converts an unrotated pattern-space point into world space.
     *
     * <p>This method uses continuous visual-geometry bounds, not block-cell bounds.
     * Block placement uses coordinates from {@code 0} to {@code size - 1}, but a
     * visual model occupies the full continuous space from {@code 0} to
     * {@code size}. Using geometry-space minimum compensation keeps visual rotation
     * stable across all horizontal and vertical orientations.</p>
     *
     * @param patternPoint pattern-space point
     * @param origin formed multiblock origin
     * @param orientation formed multiblock orientation
     * @param width unrotated pattern width
     * @param height unrotated pattern height
     * @param depth unrotated pattern depth
     * @return world-space point
     */
    private Vec3 patternPointToWorld(
            final Vec3 patternPoint,
            final BlockPos origin,
            final MultiblockOrientation orientation,
            final int width,
            final int height,
            final int depth
    ) {
        final Vec3 min = transformedGeometryMin(
                orientation,
                width,
                height,
                depth
        );

        final Vec3 right = directionVector(orientation.right());
        final Vec3 up = directionVector(orientation.up());
        final Vec3 forward = directionVector(orientation.forward());

        return new Vec3(
                origin.getX()
                        + right.x * patternPoint.x
                        + up.x * patternPoint.y
                        + forward.x * patternPoint.z
                        - min.x,
                origin.getY()
                        + right.y * patternPoint.x
                        + up.y * patternPoint.y
                        + forward.y * patternPoint.z
                        - min.y,
                origin.getZ()
                        + right.z * patternPoint.x
                        + up.z * patternPoint.y
                        + forward.z * patternPoint.z
                        - min.z
        );
    }

    /**
     * Calculates the transformed minimum corner of the continuous visual bounds.
     *
     * <p>This samples the full model/pattern extent from {@code 0} to
     * {@code width/height/depth}. This is different from the multiblock block
     * placement minimum, which samples only occupied block-cell coordinates from
     * {@code 0} to {@code size - 1}.</p>
     *
     * @param orientation formed multiblock orientation
     * @param width unrotated pattern width
     * @param height unrotated pattern height
     * @param depth unrotated pattern depth
     * @return transformed continuous geometry minimum
     */
    private static Vec3 transformedGeometryMin(
            final MultiblockOrientation orientation,
            final int width,
            final int height,
            final int depth
    ) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;

        for (int x = 0; x <= width; x += width) {
            for (int y = 0; y <= height; y += height) {
                for (int z = 0; z <= depth; z += depth) {
                    final Vec3 transformed = transformPatternVector(
                            orientation,
                            x,
                            y,
                            z
                    );

                    minX = Math.min(minX, transformed.x);
                    minY = Math.min(minY, transformed.y);
                    minZ = Math.min(minZ, transformed.z);
                }
            }
        }

        return new Vec3(
                minX,
                minY,
                minZ
        );
    }

    /**
     * Transforms a pattern-space vector into oriented space.
     *
     * @param orientation formed multiblock orientation
     * @param x pattern X
     * @param y pattern Y
     * @param z pattern Z
     * @return oriented vector
     */
    private static Vec3 transformPatternVector(
            final MultiblockOrientation orientation,
            final double x,
            final double y,
            final double z
    ) {
        final Vec3 right = directionVector(orientation.right());
        final Vec3 up = directionVector(orientation.up());
        final Vec3 forward = directionVector(orientation.forward());

        return new Vec3(
                right.x * x + up.x * y + forward.x * z,
                right.y * x + up.y * y + forward.y * z,
                right.z * x + up.z * y + forward.z * z
        );
    }

    /**
     * Converts a direction into a vector.
     *
     * @param direction direction
     * @return direction vector
     */
    private static Vec3 directionVector(final Direction direction) {
        return new Vec3(
                direction.getStepX(),
                direction.getStepY(),
                direction.getStepZ()
        );
    }

    /**
     * Calculates an axis-aligned cross product.
     *
     * @param a first direction
     * @param b second direction
     * @return crossed direction
     */
    private static Direction cross(
            final Direction a,
            final Direction b
    ) {
        final Vec3i av = a.getNormal();
        final Vec3i bv = b.getNormal();

        final int x = av.getY() * bv.getZ() - av.getZ() * bv.getY();
        final int y = av.getZ() * bv.getX() - av.getX() * bv.getZ();
        final int z = av.getX() * bv.getY() - av.getY() * bv.getX();

        for (final Direction direction : Direction.values()) {
            if (direction.getStepX() == x
                    && direction.getStepY() == y
                    && direction.getStepZ() == z) {
                return direction;
            }
        }

        throw new IllegalStateException("Invalid axis-aligned cross product.");
    }

}