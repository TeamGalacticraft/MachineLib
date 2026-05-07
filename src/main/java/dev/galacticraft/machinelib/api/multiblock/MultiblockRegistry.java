package dev.galacticraft.machinelib.api.multiblock;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

public interface MultiblockRegistry {

    void register(MultiblockDefinition definition);

    MultiblockDefinition get(ResourceLocation id);

    Collection<MultiblockDefinition> definitions();

}