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

import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import dev.galacticraft.machinelib.client.impl.multiblock.ClientMultiblockManager;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.multiblock.FormedMultiblockMachine;
import dev.galacticraft.machinelib.impl.multiblock.MultiblockPart;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Server-to-client payload announcing that a formed multiblock is now active on
 * the client.
 *
 * <p>The client uses this data as a lightweight formed-part index. It does not
 * perform validation client-side.</p>
 *
 * @param instanceId formed multiblock instance id
 * @param definitionId registered multiblock definition id
 * @param origin formed multiblock origin
 * @param orientation formed multiblock orientation
 * @param partPositions world positions occupied by this formed multiblock
 */
public record MultiblockSyncAddPayload(
        UUID instanceId,
        ResourceLocation definitionId,
        BlockPos origin,
        MultiblockOrientation orientation,
        List<BlockPos> partPositions
) implements CustomPacketPayload {

    public static final Type<MultiblockSyncAddPayload> TYPE =
            new Type<>(Constant.id("multiblock_add"));

    private static final StreamCodec<ByteBuf, MultiblockOrientation> ORIENTATION_CODEC =
            StreamCodec.of(
                    (buffer, orientation) -> ByteBufCodecs.VAR_INT.encode(buffer, orientation.ordinal()),
                    buffer -> MultiblockOrientation.values()[ByteBufCodecs.VAR_INT.decode(buffer)]
            );

    public static final StreamCodec<ByteBuf, MultiblockSyncAddPayload> CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    MultiblockSyncAddPayload::instanceId,
                    ResourceLocation.STREAM_CODEC,
                    MultiblockSyncAddPayload::definitionId,
                    BlockPos.STREAM_CODEC,
                    MultiblockSyncAddPayload::origin,
                    ORIENTATION_CODEC,
                    MultiblockSyncAddPayload::orientation,
                    BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    MultiblockSyncAddPayload::partPositions,
                    MultiblockSyncAddPayload::new
            );

    public MultiblockSyncAddPayload {
        partPositions = List.copyOf(partPositions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Applies the synced formed multiblock to the client-side multiblock index.
     *
     * @param context client networking context
     */
    public void apply(final ClientPlayNetworking.Context context) {
        ClientMultiblockManager.addMachine(
                this.instanceId,
                this.definitionId,
                this.origin,
                this.orientation,
                this.partPositions
        );
    }

    /**
     * Sends this formed multiblock to all players currently tracking its origin
     * chunk.
     *
     * @param machine formed multiblock machine
     */
    public static void syncAdded(final FormedMultiblockMachine machine) {
        final MultiblockSyncAddPayload payload = create(machine);

        for (final ServerPlayer player : PlayerLookup.tracking(machine.level(), machine.origin())) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    /**
     * Sends this formed multiblock directly to one player.
     *
     * @param player target player
     * @param machine formed multiblock machine
     */
    public static void syncAddedTo(
            final ServerPlayer player,
            final FormedMultiblockMachine machine
    ) {
        ServerPlayNetworking.send(player, create(machine));
    }

    private static MultiblockSyncAddPayload create(final FormedMultiblockMachine machine) {
        final List<BlockPos> partPositions = new ArrayList<>();

        for (final MultiblockPart part : machine.parts()) {
            partPositions.add(part.worldPos());
        }

        return new MultiblockSyncAddPayload(
                machine.instanceId(),
                machine.definition().id(),
                machine.origin(),
                machine.orientation(),
                partPositions
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