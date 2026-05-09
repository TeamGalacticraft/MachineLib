package dev.galacticraft.machinelib.client.api.screen.port;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;
import java.util.Objects;

/**
 * Stable cache key for the static preview mesh.
 *
 * <p>The key includes preview position, block state, and whether the block is
 * primary. It intentionally excludes port configuration, because ports are
 * rendered as dynamic overlays instead of baked into the static structure mesh.</p>
 *
 * @param hash calculated hash
 */
public record PortPreviewMeshKey(
        int hash
) {

    /**
     * Creates a mesh key for the current preview block list.
     *
     * @param blocks preview blocks
     * @return mesh key
     */
    public static PortPreviewMeshKey create(final List<PreviewBlock> blocks) {
        int hash = 1;

        for (final PreviewBlock block : blocks) {
            final BlockPos pos = block.previewPos();

            hash = 31 * hash + pos.hashCode();
            hash = 31 * hash + BuiltInRegistries.BLOCK.getKey(block.state().getBlock()).hashCode();
            hash = 31 * hash + block.state().hashCode();
            hash = 31 * hash + Objects.hashCode(block.primary());
        }

        return new PortPreviewMeshKey(hash);
    }
}