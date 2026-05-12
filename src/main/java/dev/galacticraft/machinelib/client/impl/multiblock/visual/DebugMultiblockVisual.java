package dev.galacticraft.machinelib.client.impl.multiblock.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.galacticraft.machinelib.api.multiblock.visual.MultiblockVisualContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Temporary debug visual that renders a white outline around a formed multiblock.
 *
 * <p>This visual exists to prove that the formed multiblock visual lifecycle is
 * working before the generic Blockbench model loader is implemented. It should be
 * replaced or supplemented later by static frame/model visuals.</p>
 */
public final class DebugMultiblockVisual implements ClientFormedMultiblockVisual {

    /**
     * Creates a debug multiblock bounds visual.
     */
    public DebugMultiblockVisual() {

    }

    /**
     * Renders a white line box around the formed multiblock bounds.
     *
     * @param context immutable formed multiblock visual context
     * @param bounds world-space bounds of the formed multiblock instance
     * @param renderContext Fabric world render context
     * @param matrices active pose stack
     * @param cameraPos current camera position
     * @param tickDelta partial tick value
     */
    @Override
    public void render(
            final MultiblockVisualContext context,
            final AABB bounds,
            final WorldRenderContext renderContext,
            final PoseStack matrices,
            final Vec3 cameraPos,
            final float tickDelta
    ) {
        final MultiBufferSource consumers = renderContext.consumers();

        if (consumers == null) {
            return;
        }

        final AABB cameraRelativeBounds = bounds.move(
                -cameraPos.x,
                -cameraPos.y,
                -cameraPos.z
        );

        LevelRenderer.renderLineBox(
                matrices,
                consumers.getBuffer(RenderType.lines()),
                cameraRelativeBounds,
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );
    }

}