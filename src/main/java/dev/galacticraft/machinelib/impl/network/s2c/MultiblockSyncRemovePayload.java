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

package dev.galacticraft.machinelib.impl.network.s2c;

import dev.galacticraft.machinelib.client.impl.multiblock.ClientMultiblockManager;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.multiblock.FormedMultiblockMachine;
import dev.galacticraft.machinelib.impl.multiblock.MultiblockPlayerSyncTracker;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Server-to-client payload removing a formed multiblock from the client-side
 * multiblock index.
 *
 * @param instanceId formed multiblock instance id
 */
public record MultiblockSyncRemovePayload(
        UUID instanceId
) implements CustomPacketPayload {

    public static final Type<MultiblockSyncRemovePayload> TYPE =
            new Type<>(Constant.id("multiblock_remove"));

    public static final StreamCodec<ByteBuf, MultiblockSyncRemovePayload> CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    MultiblockSyncRemovePayload::instanceId,
                    MultiblockSyncRemovePayload::new
            );

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Removes this multiblock from the client-side multiblock index.
     *
     * @param context client networking context
     */
    public void apply(final ClientPlayNetworking.Context context) {
        ClientMultiblockManager.removeMachine(this.instanceId);
    }

    /**
     * Sends a remove packet for the formed multiblock to all players currently
     * tracking any chunk touched by it.
     *
     * @param machine formed machine
     */
    public static void syncRemoved(final FormedMultiblockMachine machine) {
        final MultiblockSyncRemovePayload payload =
                new MultiblockSyncRemovePayload(machine.instanceId());

        final Set<ServerPlayer> players = new HashSet<>();

        for (final ChunkPos chunkPos : machine.touchedChunks()) {
            players.addAll(PlayerLookup.tracking(machine.level(), chunkPos));
        }

        for (final ServerPlayer player : players) {
            ServerPlayNetworking.send(player, payload);
            MultiblockPlayerSyncTracker.markRemoved(player, machine);
        }
    }

    /**
     * Sends a remove packet directly to one player.
     *
     * @param player target player
     * @param instanceId formed machine instance id
     */
    public static void syncRemovedTo(
            final ServerPlayer player,
            final UUID instanceId
    ) {
        ServerPlayNetworking.send(
                player,
                new MultiblockSyncRemovePayload(instanceId)
        );
    }

    /**
     * Local UUID stream codec.
     */
    private static final class UUIDUtil {

        private static final StreamCodec<ByteBuf, UUID> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_LONG,
                        UUID::getMostSignificantBits,
                        ByteBufCodecs.VAR_LONG,
                        UUID::getLeastSignificantBits,
                        UUID::new
                );

        private UUIDUtil() {

        }

    }

}