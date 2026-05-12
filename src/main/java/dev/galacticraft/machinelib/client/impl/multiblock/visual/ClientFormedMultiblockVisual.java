package dev.galacticraft.machinelib.client.impl.multiblock.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.galacticraft.machinelib.api.multiblock.visual.MultiblockVisual;
import dev.galacticraft.machinelib.api.multiblock.visual.MultiblockVisualContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Client-only extension for visuals that can be rendered in the world.
 *
 * <p>This interface is separated from the common {@link MultiblockVisual}
 * contract so common multiblock definitions do not need to directly reference
 * Minecraft client render classes. MachineLib's client visual manager only calls
 * render methods on visuals that implement this interface.</p>
 */
public interface ClientFormedMultiblockVisual extends MultiblockVisual {

    /**
     * Checks whether this visual should be rendered for the current frame.
     *
     * <p>The default implementation performs simple frustum culling against the
     * visual instance bounds. Advanced visuals may override this to use custom
     * bounds, animation-aware culling, distance culling, or multiple sub-bounds.</p>
     *
     * @param context immutable formed multiblock visual context
     * @param bounds world-space bounds of the formed multiblock instance
     * @param frustum current camera frustum
     * @param cameraPos current camera position
     * @return {@code true} if this visual should render this frame
     */
    default boolean shouldRender(
            final MultiblockVisualContext context,
            final AABB bounds,
            final Frustum frustum,
            final Vec3 cameraPos
    ) {
        return frustum == null || frustum.isVisible(bounds);
    }

    /**
     * Renders this visual.
     *
     * <p>The supplied pose stack is already positioned for world rendering by the
     * active render event. Implementations should subtract the camera position
     * when rendering world-space geometry directly, matching normal world render
     * event behaviour.</p>
     *
     * @param context immutable formed multiblock visual context
     * @param bounds world-space bounds of the formed multiblock instance
     * @param renderContext Fabric world render context
     * @param matrices active pose stack
     * @param cameraPos current camera position
     * @param tickDelta partial tick value
     */
    void render(
            MultiblockVisualContext context,
            AABB bounds,
            WorldRenderContext renderContext,
            PoseStack matrices,
            Vec3 cameraPos,
            float tickDelta
    );

}