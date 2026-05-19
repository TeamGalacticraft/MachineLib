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

package dev.galacticraft.machinelib.mixin.client;

import dev.galacticraft.machinelib.client.impl.multiblock.ClientMultiblockManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes hidden formed multiblock parts stop occluding neighbouring block faces.
 *
 * <p>MachineLib suppresses the normal rendering of formed multiblock structure
 * blocks, but those blocks still exist in the client world. Vanilla face culling
 * still sees them as solid neighbours unless this check is adjusted. This mixin
 * allows ordinary neighbouring blocks to render faces that touch a formed
 * multiblock part.</p>
 */
@Mixin(Block.class)
public abstract class BlockFaceOcclusionMixin {

    /**
     * Forces a face to render when the adjacent block position belongs to a
     * client-known formed multiblock part.
     *
     * @param state source block state whose face may render
     * @param level block getter
     * @param pos source block position
     * @param side source face direction
     * @param adjacentPos adjacent position in the queried direction
     * @param cir callback info
     */
    @Inject(
            method = "shouldRenderFace",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void machinelib$renderFacesAgainstHiddenMultiblockParts(
            final BlockState state,
            final BlockGetter level,
            final BlockPos pos,
            final Direction side,
            final BlockPos adjacentPos,
            final CallbackInfoReturnable<Boolean> cir
    ) {
        if (ClientMultiblockManager.isKnownPart(adjacentPos)) {
            cir.setReturnValue(true);
        }
    }

}