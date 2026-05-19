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

package dev.galacticraft.machinelib.api.multiblock.visual;

/**
 * Factory used to create a client-side visual for a formed multiblock.
 *
 * <p>Definitions store this factory, but the factory is only invoked on the
 * client when a formed multiblock is synced. The server never creates or renders
 * visual instances.</p>
 *
 * <p>Simple static visuals can return the same type of visual for every instance.
 * More advanced factories may inspect the context to choose a different model,
 * animation setup, or renderer depending on the formed machine's orientation,
 * size, or definition id.</p>
 */
@FunctionalInterface
public interface MultiblockVisualFactory {

    /**
     * Creates a new visual instance for one synced formed multiblock.
     *
     * <p>The returned visual is owned by the client visual manager until the
     * matching formed multiblock is removed. This method should return a new
     * independent object unless the visual is completely stateless.</p>
     *
     * @param context immutable client-side formed multiblock context
     * @return created visual, or {@code null} to render no visual
     */
    MultiblockVisual create(MultiblockVisualContext context);

}