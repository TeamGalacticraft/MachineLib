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

package dev.galacticraft.machinelib.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

import java.util.EnumSet;

public enum MultiblockOrientation {
    NORTH_UP(Direction.NORTH, Direction.UP),
    NORTH_DOWN(Direction.NORTH, Direction.DOWN),
    NORTH_EAST(Direction.NORTH, Direction.EAST),
    NORTH_WEST(Direction.NORTH, Direction.WEST),

    SOUTH_UP(Direction.SOUTH, Direction.UP),
    SOUTH_DOWN(Direction.SOUTH, Direction.DOWN),
    SOUTH_EAST(Direction.SOUTH, Direction.EAST),
    SOUTH_WEST(Direction.SOUTH, Direction.WEST),

    EAST_UP(Direction.EAST, Direction.UP),
    EAST_DOWN(Direction.EAST, Direction.DOWN),
    EAST_NORTH(Direction.EAST, Direction.NORTH),
    EAST_SOUTH(Direction.EAST, Direction.SOUTH),

    WEST_UP(Direction.WEST, Direction.UP),
    WEST_DOWN(Direction.WEST, Direction.DOWN),
    WEST_NORTH(Direction.WEST, Direction.NORTH),
    WEST_SOUTH(Direction.WEST, Direction.SOUTH),

    UP_NORTH(Direction.UP, Direction.NORTH),
    UP_SOUTH(Direction.UP, Direction.SOUTH),
    UP_EAST(Direction.UP, Direction.EAST),
    UP_WEST(Direction.UP, Direction.WEST),

    DOWN_NORTH(Direction.DOWN, Direction.NORTH),
    DOWN_SOUTH(Direction.DOWN, Direction.SOUTH),
    DOWN_EAST(Direction.DOWN, Direction.EAST),
    DOWN_WEST(Direction.DOWN, Direction.WEST);

    private final Direction forward;
    private final Direction up;
    private final Direction right;

    MultiblockOrientation(
            final Direction forward,
            final Direction up
    ) {
        if (forward.getAxis() == up.getAxis()) {
            throw new IllegalArgumentException("Forward and up cannot share an axis");
        }

        this.forward = forward;
        this.up = up;
        this.right = cross(up, forward);
    }

    public Direction forward() {
        return this.forward;
    }

    public Direction up() {
        return this.up;
    }

    public Direction right() {
        return this.right;
    }

    public Vec3i transformVector(
            final int x,
            final int y,
            final int z
    ) {
        final Vec3i rightNormal = this.right.getNormal();
        final Vec3i upNormal = this.up.getNormal();
        final Vec3i forwardNormal = this.forward.getNormal();

        return new Vec3i(
                rightNormal.getX() * x + upNormal.getX() * y + forwardNormal.getX() * z,
                rightNormal.getY() * x + upNormal.getY() * y + forwardNormal.getY() * z,
                rightNormal.getZ() * x + upNormal.getZ() * y + forwardNormal.getZ() * z
        );
    }



    public BlockPos transformRelative(
            final int x,
            final int y,
            final int z,
            final int width,
            final int height,
            final int depth
    ) {
        final Vec3i transformed = this.transformVector(x, y, z);
        final Vec3i min = this.transformedMin(width, height, depth);

        return new BlockPos(
                transformed.getX() - min.getX(),
                transformed.getY() - min.getY(),
                transformed.getZ() - min.getZ()
        );
    }

    public BlockPos transformRelative(
            final BlockPos relative,
            final int width,
            final int height,
            final int depth
    ) {
        return this.transformRelative(
                relative.getX(),
                relative.getY(),
                relative.getZ(),
                width,
                height,
                depth
        );
    }

    public Vec3i transformedMin(
            final int width,
            final int height,
            final int depth
    ) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;

        for (int x = 0; x <= width - 1; x += Math.max(1, width - 1)) {
            for (int y = 0; y <= height - 1; y += Math.max(1, height - 1)) {
                for (int z = 0; z <= depth - 1; z += Math.max(1, depth - 1)) {
                    final Vec3i transformed = this.transformVector(x, y, z);

                    minX = Math.min(minX, transformed.getX());
                    minY = Math.min(minY, transformed.getY());
                    minZ = Math.min(minZ, transformed.getZ());
                }
            }
        }

        return new Vec3i(minX, minY, minZ);
    }

    public Direction transformDirection(final Direction direction) {
        return switch (direction) {
            case EAST -> this.right;
            case WEST -> this.right.getOpposite();
            case UP -> this.up;
            case DOWN -> this.up.getOpposite();
            case SOUTH -> this.forward;
            case NORTH -> this.forward.getOpposite();
        };
    }

    public static EnumSet<MultiblockOrientation> all() {
        return EnumSet.allOf(MultiblockOrientation.class);
    }

    public static EnumSet<MultiblockOrientation> horizontal() {
        return EnumSet.of(
                NORTH_UP,
                SOUTH_UP,
                EAST_UP,
                WEST_UP
        );
    }

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
            final Vec3i normal = direction.getNormal();

            if (normal.getX() == x && normal.getY() == y && normal.getZ() == z) {
                return direction;
            }
        }

        throw new IllegalStateException("Invalid direction cross product");
    }
}