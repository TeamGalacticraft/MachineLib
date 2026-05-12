package dev.galacticraft.machinelib.api.multiblock.visual;

/**
 * Utility factories for common MachineLib multiblock visual behaviours.
 *
 * <p>This class is intentionally small for the first visual framework stage.
 * Static generic Blockbench model helpers should be added here later once the
 * model loader and mesh baker exist.</p>
 */
public final class MultiblockVisuals {

    private static final MultiblockVisualFactory NONE = context -> null;

    private MultiblockVisuals() {

    }

    /**
     * Gets a visual factory that creates no visual.
     *
     * <p>This is useful for explicit opt-out definitions and for tests that want
     * to exercise the visual lifecycle without rendering anything.</p>
     *
     * @return no-op visual factory
     */
    public static MultiblockVisualFactory none() {
        return NONE;
    }

}