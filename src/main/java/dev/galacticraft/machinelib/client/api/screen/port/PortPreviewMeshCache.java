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

import java.util.List;

/**
 * Small per-widget cache for static preview geometry.
 *
 * <p>The cache belongs to the widget/renderer rather than the scene because the
 * scene should remain logical data. If the scene's block list changes, the mesh
 * key changes and the cache rebuilds automatically.</p>
 */
public final class PortPreviewMeshCache {

    private PortPreviewMesh mesh;

    /**
     * Gets a cached mesh for the current scene.
     *
     * <p>Only primary blocks are included in the cached static mesh. Adjacent
     * neighbour blocks are deliberately excluded because they are context objects
     * such as pipes, inventories, cables, and nearby solid blocks. Those may change
     * independently from the formed machine and should not invalidate the whole
     * static structure mesh.</p>
     *
     * @param scene preview scene
     * @return cached mesh
     */
    public PortPreviewMesh get(final PortPreviewScene scene) {
        final List<PreviewBlock> blocks = scene.blocks()
                .stream()
                .filter(PreviewBlock::primary)
                .toList();

        final PortPreviewMeshKey key = PortPreviewMeshKey.create(blocks);

        if (this.mesh != null && this.mesh.key().equals(key)) {
            return this.mesh;
        }

        this.mesh = PortPreviewMeshBuilder.build(
                key,
                blocks
        );

        return this.mesh;
    }

    /**
     * Clears the cached mesh.
     */
    public void clear() {
        this.mesh = null;
    }
}