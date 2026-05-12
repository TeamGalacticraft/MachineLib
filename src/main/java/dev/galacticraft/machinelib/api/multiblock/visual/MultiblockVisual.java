package dev.galacticraft.machinelib.api.multiblock.visual;

/**
 * Base interface for a client-side visual attached to a formed multiblock.
 *
 * <p>This interface intentionally contains no Minecraft client rendering types.
 * That keeps the public multiblock definition API safe to reference from common
 * code and dedicated server code. Client-only renderable visuals should also
 * implement the client-side visual interface used by MachineLib's client render
 * manager.</p>
 *
 * <p>A visual is created when the client receives a formed multiblock sync packet
 * and destroyed when the client receives the corresponding remove packet or when
 * all client multiblock state is cleared. Visuals are runtime-only and are never
 * saved to disk.</p>
 */
public interface MultiblockVisual {

    /**
     * Called once per client tick while the visual is alive.
     *
     * <p>Static visuals may leave this method empty. Animated visuals can use this
     * to advance timelines, update cached transforms, invalidate dynamic meshes, or
     * prepare render state for the next frame.</p>
     *
     * @param context immutable context describing the synced formed multiblock
     */
    default void tick(final MultiblockVisualContext context) {

    }

    /**
     * Called when the visual is removed from the client visual manager.
     *
     * <p>Implementations should release any client-side resources they own here.
     * Static debug visuals usually do not need to do anything. Future model,
     * texture, buffer, or Glasswork-backed visuals may use this to unregister
     * scenes, release buffers, or clear cached render data.</p>
     *
     * @param context immutable context describing the synced formed multiblock
     */
    default void close(final MultiblockVisualContext context) {

    }

}