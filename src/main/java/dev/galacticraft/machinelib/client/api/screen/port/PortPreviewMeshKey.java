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
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;
import java.util.Objects;

/**
 * Stable cache key for the static preview mesh.
 *
 * <p>The key includes preview position, block state, and whether the block is
 * primary. It intentionally excludes port configuration, because ports are
 * rendered as dynamic overlays instead of baked into the static structure mesh.</p>
 *
 * @param hash calculated hash
 */
public record PortPreviewMeshKey(
        int hash
) {

    /**
     * Creates a mesh key for the current preview block list.
     *
     * @param blocks preview blocks
     * @return mesh key
     */
    public static PortPreviewMeshKey create(final List<PreviewBlock> blocks) {
        int hash = 1;

        for (final PreviewBlock block : blocks) {
            final BlockPos pos = block.previewPos();

            hash = 31 * hash + pos.hashCode();
            hash = 31 * hash + BuiltInRegistries.BLOCK.getKey(block.state().getBlock()).hashCode();
            hash = 31 * hash + block.state().hashCode();
            hash = 31 * hash + Objects.hashCode(block.primary());
        }

        return new PortPreviewMeshKey(hash);
    }
}