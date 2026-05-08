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

package dev.galacticraft.machinelib.api.multiblock.port;

import net.minecraft.resources.ResourceLocation;

/**
 * Target inside a multiblock component that a port connects to.
 *
 * <p>The target can either represent one exact endpoint id, such as one item
 * slot or one redstone getter, or a group id, such as a group of input item
 * slots.</p>
 *
 * @param id target id
 * @param group {@code true} if this target represents a group id
 */
public record MultiblockPortTarget(
        ResourceLocation id,
        boolean group
) {

    /**
     * Creates an exact endpoint target.
     *
     * @param id endpoint id
     * @return endpoint target
     */
    public static MultiblockPortTarget id(final ResourceLocation id) {
        return new MultiblockPortTarget(
                id,
                false
        );
    }

    /**
     * Creates a grouped endpoint target.
     *
     * @param id group id
     * @return group target
     */
    public static MultiblockPortTarget group(final ResourceLocation id) {
        return new MultiblockPortTarget(
                id,
                true
        );
    }

}