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

package dev.galacticraft.machinelib.api.multiblock;

import dev.galacticraft.machinelib.api.storage.StorageSpec;
import dev.galacticraft.machinelib.impl.multiblock.FormedMultiblockMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;

/**
 * Shared definition for a default MachineLib-style formed multiblock menu.
 *
 * <p>This object ties together the storage layout, menu type, server-side menu
 * factory, and display name. Keeping these values together prevents the
 * server-side storage component and client-side menu from accidentally using
 * different storage specs.</p>
 *
 * @param storage storage spec used by the formed machine and client menu
 * @param menuType menu type
 * @param serverFactory server-side menu factory
 * @param displayName menu display name
 * @param <Menu> menu type
 */
public record MultiblockMachineMenuSpec<Menu extends MultiblockMachineMenu>(
        StorageSpec storage,
        MenuType<Menu> menuType,
        MultiblockServerMenuFactory<Menu> serverFactory,
        Component displayName
) {

    /**
     * Creates a server-side menu for this multiblock machine menu spec.
     *
     * @param syncId sync id
     * @param player server player
     * @param machine formed multiblock machine
     * @param clickedPos clicked multiblock part position
     * @return server-side menu
     */
    public Menu createServerMenu(
            final int syncId,
            final ServerPlayer player,
            final FormedMultiblockMachine machine,
            final BlockPos clickedPos
    ) {
        return this.serverFactory.create(
                syncId,
                player,
                machine,
                clickedPos
        );
    }

}