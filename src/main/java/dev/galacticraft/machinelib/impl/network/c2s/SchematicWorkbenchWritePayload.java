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