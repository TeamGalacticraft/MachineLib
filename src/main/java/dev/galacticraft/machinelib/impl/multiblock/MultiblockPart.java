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

package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import dev.galacticraft.machinelib.api.multiblock.MultiblockSlotPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record MultiblockPart(BlockPos worldPos, BlockPos originalRelativePos, BlockPos transformedRelativePos,
                             MultiblockSlotPredicate predicate) {

    public MultiblockPart(
            final BlockPos worldPos,
            final BlockPos originalRelativePos,
            final BlockPos transformedRelativePos,
            final MultiblockSlotPredicate predicate
    ) {
        this.worldPos = worldPos.immutable();
        this.originalRelativePos = originalRelativePos.immutable();
        this.transformedRelativePos = transformedRelativePos.immutable();
        this.predicate = predicate;
    }

    public MultiblockPartData createData(
            final UUID instanceId,
            final ResourceLocation definitionId,
            final BlockPos origin,
            final MultiblockOrientation orientation
    ) {
        return new MultiblockPartData(
                instanceId,
                definitionId,
                origin,
                this.worldPos,
                this.originalRelativePos,
                this.transformedRelativePos,
                orientation
        );
    }

}