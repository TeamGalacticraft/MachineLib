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