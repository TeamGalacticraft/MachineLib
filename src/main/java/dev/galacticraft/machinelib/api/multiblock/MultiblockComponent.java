package dev.galacticraft.machinelib.api.multiblock;

/**
 * Runtime logic attached to a formed multiblock machine.
 *
 * <p>Components are the first step toward real multiblock machines. They allow a
 * formed structure to hold runtime behaviour such as inventories, tanks, energy,
 * recipes, progress, animation state, and machine status.</p>
 *
 * <p>This first implementation is runtime-only. Persistence should be added
 * later once the component model is stable.</p>
 */
public interface MultiblockComponent {

    /**
     * Called once after the component is created and attached to a formed
     * machine.
     *
     * @param context component context
     */
    default void onFormed(final MultiblockComponentContext context) {

    }

    /**
     * Called once per server tick while the formed machine is loaded.
     *
     * @param context component context
     */
    default void tick(final MultiblockComponentContext context) {

    }

    /**
     * Called when the formed machine is permanently invalidated.
     *
     * <p>This is used when the structure is broken and removed from persistent
     * saved data.</p>
     *
     * @param context component context
     */
    default void onInvalidated(final MultiblockComponentContext context) {

    }

    /**
     * Called when the runtime machine is unloaded while persistent saved data is
     * kept.
     *
     * <p>This is used for chunk unload/runtime unload behaviour.</p>
     *
     * @param context component context
     */
    default void onRuntimeUnloaded(final MultiblockComponentContext context) {

    }

}