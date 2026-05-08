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

import dev.galacticraft.machinelib.api.multiblock.MultiblockDefinition;
import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import dev.galacticraft.machinelib.api.multiblock.MultiblockSlotPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public final class CompiledLocalVariant {

    private final MultiblockDefinition definition;
    private final MultiblockOrientation orientation;

    private final BlockPos transformedRelativePos;

    private final MultiblockSlotPredicate up;
    private final MultiblockSlotPredicate down;
    private final MultiblockSlotPredicate north;
    private final MultiblockSlotPredicate south;
    private final MultiblockSlotPredicate east;
    private final MultiblockSlotPredicate west;

    public CompiledLocalVariant(
            final MultiblockDefinition definition,
            final MultiblockOrientation orientation,
            final BlockPos transformedRelativePos,
            final MultiblockSlotPredicate up,
            final MultiblockSlotPredicate down,
            final MultiblockSlotPredicate north,
            final MultiblockSlotPredicate south,
            final MultiblockSlotPredicate east,
            final MultiblockSlotPredicate west
    ) {
        this.definition = definition;
        this.orientation = orientation;
        this.transformedRelativePos = transformedRelativePos.immutable();
        this.up = up;
        this.down = down;
        this.north = north;
        this.south = south;
        this.east = east;
        this.west = west;
    }

    public MultiblockDefinition definition() {
        return this.definition;
    }

    public MultiblockOrientation orientation() {
        return this.orientation;
    }

    public int specificity() {
        int specificity = 0;

        if (this.up != null) {
            specificity++;
        }

        if (this.down != null) {
            specificity++;
        }

        if (this.north != null) {
            specificity++;
        }

        if (this.south != null) {
            specificity++;
        }

        if (this.east != null) {
            specificity++;
        }

        if (this.west != null) {
            specificity++;
        }

        return specificity;
    }

    public boolean matches(
            final ServerLevel level,
            final BlockPos changedPos,
            final BlockState upState,
            final BlockState downState,
            final BlockState northState,
            final BlockState southState,
            final BlockState eastState,
            final BlockState westState
    ) {
        if (this.up != null && !this.up.matches(level, changedPos.above(), upState)) {
            return false;
        }

        if (this.down != null && !this.down.matches(level, changedPos.below(), downState)) {
            return false;
        }

        if (this.north != null && !this.north.matches(level, changedPos.north(), northState)) {
            return false;
        }

        if (this.south != null && !this.south.matches(level, changedPos.south(), southState)) {
            return false;
        }

        if (this.east != null && !this.east.matches(level, changedPos.east(), eastState)) {
            return false;
        }

        if (this.west != null && !this.west.matches(level, changedPos.west(), westState)) {
            return false;
        }

        return true;
    }

    public BlockPos resolveOrigin(final BlockPos changedPos) {
        return changedPos.offset(
                -this.transformedRelativePos.getX(),
                -this.transformedRelativePos.getY(),
                -this.transformedRelativePos.getZ()
        );
    }

}