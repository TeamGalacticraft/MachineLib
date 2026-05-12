package dev.galacticraft.machinelib.api.multiblock.visual;

/**
 * Factory used to create a client-side visual for a formed multiblock.
 *
 * <p>Definitions store this factory, but the factory is only invoked on the
 * client when a formed multiblock is synced. The server never creates or renders
 * visual instances.</p>
 *
 * <p>Simple static visuals can return the same type of visual for every instance.
 * More advanced factories may inspect the context to choose a different model,
 * animation setup, or renderer depending on the formed machine's orientation,
 * size, or definition id.</p>
 */
@FunctionalInterface
public interface MultiblockVisualFactory {

    /**
     * Creates a new visual instance for one synced formed multiblock.
     *
     * <p>The returned visual is owned by the client visual manager until the
     * matching formed multiblock is removed. This method should return a new
     * independent object unless the visual is completely stateless.</p>
     *
     * @param context immutable client-side formed multiblock context
     * @return created visual, or {@code null} to render no visual
     */
    MultiblockVisual create(MultiblockVisualContext context);

}