package dev.galacticraft.machinelib.api.multiblock;

/**
 * Registered component factory entry for a multiblock definition.
 *
 * @param type component lookup type
 * @param factory component factory
 * @param <T> component type
 */
public record MultiblockComponentFactoryEntry<T extends MultiblockComponent>(
        Class<T> type,
        MultiblockComponentFactory<? extends T> factory
) {

}