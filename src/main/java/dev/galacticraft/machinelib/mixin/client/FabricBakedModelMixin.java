package dev.galacticraft.machinelib.mixin.client;

import dev.galacticraft.machinelib.client.impl.multiblock.ClientMultiblockManager;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

/**
 * Suppresses Fabric Renderer API model emission for formed multiblock parts.
 *
 * <p>MachineLib uses Fabric's renderer API, and Fabric can route chunk rebuilds
 * through {@link FabricBakedModel#emitBlockQuads} instead of vanilla's
 * {@code BlockRenderDispatcher} or {@code ModelBlockRenderer}. Cancelling here
 * prevents Fabric-rendered block models from emitting quads for formed
 * multiblock structure blocks.</p>
 */
@Mixin(FabricBakedModel.class)
public interface FabricBakedModelMixin {

    /**
     * Cancels Fabric Renderer API quad emission for client-known formed
     * multiblock part positions.
     *
     * @param blockView render world
     * @param state block state being rendered
     * @param pos world position being rendered
     * @param randomSupplier random supplier used by the renderer
     * @param context Fabric render context
     * @param ci callback info
     */
    @Inject(
            method = "emitBlockQuads",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void machinelib$hideFormedMultiblockPartQuads(
            final BlockAndTintGetter blockView,
            final BlockState state,
            final BlockPos pos,
            final Supplier<RandomSource> randomSupplier,
            final RenderContext context,
            final CallbackInfo ci
    ) {
        if (ClientMultiblockManager.isKnownPart(pos)) {
            ci.cancel();
        }
    }

}