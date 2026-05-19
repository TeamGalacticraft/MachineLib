package dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.galacticraft.machinelib.api.multiblock.visual.MultiblockVisualContext;
import dev.galacticraft.machinelib.client.impl.multiblock.visual.ClientFormedMultiblockVisual;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Static glTF multiblock visual renderer.
 *
 * <p>This renderer places a loaded glTF model relative to a declared anchor block
 * inside the formed multiblock. The model offset is local to that anchor and is
 * rotated by the multiblock orientation, so directional and non-standard
 * orientations render consistently.</p>
 */
public final class StaticGltfMultiblockVisual implements ClientFormedMultiblockVisual {

    private final ResourceLocation modelId;
    private final StaticGltfVisualTransform transform;

    /**
     * Creates a static glTF visual using the default origin anchor.
     *
     * @param modelId loaded visual model id
     */
    public StaticGltfMultiblockVisual(final ResourceLocation modelId) {
        this(
                modelId,
                StaticGltfVisualTransform.identity()
        );
    }

    /**
     * Creates a static glTF visual.
     *
     * @param modelId loaded visual model id
     * @param transform anchor-relative visual transform
     */
    public StaticGltfMultiblockVisual(
            final ResourceLocation modelId,
            final StaticGltfVisualTransform transform
    ) {
        this.modelId = modelId;
        this.transform = transform;
    }

    /**
     * Renders the loaded glTF model as camera-relative debug wireframe geometry.
     *
     * <p>This implementation transforms every model-space vertex manually into
     * world space using {@link StaticGltfVisualTransform#modelPointToWorld}. This
     * avoids pose-stack matrix ordering issues and guarantees the visual follows the
     * same orientation and pattern dimensions as the formed multiblock.</p>
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
        final GltfVisualModel model = GltfVisualModelManager.INSTANCE.get(this.modelId);

        if (model == null) {
            return;
        }

        if (renderContext.consumers() == null) {
            return;
        }

        final VertexConsumer consumer = renderContext.consumers()
                .getBuffer(RenderType.lines());

        matrices.pushPose();

        for (final GltfVisualTriangle triangle : model.mesh().triangles()) {
            final Vec3 a = this.transform.modelPointToWorld(
                    triangle.a().position(),
                    context.origin(),
                    context.orientation(),
                    context.patternWidth(),
                    context.patternHeight(),
                    context.patternDepth()
            );

            final Vec3 b = this.transform.modelPointToWorld(
                    triangle.b().position(),
                    context.origin(),
                    context.orientation(),
                    context.patternWidth(),
                    context.patternHeight(),
                    context.patternDepth()
            );

            final Vec3 c = this.transform.modelPointToWorld(
                    triangle.c().position(),
                    context.origin(),
                    context.orientation(),
                    context.patternWidth(),
                    context.patternHeight(),
                    context.patternDepth()
            );

            renderLine(
                    matrices,
                    consumer,
                    cameraRelative(a, cameraPos),
                    cameraRelative(b, cameraPos)
            );

            renderLine(
                    matrices,
                    consumer,
                    cameraRelative(b, cameraPos),
                    cameraRelative(c, cameraPos)
            );

            renderLine(
                    matrices,
                    consumer,
                    cameraRelative(c, cameraPos),
                    cameraRelative(a, cameraPos)
            );
        }

        matrices.popPose();
    }

    /**
     * Converts a world-space point into camera-relative render coordinates.
     *
     * @param point world-space point
     * @param cameraPos camera position
     * @return camera-relative point
     */
    private static Vector3f cameraRelative(
            final Vec3 point,
            final Vec3 cameraPos
    ) {
        return new Vector3f(
                (float) (point.x - cameraPos.x),
                (float) (point.y - cameraPos.y),
                (float) (point.z - cameraPos.z)
        );
    }

    /**
     * Emits one white debug line.
     *
     * @param matrices active pose stack
     * @param consumer line vertex consumer
     * @param from first point
     * @param to second point
     */
    private static void renderLine(
            final PoseStack matrices,
            final VertexConsumer consumer,
            final Vector3f from,
            final Vector3f to
    ) {
        final PoseStack.Pose pose = matrices.last();

        final float dx = to.x() - from.x();
        final float dy = to.y() - from.y();
        final float dz = to.z() - from.z();
        final float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (length <= 0.0F) {
            return;
        }

        consumer.addVertex(
                        pose,
                        from.x(),
                        from.y(),
                        from.z()
                )
                .setColor(255, 255, 255, 255)
                .setNormal(
                        pose,
                        dx / length,
                        dy / length,
                        dz / length
                );

        consumer.addVertex(
                        pose,
                        to.x(),
                        to.y(),
                        to.z()
                )
                .setColor(255, 255, 255, 255)
                .setNormal(
                        pose,
                        dx / length,
                        dy / length,
                        dz / length
                );
    }

}