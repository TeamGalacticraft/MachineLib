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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a cached preview mesh from client-side baked block models.
 */
public final class PortPreviewMeshBuilder {

    private PortPreviewMeshBuilder() {

    }

    /**
     * Builds a static preview mesh from the scene's current block list.
     *
     * <p>This method reads each block state's client baked model and copies its
     * baked quads into preview-space. The resulting mesh can then be rendered
     * repeatedly without asking the block renderer to rebuild model quads every
     * frame.</p>
     *
     * @param key cache key for the block list
     * @param blocks preview blocks
     * @return cached preview mesh
     */
    public static PortPreviewMesh build(
            final PortPreviewMeshKey key,
            final List<PreviewBlock> blocks
    ) {
        final Minecraft minecraft = Minecraft.getInstance();
        final BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
        final List<PortPreviewQuad> quads = new ArrayList<>();

        for (final PreviewBlock block : blocks) {
            if (block.state().isAir()) {
                continue;
            }

            final BakedModel model = dispatcher.getBlockModel(block.state());

            addQuads(
                    quads,
                    model,
                    block,
                    null
            );

            for (final Direction direction : Direction.values()) {
                addQuads(
                        quads,
                        model,
                        block,
                        direction
                );
            }
        }

        return new PortPreviewMesh(
                key,
                List.copyOf(quads)
        );
    }

    /**
     * Adds baked quads for one cull direction.
     *
     * @param output output quad list
     * @param model baked model
     * @param block preview block
     * @param direction cull direction, or {@code null} for uncullable quads
     */
    private static void addQuads(
            final List<PortPreviewQuad> output,
            final BakedModel model,
            final PreviewBlock block,
            final Direction direction
    ) {
        final RandomSource random = RandomSource.create(42L);

        model.getQuads(
                block.state(),
                direction,
                random
        ).forEach(quad -> output.add(PortPreviewQuad.fromBakedQuad(
                quad,
                block.previewPos().getX(),
                block.previewPos().getY(),
                block.previewPos().getZ()
        )));
    }
}