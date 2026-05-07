package dev.galacticraft.machinelib.api.multiblock;

public interface MultiblockBuilder {

    MultiblockBuilder pattern(MultiblockPattern pattern);

    MultiblockBuilder rule(FormationRule rule);

    MultiblockDefinition build();

}