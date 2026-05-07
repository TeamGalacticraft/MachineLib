package dev.galacticraft.machinelib.client.impl;

import dev.galacticraft.machinelib.client.impl.multiblock.TestIronCubeMultiblockScreen;
import dev.galacticraft.machinelib.impl.TestMenuTypeRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

public final class TestMenuScreens {

    private TestMenuScreens() {

    }

    public static void register() {
        MenuScreens.register(
                TestMenuTypeRegistry.TEST_IRON_CUBE_MULTIBLOCK,
                TestIronCubeMultiblockScreen::new
        );
    }
}