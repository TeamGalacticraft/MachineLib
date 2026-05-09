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

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * One cached baked-model quad translated into preview-space.
 *
 * <p>This stores only the geometry needed by the port preview renderer. The
 * original Minecraft baked block model is read once when the preview mesh is
 * rebuilt, then each frame only applies camera transforms through the active
 * {@link com.mojang.blaze3d.vertex.PoseStack}.</p>
 *
 * @param vertices quad vertices in preview-space
 * @param u texture U coordinates for each vertex
 * @param v texture V coordinates for each vertex
 * @param direction baked quad direction
 * @param tintIndex baked quad tint index
 * @param shaded whether the original baked quad uses shade
 */
public record PortPreviewQuad(
        Vec3[] vertices,
        float[] u,
        float[] v,
        Direction direction,
        int tintIndex,
        boolean shaded
) {

    /**
     * Creates a cached preview quad from a Minecraft baked quad.
     *
     * <p>The baked quad vertex data stores block-local coordinates. The supplied
     * offsets translate those coordinates into preview-space so the renderer does
     * not need to know which block originally created the quad.</p>
     *
     * @param quad baked quad
     * @param offsetX preview-space x offset
     * @param offsetY preview-space y offset
     * @param offsetZ preview-space z offset
     * @return cached preview quad
     */
    public static PortPreviewQuad fromBakedQuad(
            final BakedQuad quad,
            final int offsetX,
            final int offsetY,
            final int offsetZ
    ) {
        final int[] data = quad.getVertices();
        final Vec3[] vertices = new Vec3[4];
        final float[] u = new float[4];
        final float[] v = new float[4];

        for (int vertex = 0; vertex < 4; vertex++) {
            final int base = vertex * 8;

            vertices[vertex] = new Vec3(
                    Float.intBitsToFloat(data[base]) + offsetX,
                    Float.intBitsToFloat(data[base + 1]) + offsetY,
                    Float.intBitsToFloat(data[base + 2]) + offsetZ
            );

            u[vertex] = Float.intBitsToFloat(data[base + 4]);
            v[vertex] = Float.intBitsToFloat(data[base + 5]);
        }

        return new PortPreviewQuad(
                vertices,
                u,
                v,
                quad.getDirection(),
                quad.getTintIndex(),
                quad.isShade()
        );
    }
}