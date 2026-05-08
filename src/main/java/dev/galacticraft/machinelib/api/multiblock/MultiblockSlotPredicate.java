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

package dev.galacticraft.machinelib.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface MultiblockSlotPredicate {

    boolean matches(Level level, BlockPos pos, BlockState state);

    boolean canBeDetectionCenter();

    Block[] detectionBlocks();

    String signatureKey();

    static MultiblockSlotPredicate block(final Block block) {
        return new MultiblockSlotPredicate() {

            @Override
            public boolean matches(final Level level, final BlockPos pos, final BlockState state) {
                return state.is(block);
            }

            @Override
            public boolean canBeDetectionCenter() {
                return true;
            }

            @Override
            public Block[] detectionBlocks() {
                return new Block[] { block };
            }

            @Override
            public String signatureKey() {
                return "block:" + BuiltInRegistries.BLOCK.getKey(block);
            }
        };
    }

    static MultiblockSlotPredicate anyBlock() {
        return new MultiblockSlotPredicate() {

            @Override
            public boolean matches(final Level level, final BlockPos pos, final BlockState state) {
                return !state.isAir();
            }

            @Override
            public boolean canBeDetectionCenter() {
                return false;
            }

            @Override
            public Block[] detectionBlocks() {
                return new Block[0];
            }

            @Override
            public String signatureKey() {
                return "any_block";
            }
        };
    }

    static MultiblockSlotPredicate air() {
        return new MultiblockSlotPredicate() {

            @Override
            public boolean matches(final Level level, final BlockPos pos, final BlockState state) {
                return state.isAir();
            }

            @Override
            public boolean canBeDetectionCenter() {
                return false;
            }

            @Override
            public Block[] detectionBlocks() {
                return new Block[0];
            }

            @Override
            public String signatureKey() {
                return "air";
            }
        };
    }

}