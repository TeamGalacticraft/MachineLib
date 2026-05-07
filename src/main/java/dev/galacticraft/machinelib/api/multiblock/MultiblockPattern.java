package dev.galacticraft.machinelib.api.multiblock;

public interface MultiblockPattern {

    int sizeX();

    int sizeY();

    int sizeZ();

    MultiblockSlotPredicate predicateAt(int x, int y, int z);

}