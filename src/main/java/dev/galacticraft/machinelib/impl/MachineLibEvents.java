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