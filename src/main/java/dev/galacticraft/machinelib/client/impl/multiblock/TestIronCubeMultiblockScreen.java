package dev.galacticraft.machinelib.client.impl.multiblock;

import dev.galacticraft.machinelib.client.api.screen.MultiblockMachineScreen;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.multiblock.TestIronCubeMultiblockMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class TestIronCubeMultiblockScreen extends MultiblockMachineScreen<TestIronCubeMultiblockMenu> {

    /**
     * Creates the test iron cube multiblock screen.
     *
     * @param menu menu
     * @param inventory player inventory
     * @param title title
     */
    public TestIronCubeMultiblockScreen(
            final TestIronCubeMultiblockMenu menu,
            final Inventory inventory,
            final Component title
    ) {
        super(
                menu,
                title,
                Constant.id("textures/gui/screen.png")
        );
    }

}