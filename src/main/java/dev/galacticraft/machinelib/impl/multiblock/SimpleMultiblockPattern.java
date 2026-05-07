package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.MultiblockPattern;
import dev.galacticraft.machinelib.api.multiblock.MultiblockSlotPredicate;

public final class SimpleMultiblockPattern implements MultiblockPattern {

    private final int width;
    private final int height;
    private final int depth;

    private final MultiblockSlotPredicate[][][] predicates;

    public SimpleMultiblockPattern(
            final int width,
            final int height,
            final int depth
    ) {
        this.width = width;
        this.height = height;
        this.depth = depth;

        this.predicates = new MultiblockSlotPredicate[width][height][depth];
    }

    public SimpleMultiblockPattern set(
            final int x,
            final int y,
            final int z,
            final MultiblockSlotPredicate predicate
    ) {
        this.predicates[x][y][z] = predicate;
        return this;
    }

    @Override
    public int sizeX() {
        return this.width;
    }

    @Override
    public int sizeY() {
        return this.height;
    }

    @Override
    public int sizeZ() {
        return this.depth;
    }

    @Override
    public MultiblockSlotPredicate predicateAt(
            final int x,
            final int y,
            final int z
    ) {
        return this.predicates[x][y][z];
    }

}