package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.MultiblockComponent;
import dev.galacticraft.machinelib.api.multiblock.MultiblockComponentContext;
import net.minecraft.nbt.CompoundTag;

/**
 * Simple persistent test component that counts loaded ticks.
 */
public final class TestCounterComponent implements MultiblockComponent {

    private static final String TICKS = "Ticks";

    private int ticks;

    /**
     * Loads the saved tick counter.
     *
     * @param context component context
     * @param tag saved component tag
     */
    @Override
    public void load(
            final MultiblockComponentContext context,
            final CompoundTag tag
    ) {
        this.ticks = tag.getInt(TICKS);
    }

    /**
     * Saves the tick counter.
     *
     * @param context component context
     * @param tag component tag to write into
     */
    @Override
    public void save(
            final MultiblockComponentContext context,
            final CompoundTag tag
    ) {
        tag.putInt(TICKS, this.ticks);
    }

    /**
     * Increments the counter once per tick.
     *
     * @param context component context
     */
    @Override
    public void tick(final MultiblockComponentContext context) {
        this.ticks++;
        context.setChanged();
    }

    /**
     * Gets the number of loaded ticks this multiblock has accumulated.
     *
     * @return tick count
     */
    public int ticks() {
        return this.ticks;
    }

}