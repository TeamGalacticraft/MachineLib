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

import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.UUID;

/**
 * Immutable client-side context describing one synced formed multiblock visual.
 *
 * @param instanceId unique formed multiblock instance id
 * @param definitionId registered multiblock definition id
 * @param origin world-space origin of the formed multiblock
 * @param orientation orientation used when the multiblock formed
 * @param partPositions immutable world-space positions occupied by the formed multiblock
 * @param patternWidth unrotated multiblock pattern width
 * @param patternHeight unrotated multiblock pattern height
 * @param patternDepth unrotated multiblock pattern depth
 */
public record MultiblockVisualContext(
        UUID instanceId,
        ResourceLocation definitionId,
        BlockPos origin,
        MultiblockOrientation orientation,
        List<BlockPos> partPositions,
        int patternWidth,
        int patternHeight,
        int patternDepth
) {

    /**
     * Creates an immutable visual context.
     */
    public MultiblockVisualContext {
        origin = origin.immutable();
        partPositions = List.copyOf(partPositions.stream()
                .map(BlockPos::immutable)
                .toList());

        if (patternWidth <= 0 || patternHeight <= 0 || patternDepth <= 0) {
            throw new IllegalArgumentException("Multiblock visual pattern dimensions must be positive.");
        }
    }

}