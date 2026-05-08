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

import dev.galacticraft.machinelib.api.multiblock.MultiblockPattern;
import dev.galacticraft.machinelib.api.multiblock.MultiblockSlotPredicate;

public final class SimpleMultiblockPattern implements MultiblockPattern {

    private final int width;
    private final int height;
    private final int depth;

    private final MultiblockSlotPredicate[][][] predicates;

    public SimpleMultiblockPattern(
            final int width,
            final int height,
            final int depth
    ) {
        this.width = width;
        this.height = height;
        this.depth = depth;

        this.predicates = new MultiblockSlotPredicate[width][height][depth];
    }

    public SimpleMultiblockPattern set(
            final int x,
            final int y,
            final int z,
            final MultiblockSlotPredicate predicate
    ) {
        this.predicates[x][y][z] = predicate;
        return this;
    }

    @Override
    public int sizeX() {
        return this.width;
    }

    @Override
    public int sizeY() {
        return this.height;
    }

    @Override
    public int sizeZ() {
        return this.depth;
    }

    @Override
    public MultiblockSlotPredicate predicateAt(
            final int x,
            final int y,
            final int z
    ) {
        return this.predicates[x][y][z];
    }

}