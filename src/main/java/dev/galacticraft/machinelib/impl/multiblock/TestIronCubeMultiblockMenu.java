/*
 * Copyright (c) 2021-2025 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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