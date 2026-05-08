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

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Creates menus for formed multiblock machines.
 *
 * <p>This allows arbitrary formed multiblocks to open menus without requiring a
 * controller block entity. The menu is created from the formed-machine context
 * instead of from a single block entity.</p>
 */
@FunctionalInterface
public interface MultiblockMenuFactory {

    /**
     * Creates a menu for a formed multiblock.
     *
     * @param context menu context
     * @param syncId vanilla menu sync id
     * @param inventory player inventory
     * @param player player opening the menu
     * @return created menu, or {@code null} to refuse opening
     */
    AbstractContainerMenu createMenu(
            MultiblockMenuContext context,
            int syncId,
            Inventory inventory,
            Player player
    );

    /**
     * Gets the display name used for this multiblock menu.
     *
     * @param context menu context
     * @return display name
     */
    default Component getDisplayName(final MultiblockMenuContext context) {
        return Component.translatable(
                "container." + context.definition().id().getNamespace() + "." + context.definition().id().getPath()
        );
    }

    /**
     * Gets the opening data sent to the client when this menu opens.
     *
     * @param context menu context
     * @param player player opening the menu
     * @return screen opening data
     */
    default Object getScreenOpeningData(
            final MultiblockMenuContext context,
            final ServerPlayer player
    ) {
        return new MultiblockMenuOpeningData(
                context.instanceId(),
                context.definition().id(),
                context.origin(),
                context.clickedPos(),
                context.orientation()
        );
    }

    /**
     * Whether opening this menu should close the player's current screen first.
     *
     * @return {@code true} to close the current screen
     */
    default boolean shouldCloseCurrentScreen() {
        return true;
    }

}