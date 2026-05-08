package dev.galacticraft.machinelib.impl.network.c2s;

import dev.galacticraft.machinelib.api.multiblock.MultiblockConfiguredMenu;
import dev.galacticraft.machinelib.api.multiblock.components.MultiblockPortComponent;
import dev.galacticraft.machinelib.api.multiblock.port.ConfiguredMultiblockPort;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortFace;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortMode;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortTarget;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortType;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.multiblock.FormedMultiblockMachine;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Client-to-server packet used to update one configured multiblock port.
 */
public record MultiblockPortConfigUpdatePayload(
        UUID instanceId,
        BlockPos relativePos,
        Direction face,
        Action action,
        MultiblockPortType portType,
        MultiblockPortMode mode,
        ResourceLocation targetId,
        boolean targetGroup
) implements CustomPacketPayload {

    public static final Type<MultiblockPortConfigUpdatePayload> TYPE =
            new Type<>(Constant.id("multiblock_port_config_update"));

    public static final StreamCodec<ByteBuf, UUID> UUID_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_LONG,
                    UUID::getMostSignificantBits,
                    ByteBufCodecs.VAR_LONG,
                    UUID::getLeastSignificantBits,
                    UUID::new
            );

    private static final StreamCodec<ByteBuf, Direction> DIRECTION_CODEC =
            ByteBufCodecs.BYTE.map(
                    value -> value == -1 ? null : Direction.values()[value],
                    direction -> direction == null ? -1 : (byte) direction.ordinal()
            );

    private static final StreamCodec<ByteBuf, Action> ACTION_CODEC =
            ByteBufCodecs.BYTE.map(
                    value -> value == -1 ? null : Action.values()[value],
                    action -> action == null ? -1 : (byte) action.ordinal()
            );

    private static final StreamCodec<ByteBuf, MultiblockPortType> TYPE_CODEC =
            ByteBufCodecs.BYTE.map(
                    value -> value == -1 ? null : MultiblockPortType.values()[value],
                    type -> type == null ? -1 : (byte) type.ordinal()
            );

    private static final StreamCodec<ByteBuf, MultiblockPortMode> MODE_CODEC =
            ByteBufCodecs.BYTE.map(
                    value -> value == -1 ? null : MultiblockPortMode.values()[value],
                    mode -> mode == null ? -1 : (byte) mode.ordinal()
            );

    public static final StreamCodec<ByteBuf, MultiblockPortConfigUpdatePayload> CODEC =
            StreamCodec.of(
                    MultiblockPortConfigUpdatePayload::encode,
                    MultiblockPortConfigUpdatePayload::decode
            );

    private static void encode(
            final ByteBuf buffer,
            final MultiblockPortConfigUpdatePayload payload
    ) {
        UUID_CODEC.encode(buffer, payload.instanceId);
        BlockPos.STREAM_CODEC.encode(buffer, payload.relativePos);
        DIRECTION_CODEC.encode(buffer, payload.face);
        ACTION_CODEC.encode(buffer, payload.action);
        TYPE_CODEC.encode(buffer, payload.portType);
        MODE_CODEC.encode(buffer, payload.mode);
        ResourceLocation.STREAM_CODEC.encode(buffer, payload.targetId);
        ByteBufCodecs.BOOL.encode(buffer, payload.targetGroup);
    }

    private static MultiblockPortConfigUpdatePayload decode(final ByteBuf buffer) {
        return new MultiblockPortConfigUpdatePayload(
                UUID_CODEC.decode(buffer),
                BlockPos.STREAM_CODEC.decode(buffer),
                DIRECTION_CODEC.decode(buffer),
                ACTION_CODEC.decode(buffer),
                TYPE_CODEC.decode(buffer),
                MODE_CODEC.decode(buffer),
                ResourceLocation.STREAM_CODEC.decode(buffer),
                ByteBufCodecs.BOOL.decode(buffer)
        );
    }

    /**
     * Creates a packet that sets one configured port.
     *
     * @param instanceId formed multiblock instance id
     * @param port configured port
     * @return update packet
     */
    public static MultiblockPortConfigUpdatePayload set(
            final UUID instanceId,
            final ConfiguredMultiblockPort port
    ) {
        return new MultiblockPortConfigUpdatePayload(
                instanceId,
                port.face().relativePos(),
                port.face().face(),
                Action.SET,
                port.type(),
                port.mode(),
                port.target().id(),
                port.target().group()
        );
    }

    /**
     * Creates a packet that removes the configured port on one face.
     *
     * @param instanceId formed multiblock instance id
     * @param face port face
     * @return remove packet
     */
    public static MultiblockPortConfigUpdatePayload remove(
            final UUID instanceId,
            final MultiblockPortFace face
    ) {
        return new MultiblockPortConfigUpdatePayload(
                instanceId,
                face.relativePos(),
                face.face(),
                Action.REMOVE,
                null,
                null,
                Constant.id("empty"),
                false
        );
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Applies the port update on the logical server.
     *
     * @param context packet context
     */
    public void apply(final ServerPlayNetworking.Context context) {
        if (!(context.player().containerMenu instanceof MultiblockConfiguredMenu menu)) {
            return;
        }

        if (!menu.instanceId.equals(this.instanceId)) {
            return;
        }

        final FormedMultiblockMachine machine = menu.machine();

        if (machine == null || !machine.isFormed()) {
            return;
        }

        if (!machine.instanceId().equals(this.instanceId)) {
            return;
        }

        if (!menu.canModifyPortConfiguration(context.player())) {
            return;
        }

        final MultiblockPortComponent ports = machine.component(MultiblockPortComponent.class);

        if (ports == null) {
            return;
        }

        final MultiblockPortFace portFace = new MultiblockPortFace(
                this.relativePos,
                this.face
        );

        if (this.action == Action.REMOVE) {
            if (ports.removePort(portFace)) {
                machine.setComponentsChanged();
                menu.syncPortsToClient();
            }

            return;
        }

        if (this.portType == null || this.mode == null || this.targetId == null) {
            return;
        }

        final ConfiguredMultiblockPort port = new ConfiguredMultiblockPort(
                portFace,
                this.portType,
                this.mode,
                new MultiblockPortTarget(
                        this.targetId,
                        this.targetGroup
                )
        );

        if (ports.setPort(port)) {
            machine.setComponentsChanged();
            menu.syncPortsToClient();
        }
    }

    /**
     * Port update action.
     */
    public enum Action {
        SET,
        REMOVE
    }
}