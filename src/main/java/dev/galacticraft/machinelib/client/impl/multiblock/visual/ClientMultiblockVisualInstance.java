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

package dev.galacticraft.machinelib.client.impl.multiblock.visual;

import dev.galacticraft.machinelib.api.multiblock.visual.MultiblockVisual;
import dev.galacticraft.machinelib.api.multiblock.visual.MultiblockVisualContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

/**
 * Runtime client-side holder for one formed multiblock visual.
 *
 * <p>This object binds the immutable synced visual context, the created visual
 * implementation, and a precomputed world-space bounding box. The manager stores
 * instances of this class by formed multiblock id.</p>
 */
public final class ClientMultiblockVisualInstance {

    private final MultiblockVisualContext context;
    private final MultiblockVisual visual;
    private final AABB bounds;

    /**
     * Creates a visual instance wrapper.
     *
     * @param context immutable formed multiblock visual context
     * @param visual created visual implementation
     */
    public ClientMultiblockVisualInstance(
            final MultiblockVisualContext context,
            final MultiblockVisual visual
    ) {
        this.context = context;
        this.visual = visual;
        this.bounds = createBounds(context);
    }

    /**
     * Gets the immutable synced context for this visual.
     *
     * @return visual context
     */
    public MultiblockVisualContext context() {
        return this.context;
    }

    /**
     * Gets the visual implementation.
     *
     * @return visual implementation
     */
    public MultiblockVisual visual() {
        return this.visual;
    }

    /**
     * Gets the world-space bounds occupied by the formed multiblock parts.
     *
     * @return world-space visual bounds
     */
    public AABB bounds() {
        return this.bounds;
    }

    /**
     * Ticks the wrapped visual.
     */
    public void tick() {
        this.visual.tick(this.context);
    }

    /**
     * Closes the wrapped visual.
     */
    public void close() {
        this.visual.close(this.context);
    }

    /**
     * Builds a world-space bounding box around all synced multiblock part
     * positions.
     *
     * @param context visual context
     * @return world-space bounds
     */
    private static AABB createBounds(final MultiblockVisualContext context) {
        if (context.partPositions().isEmpty()) {
            final BlockPos origin = context.origin();
            return new AABB(origin);
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (final BlockPos position : context.partPositions()) {
            minX = Math.min(minX, position.getX());
            minY = Math.min(minY, position.getY());
            minZ = Math.min(minZ, position.getZ());
            maxX = Math.max(maxX, position.getX() + 1);
            maxY = Math.max(maxY, position.getY() + 1);
            maxZ = Math.max(maxZ, position.getZ() + 1);
        }

        return new AABB(
                minX,
                minY,
                minZ,
                maxX,
                maxY,
                maxZ
        );
    }

}