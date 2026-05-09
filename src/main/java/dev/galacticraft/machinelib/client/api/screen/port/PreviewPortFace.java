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
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

/**
 * One selectable face inside a 3D port preview scene.
 *
 * @param previewPos preview-space block position
 * @param previewFace preview-space face direction
 * @param label readable label
 * @param configured whether this face currently has a configured port
 * @param fillColor ARGB translucent fill colour
 * @param outlineColor ARGB outline colour
 */
public record PreviewPortFace(
        BlockPos previewPos,
        Direction previewFace,
        Component label,
        boolean configured,
        int fillColor,
        int outlineColor
) {

    public PreviewPortFace {
        previewPos = previewPos.immutable();
    }

    /**
     * Creates a fallback unconfigured preview face.
     *
     * @param previewPos preview-space block position
     * @param previewFace preview-space face direction
     * @param label readable label
     * @param configured whether this face currently has a configured port
     */
    public PreviewPortFace(
            final BlockPos previewPos,
            final Direction previewFace,
            final Component label,
            final boolean configured
    ) {
        this(
                previewPos,
                previewFace,
                label,
                configured,
                configured ? 0x6637D65C : 0x00000000,
                configured ? 0xFF37D65C : 0xFF9A9A9A
        );
    }
}