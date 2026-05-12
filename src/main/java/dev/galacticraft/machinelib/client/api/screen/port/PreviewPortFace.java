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

import java.util.List;

/**
 * One selectable face inside a 3D port preview scene.
 *
 * @param previewPos preview-space block position
 * @param previewFace preview-space face direction
 * @param label readable face label
 * @param configured whether this face currently has a configured port
 * @param typeName current port type display text, or {@code null}
 * @param modeName current port mode/flow display text, or {@code null}
 * @param targetName current target display text, or {@code null}
 * @param fillColor ARGB translucent fill colour
 * @param outlineColor ARGB outline colour
 * @param detailLines cached detail lines shown when this face is selected
 */
public record PreviewPortFace(
        BlockPos previewPos,
        Direction previewFace,
        Component label,
        boolean configured,
        String typeName,
        String modeName,
        String targetName,
        int fillColor,
        int outlineColor,
        List<Component> detailLines
) {

    /**
     * Normalizes immutable values.
     */
    public PreviewPortFace {
        previewPos = previewPos.immutable();
        detailLines = List.copyOf(detailLines);
    }

    /**
     * Creates an unconfigured preview face.
     *
     * @param previewPos preview-space block position
     * @param previewFace preview-space face direction
     * @param label readable face label
     * @return unconfigured face
     */
    public static PreviewPortFace none(
            final BlockPos previewPos,
            final Direction previewFace,
            final Component label
    ) {
        return new PreviewPortFace(
                previewPos,
                previewFace,
                label,
                false,
                null,
                null,
                null,
                0x00000000,
                0xFF9A9A9A,
                List.of(
                        label,
                        Component.literal("Port: none")
                )
        );
    }
}