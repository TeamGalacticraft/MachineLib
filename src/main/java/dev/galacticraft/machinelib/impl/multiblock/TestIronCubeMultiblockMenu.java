package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.MultiblockMachineMenu;
import dev.galacticraft.machinelib.api.multiblock.MultiblockMenuOpeningData;
import dev.galacticraft.machinelib.api.storage.StorageSpec;
import dev.galacticraft.machinelib.impl.TestMenuTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;

/**
 * Test MachineLib-style menu for the formed iron cube multiblock.
 */
public final class TestIronCubeMultiblockMenu extends MultiblockMachineMenu {

    /**
     * Creates the server-side test menu.
     *
     * @param syncId sync id
     * @param player player
     * @param machine formed machine
     * @param clickedPos clicked part position
     */
    public TestIronCubeMultiblockMenu(
            final int syncId,
            final ServerPlayer player,
            final FormedMultiblockMachine machine,
            final BlockPos clickedPos
    ) {
        super(
                TestMenuTypeRegistry.TEST_IRON_CUBE_MULTIBLOCK,
                syncId,
                player,
                machine,
                clickedPos
        );
    }

    /**
     * Creates the client-side test menu.
     *
     * @param syncId sync id
     * @param inventory player inventory
     * @param openingData multiblock opening data
     * @param spec storage spec
     */
    public TestIronCubeMultiblockMenu(
            final int syncId,
            final Inventory inventory,
            final MultiblockMenuOpeningData openingData,
            final StorageSpec spec
    ) {
        super(
                TestMenuTypeRegistry.TEST_IRON_CUBE_MULTIBLOCK,
                syncId,
                inventory,
                openingData,
                spec,
                8,
                84
        );
    }

}