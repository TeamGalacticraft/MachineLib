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