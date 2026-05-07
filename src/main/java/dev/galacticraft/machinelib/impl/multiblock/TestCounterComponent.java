package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.MultiblockComponent;
import dev.galacticraft.machinelib.api.multiblock.MultiblockComponentContext;
import net.minecraft.network.chat.Component;

/**
 * Simple test runtime component that counts ticks while the multiblock is
 * loaded and formed.
 */
public final class TestCounterComponent implements MultiblockComponent {

    private int ticks;

    @Override
    public void onFormed(final MultiblockComponentContext context) {
        this.ticks = 0;
    }

    @Override
    public void tick(final MultiblockComponentContext context) {
        this.ticks++;

        if (this.ticks % 200 == 0) {
            context.level().getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal(
                            "Multiblock " + context.definition().id()
                                    + " ticked for " + this.ticks + " ticks"
                    ),
                    false
            );
        }
    }

    /**
     * Gets the number of ticks this component has been loaded for.
     *
     * @return tick count
     */
    public int ticks() {
        return this.ticks;
    }

}