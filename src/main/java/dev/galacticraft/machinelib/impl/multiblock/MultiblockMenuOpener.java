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

import dev.galacticraft.machinelib.api.multiblock.MultiblockMenuFactory;
import dev.galacticraft.machinelib.api.multiblock.MultiblockPartInteractionContext;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Opens menus for formed multiblocks.
 */
public final class MultiblockMenuOpener {

    private MultiblockMenuOpener() {

    }

    /**
     * Opens a multiblock menu for an interaction context.
     *
     * @param interaction interaction context
     * @param factory menu factory
     * @return interaction result
     */
    public static InteractionResult open(
            final MultiblockPartInteractionContext interaction,
            final MultiblockMenuFactory factory
    ) {
        if (factory == null) {
            return InteractionResult.PASS;
        }

        final SimpleMultiblockMenuContext menuContext =
                new SimpleMultiblockMenuContext(interaction);

        interaction.player().openMenu(new ExtendedScreenHandlerFactory<>() {
            @Override
            public AbstractContainerMenu createMenu(
                    final int syncId,
                    final Inventory inventory,
                    final Player player
            ) {
                return factory.createMenu(
                        menuContext,
                        syncId,
                        inventory,
                        player
                );
            }

            @Override
            public Component getDisplayName() {
                return factory.getDisplayName(menuContext);
            }

            @Override
            public Object getScreenOpeningData(final ServerPlayer player) {
                return factory.getScreenOpeningData(
                        menuContext,
                        player
                );
            }

            @Override
            public boolean shouldCloseCurrentScreen() {
                return factory.shouldCloseCurrentScreen();
            }
        });

        return InteractionResult.CONSUME;
    }

}