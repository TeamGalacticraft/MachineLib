package dev.galacticraft.machinelib.impl;

import dev.galacticraft.machinelib.api.multiblock.MultiblockValidationMode;
import dev.galacticraft.machinelib.client.impl.multiblock.ClientMultiblockManager;
import dev.galacticraft.machinelib.impl.multiblock.MachineLibMultiblocks;
import dev.galacticraft.machinelib.impl.multiblock.MultiblockConfig;
import dev.galacticraft.machinelib.impl.multiblock.MultiblockManager;
import dev.galacticraft.machinelib.impl.multiblock.MultiblockPlayerSyncTracker;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

/**
 * Registers MachineLib runtime events.
 */
public final class MachineLibEvents {

    private MachineLibEvents() {

    }

    /**
     * Registers MachineLib server lifecycle and player interaction events.
     */
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (final ServerLevel level : server.getAllLevels()) {
                final MultiblockManager manager = MultiblockManager.get(level);

                manager.loadPersistentMachines();
                manager.tryRestorePersistentMachines();

                manager.tickComponents();

                MultiblockPlayerSyncTracker.syncLevel(level);
            }

            final MultiblockValidationMode mode =
                    MultiblockConfig.validationMode();

            if (mode == MultiblockValidationMode.IMMEDIATE) {
                return;
            }

            if (mode == MultiblockValidationMode.END_OF_TICK) {
                MachineLibMultiblocks.processQueuedValidations(
                        Integer.MAX_VALUE
                );

                return;
            }

            if (mode == MultiblockValidationMode.BUDGETED) {
                MachineLibMultiblocks.processQueuedValidations(
                        MultiblockConfig.validationBudgetPerTick()
                );
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            MultiblockPlayerSyncTracker.clearPlayer(handler.player);
        });

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.PASS;
            }

            if (level.isClientSide()) {
                if (ClientMultiblockManager.isKnownPart(hitResult.getBlockPos())) {
                    return InteractionResult.SUCCESS;
                }

                return InteractionResult.PASS;
            }

            if (!(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }

            if (!(level instanceof ServerLevel serverLevel)) {
                return InteractionResult.PASS;
            }

            return MultiblockManager.get(serverLevel).handleUsePart(
                    serverPlayer,
                    hitResult.getBlockPos(),
                    hand,
                    hitResult
            );
        });
    }

}