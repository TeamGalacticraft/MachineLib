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

package dev.galacticraft.machinelib.impl.multiblock.detection;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class CompiledVariantBucket {

    private final Block centerBlock;

    private final List<CompiledLocalVariant> variants = new ArrayList<>();

    public CompiledVariantBucket(final Block centerBlock) {
        this.centerBlock = centerBlock;
    }

    public Block centerBlock() {
        return this.centerBlock;
    }

    public void add(final CompiledLocalVariant variant) {
        this.variants.add(variant);
    }

    public void sort() {
        this.variants.sort(
                Comparator.comparingInt(CompiledLocalVariant::specificity).reversed()
        );
    }

    public void test(
            final ServerLevel level,
            final BlockPos changedPos,
            final MultiblockValidationQueue validationQueue
    ) {
        if (this.variants.isEmpty()) {
            return;
        }

        final BlockState upState = level.getBlockState(changedPos.above());
        final BlockState downState = level.getBlockState(changedPos.below());
        final BlockState northState = level.getBlockState(changedPos.north());
        final BlockState southState = level.getBlockState(changedPos.south());
        final BlockState eastState = level.getBlockState(changedPos.east());
        final BlockState westState = level.getBlockState(changedPos.west());

        for (final CompiledLocalVariant variant : this.variants) {
            if (!variant.matches(
                    level,
                    changedPos,
                    upState,
                    downState,
                    northState,
                    southState,
                    eastState,
                    westState
            )) {
                continue;
            }

            validationQueue.enqueue(
                    new MultiblockCandidate(
                            level,
                            variant.resolveOrigin(changedPos),
                            variant.orientation(),
                            variant.definition()
                    )
            );
        }
    }

}