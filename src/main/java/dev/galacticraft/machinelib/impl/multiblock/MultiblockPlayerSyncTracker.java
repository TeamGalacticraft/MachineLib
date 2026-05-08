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

import dev.galacticraft.machinelib.impl.network.s2c.MultiblockSyncAddPayload;
import dev.galacticraft.machinelib.impl.network.s2c.MultiblockSyncRemovePayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.*;

/**
 * Tracks which formed multiblock instances have been synced to each player.
 *
 * <p>This class acts as the server-side reconciliation layer for client
 * multiblock awareness. Rather than relying on a fragile chunk-watch callback,
 * it checks the server's current player chunk tracking state and keeps each
 * client synchronized with the formed machines they can currently observe.</p>
 */
public final class MultiblockPlayerSyncTracker {

    private static final Map<UUID, Set<UUID>> SYNCED_INSTANCES_BY_PLAYER =
            new HashMap<>();

    private MultiblockPlayerSyncTracker() {

    }

    /**
     * Reconciles all player multiblock sync state for a server level.
     *
     * <p>Any player tracking at least one chunk touched by a formed multiblock
     * receives that multiblock. Any player no longer tracking any touched chunk
     * receives a remove packet.</p>
     *
     * @param level server level to synchronize
     */
    public static void syncLevel(final ServerLevel level) {
        final MultiblockManager manager = MultiblockManager.get(level);

        for (final ServerPlayer player : level.players()) {
            syncPlayer(
                    player,
                    manager
            );
        }
    }

    /**
     * Removes all sync tracking state for a disconnected player.
     *
     * @param player disconnected player
     */
    public static void clearPlayer(final ServerPlayer player) {
        SYNCED_INSTANCES_BY_PLAYER.remove(player.getUUID());
    }

    /**
     * Marks a machine as already synced to a player.
     *
     * <p>This is used by immediate sync paths so the reconciliation pass does
     * not resend the same machine next tick.</p>
     *
     * @param player target player
     * @param machine formed machine
     */
    public static void markSynced(
            final ServerPlayer player,
            final FormedMultiblockMachine machine
    ) {
        SYNCED_INSTANCES_BY_PLAYER
                .computeIfAbsent(player.getUUID(), ignored -> new HashSet<>())
                .add(machine.instanceId());
    }

    /**
     * Marks a machine as no longer synced to a player.
     *
     * @param player target player
     * @param machine formed machine
     */
    public static void markRemoved(
            final ServerPlayer player,
            final FormedMultiblockMachine machine
    ) {
        final Set<UUID> synced = SYNCED_INSTANCES_BY_PLAYER.get(player.getUUID());

        if (synced == null) {
            return;
        }

        synced.remove(machine.instanceId());

        if (synced.isEmpty()) {
            SYNCED_INSTANCES_BY_PLAYER.remove(player.getUUID());
        }
    }

    /**
     * Removes a machine id from every tracked player.
     *
     * @param instanceId machine instance id
     */
    public static void forgetMachine(final UUID instanceId) {
        for (final Iterator<Set<UUID>> iterator = SYNCED_INSTANCES_BY_PLAYER.values().iterator(); iterator.hasNext(); ) {
            final Set<UUID> synced = iterator.next();

            synced.remove(instanceId);

            if (synced.isEmpty()) {
                iterator.remove();
            }
        }
    }

    private static void syncPlayer(
            final ServerPlayer player,
            final MultiblockManager manager
    ) {
        final Set<UUID> synced = SYNCED_INSTANCES_BY_PLAYER.computeIfAbsent(
                player.getUUID(),
                ignored -> new HashSet<>()
        );

        final Set<UUID> visible = new HashSet<>();

        for (final FormedMultiblockMachine machine : manager.formedMachines()) {
            if (!isPlayerTrackingMachine(player, machine)) {
                continue;
            }

            visible.add(machine.instanceId());

            if (!synced.contains(machine.instanceId())) {
                MultiblockSyncAddPayload.syncAddedTo(
                        player,
                        machine
                );

                synced.add(machine.instanceId());
            }
        }

        for (final UUID instanceId : List.copyOf(synced)) {
            if (visible.contains(instanceId)) {
                continue;
            }

            MultiblockSyncRemovePayload.syncRemovedTo(
                    player,
                    instanceId
            );

            synced.remove(instanceId);
        }

        if (synced.isEmpty()) {
            SYNCED_INSTANCES_BY_PLAYER.remove(player.getUUID());
        }
    }

    private static boolean isPlayerTrackingMachine(
            final ServerPlayer player,
            final FormedMultiblockMachine machine
    ) {
        for (final ChunkPos chunkPos : machine.touchedChunks()) {
            if (PlayerLookup.tracking(machine.level(), chunkPos).contains(player)) {
                return true;
            }
        }

        return false;
    }

}