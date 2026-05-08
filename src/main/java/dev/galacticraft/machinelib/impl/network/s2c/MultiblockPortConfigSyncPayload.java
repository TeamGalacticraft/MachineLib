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

import dev.galacticraft.machinelib.api.multiblock.MultiblockConfiguredMenu;
import dev.galacticraft.machinelib.api.multiblock.port.ConfiguredMultiblockPort;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortFace;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortMode;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortTarget;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortType;
import dev.galacticraft.machinelib.impl.Constant;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Server-to-client packet that synchronizes configured multiblock ports for an
 * open multiblock menu.
 *
 * @param instanceId formed multiblock instance id
 * @param ports configured ports
 */
public record MultiblockPortConfigSyncPayload(
        UUID instanceId,
        List<ConfiguredMultiblockPort> ports
) implements CustomPacketPayload {

    public static final Type<MultiblockPortConfigSyncPayload> TYPE =
            new Type<>(Constant.id("multiblock_port_config_sync"));

    private static final StreamCodec<ByteBuf, UUID> UUID_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_LONG,
                    UUID::getMostSignificantBits,
                    ByteBufCodecs.VAR_LONG,
                    UUID::getLeastSignificantBits,
                    UUID::new
            );

    public static final StreamCodec<ByteBuf, MultiblockPortConfigSyncPayload> CODEC =
            StreamCodec.of(
                    MultiblockPortConfigSyncPayload::encode,
                    MultiblockPortConfigSyncPayload::decode
            );

    /**
     * Encodes this payload into a network buffer.
     *
     * @param buffer network buffer
     * @param payload payload to encode
     */
    private static void encode(
            final ByteBuf buffer,
            final MultiblockPortConfigSyncPayload payload
    ) {
        UUID_CODEC.encode(buffer, payload.instanceId);
        ByteBufCodecs.VAR_INT.encode(buffer, payload.ports.size());

        for (final ConfiguredMultiblockPort port : payload.ports) {
            encodePort(buffer, port);
        }
    }

    /**
     * Decodes this payload from a network buffer.
     *
     * @param buffer network buffer
     * @return decoded payload
     */
    private static MultiblockPortConfigSyncPayload decode(final ByteBuf buffer) {
        final UUID instanceId = UUID_CODEC.decode(buffer);
        final int size = ByteBufCodecs.VAR_INT.decode(buffer);
        final List<ConfiguredMultiblockPort> ports = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            ports.add(decodePort(buffer));
        }

        return new MultiblockPortConfigSyncPayload(
                instanceId,
                List.copyOf(ports)
        );
    }

    /**
     * Encodes one configured port.
     *
     * @param buffer network buffer
     * @param port configured port
     */
    private static void encodePort(
            final ByteBuf buffer,
            final ConfiguredMultiblockPort port
    ) {
        BlockPos.STREAM_CODEC.encode(buffer, port.face().relativePos());
        ByteBufCodecs.BYTE.encode(buffer, (byte) port.face().face().ordinal());
        ByteBufCodecs.BYTE.encode(buffer, (byte) port.type().ordinal());
        ByteBufCodecs.BYTE.encode(buffer, (byte) port.mode().ordinal());
        ResourceLocation.STREAM_CODEC.encode(buffer, port.target().id());
        ByteBufCodecs.BOOL.encode(buffer, port.target().group());
    }

    /**
     * Decodes one configured port.
     *
     * @param buffer network buffer
     * @return decoded configured port
     */
    private static ConfiguredMultiblockPort decodePort(final ByteBuf buffer) {
        final BlockPos relativePos = BlockPos.STREAM_CODEC.decode(buffer);
        final Direction face = Direction.values()[ByteBufCodecs.BYTE.decode(buffer)];
        final MultiblockPortType type = MultiblockPortType.values()[ByteBufCodecs.BYTE.decode(buffer)];
        final MultiblockPortMode mode = MultiblockPortMode.values()[ByteBufCodecs.BYTE.decode(buffer)];
        final ResourceLocation targetId = ResourceLocation.STREAM_CODEC.decode(buffer);
        final boolean targetGroup = ByteBufCodecs.BOOL.decode(buffer);

        return new ConfiguredMultiblockPort(
                new MultiblockPortFace(
                        relativePos,
                        face
                ),
                type,
                mode,
                new MultiblockPortTarget(
                        targetId,
                        targetGroup
                )
        );
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Applies the port sync on the client menu.
     *
     * @param context client networking context
     */
    public void apply(final ClientPlayNetworking.Context context) {
        if (context.client().player == null) {
            return;
        }

        if (!(context.client().player.containerMenu instanceof MultiblockConfiguredMenu menu)) {
            return;
        }

        if (!menu.instanceId.equals(this.instanceId)) {
            return;
        }

        menu.applyClientPortSync(this.ports);
    }
}