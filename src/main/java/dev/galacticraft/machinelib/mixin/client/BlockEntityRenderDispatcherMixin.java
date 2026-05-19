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

import com.mojang.blaze3d.vertex.PoseStack;
import dev.galacticraft.machinelib.client.impl.multiblock.ClientMultiblockManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses block entity rendering for formed multiblock parts on the client.
 *
 * <p>This is separate from normal block model suppression because many blocks,
 * such as chests or special machine blocks, render additional geometry through a
 * block entity renderer rather than only through chunk-baked block models.</p>
 */
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {

    /**
     * Cancels block entity rendering when the block entity belongs to a synced
     * formed multiblock part.
     *
     * @param blockEntity block entity being rendered
     * @param tickDelta partial tick value
     * @param matrices active pose stack
     * @param consumers render buffer source
     * @param ci callback info
     */
    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true
    )
    private <E extends BlockEntity> void machinelib$hideFormedMultiblockBlockEntity(
            final E blockEntity,
            final float tickDelta,
            final PoseStack matrices,
            final MultiBufferSource consumers,
            final CallbackInfo ci
    ) {
        if (ClientMultiblockManager.isKnownPart(blockEntity.getBlockPos())) {
            ci.cancel();
        }
    }

}