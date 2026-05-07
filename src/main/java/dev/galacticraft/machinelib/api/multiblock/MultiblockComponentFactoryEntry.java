package dev.galacticraft.machinelib.api.multiblock;

import net.minecraft.resources.ResourceLocation;

/**
 * Registered component factory entry for a multiblock definition.
 *
 * @param id stable persistent component id
 * @param type component lookup type
 * @param factory component factory
 * @param <T> component type
 */
public record MultiblockComponentFactoryEntry<T extends MultiblockComponent>(
        ResourceLocation id,
        Class<T> type,
        MultiblockComponentFactory<? extends T> factory
) {

}