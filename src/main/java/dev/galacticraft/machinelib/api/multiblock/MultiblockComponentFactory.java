package dev.galacticraft.machinelib.api.multiblock;

/**
 * Creates a runtime component for a formed multiblock machine.
 *
 * @param <T> component type
 */
@FunctionalInterface
public interface MultiblockComponentFactory<T extends MultiblockComponent> {

    /**
     * Creates a new component instance.
     *
     * @param context formed machine context
     * @return component instance
     */
    T create(MultiblockComponentContext context);

}