package dev.galacticraft.machinelib.mixin.client;

import dev.galacticraft.machinelib.client.impl.multiblock.ClientMultiblockManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes client-hidden formed multiblock parts non-occluding for client lighting.
 *
 * <p>MachineLib hides formed multiblock structure blocks visually, but the client
 * light calculations still query the real block states. Without this mixin, those
 * hidden blocks continue to block skylight, block light, and ambient-occlusion
 * sampling, causing black faces around the formed visual.</p>
 *
 * <p>This is client-only. It does not alter server lighting, collision, gameplay,
 * or saved world state.</p>
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateLightOcclusionMixin {

    /**
     * Treats formed multiblock parts as having no light-blocking opacity.
     *
     * @param level block getter
     * @param pos queried block position
     * @param cir callback info
     */
    @Inject(
            method = "getLightBlock",
            at = @At("HEAD"),
            cancellable = true
    )
    private void machinelib$formedMultiblockPartsDoNotBlockLight(
            final BlockGetter level,
            final BlockPos pos,
            final CallbackInfoReturnable<Integer> cir
    ) {
        if (ClientMultiblockManager.isKnownPart(pos)) {
            cir.setReturnValue(0);
        }
    }

    /**
     * Allows skylight to propagate through formed multiblock parts on the client.
     *
     * @param level block getter
     * @param pos queried block position
     * @param cir callback info
     */
    @Inject(
            method = "propagatesSkylightDown",
            at = @At("HEAD"),
            cancellable = true
    )
    private void machinelib$formedMultiblockPartsPropagateSkylight(
            final BlockGetter level,
            final BlockPos pos,
            final CallbackInfoReturnable<Boolean> cir
    ) {
        if (ClientMultiblockManager.isKnownPart(pos)) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Prevents formed multiblock parts from being treated as view-blocking during
     * ambient-occlusion and neighbour brightness calculations.
     *
     * @param level block getter
     * @param pos queried block position
     * @param cir callback info
     */
    @Inject(
            method = "isViewBlocking",
            at = @At("HEAD"),
            cancellable = true
    )
    private void machinelib$formedMultiblockPartsDoNotBlockView(
            final BlockGetter level,
            final BlockPos pos,
            final CallbackInfoReturnable<Boolean> cir
    ) {
        if (ClientMultiblockManager.isKnownPart(pos)) {
            cir.setReturnValue(false);
        }
    }
}