package dev.galacticraft.machinelib.mixin;

import dev.galacticraft.machinelib.impl.multiblock.MachineLibMultiblocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin {

    @Inject(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("RETURN")
    )
    private void machinelib$onSetBlock(
            final BlockPos pos,
            final BlockState state,
            final int flags,
            final int recursionLeft,
            final CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValueZ()) {
            return;
        }

        final Object self = this;

        if (!(self instanceof ServerLevel serverLevel)) {
            return;
        }

        MachineLibMultiblocks.onBlockChanged(
                serverLevel,
                pos,
                state
        );
    }

}