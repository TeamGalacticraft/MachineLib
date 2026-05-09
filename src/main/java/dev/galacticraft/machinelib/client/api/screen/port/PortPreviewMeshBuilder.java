package dev.galacticraft.machinelib.client.api.screen.port;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a cached preview mesh from client-side baked block models.
 */
public final class PortPreviewMeshBuilder {

    private PortPreviewMeshBuilder() {

    }

    /**
     * Builds a static preview mesh from the scene's current block list.
     *
     * <p>This method reads each block state's client baked model and copies its
     * baked quads into preview-space. The resulting mesh can then be rendered
     * repeatedly without asking the block renderer to rebuild model quads every
     * frame.</p>
     *
     * @param key cache key for the block list
     * @param blocks preview blocks
     * @return cached preview mesh
     */
    public static PortPreviewMesh build(
            final PortPreviewMeshKey key,
            final List<PreviewBlock> blocks
    ) {
        final Minecraft minecraft = Minecraft.getInstance();
        final BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
        final List<PortPreviewQuad> quads = new ArrayList<>();

        for (final PreviewBlock block : blocks) {
            if (block.state().isAir()) {
                continue;
            }

            final BakedModel model = dispatcher.getBlockModel(block.state());

            addQuads(
                    quads,
                    model,
                    block,
                    null
            );

            for (final Direction direction : Direction.values()) {
                addQuads(
                        quads,
                        model,
                        block,
                        direction
                );
            }
        }

        return new PortPreviewMesh(
                key,
                List.copyOf(quads)
        );
    }

    /**
     * Adds baked quads for one cull direction.
     *
     * @param output output quad list
     * @param model baked model
     * @param block preview block
     * @param direction cull direction, or {@code null} for uncullable quads
     */
    private static void addQuads(
            final List<PortPreviewQuad> output,
            final BakedModel model,
            final PreviewBlock block,
            final Direction direction
    ) {
        final RandomSource random = RandomSource.create(42L);

        model.getQuads(
                block.state(),
                direction,
                random
        ).forEach(quad -> output.add(PortPreviewQuad.fromBakedQuad(
                quad,
                block.previewPos().getX(),
                block.previewPos().getY(),
                block.previewPos().getZ()
        )));
    }
}