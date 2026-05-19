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
 * Base interface for a client-side visual attached to a formed multiblock.
 *
 * <p>This interface intentionally contains no Minecraft client rendering types.
 * That keeps the public multiblock definition API safe to reference from common
 * code and dedicated server code. Client-only renderable visuals should also
 * implement the client-side visual interface used by MachineLib's client render
 * manager.</p>
 *
 * <p>A visual is created when the client receives a formed multiblock sync packet
 * and destroyed when the client receives the corresponding remove packet or when
 * all client multiblock state is cleared. Visuals are runtime-only and are never
 * saved to disk.</p>
 */
public interface MultiblockVisual {

    /**
     * Called once per client tick while the visual is alive.
     *
     * <p>Static visuals may leave this method empty. Animated visuals can use this
     * to advance timelines, update cached transforms, invalidate dynamic meshes, or
     * prepare render state for the next frame.</p>
     *
     * @param context immutable context describing the synced formed multiblock
     */
    default void tick(final MultiblockVisualContext context) {

    }

    /**
     * Called when the visual is removed from the client visual manager.
     *
     * <p>Implementations should release any client-side resources they own here.
     * Static debug visuals usually do not need to do anything. Future model,
     * texture, buffer, or Glasswork-backed visuals may use this to unregister
     * scenes, release buffers, or clear cached render data.</p>
     *
     * @param context immutable context describing the synced formed multiblock
     */
    default void close(final MultiblockVisualContext context) {

    }

}