package dev.galacticraft.machinelib.impl.network.c2s;

import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.schematic.menu.SchematicWorkbenchMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client-to-server payload used by the schematic workbench write button.
 *
 * @param containerId open menu id
 * @param multiblockId selected multiblock id
 */
public record SchematicWorkbenchWritePayload(
        int containerId,
        ResourceLocation multiblockId
) implements CustomPacketPayload {
    public static final Type<SchematicWorkbenchWritePayload> TYPE =
            new Type<>(Constant.id("schematic_workbench_write"));

    public static final StreamCodec<FriendlyByteBuf, SchematicWorkbenchWritePayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeVarInt(payload.containerId);
                        buf.writeResourceLocation(payload.multiblockId);
                    },
                    buf -> new SchematicWorkbenchWritePayload(
                            buf.readVarInt(),
                            buf.readResourceLocation()
                    )
            );

    /**
     * Handles this packet on the server.
     *
     * @param payload received payload
     * @param context packet context
     */
    public static void apply(
            final SchematicWorkbenchWritePayload payload,
            final net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context context
    ) {
        context.player().server.execute(() -> {
            if (context.player().containerMenu instanceof SchematicWorkbenchMenu menu
                    && menu.containerId == payload.containerId()) {
                menu.writeSelectedSchematic(payload.multiblockId());
            }
        });
    }

    /**
     * {@return packet type}
     */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}