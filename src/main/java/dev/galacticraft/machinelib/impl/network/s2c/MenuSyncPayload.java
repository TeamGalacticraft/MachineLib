/*
 * Copyright (c) 2021-2026 Team Galacticraft
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

import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.MachineLib;
import dev.galacticraft.machinelib.impl.util.Utils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record MenuSyncPayload(RegistryFriendlyByteBuf buf) implements CustomPacketPayload {
    public static final Type<MenuSyncPayload> TYPE = new Type<>(Constant.id("menu_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MenuSyncPayload> CODEC = Utils.BUF_IDENTITY_CODEC.map(MenuSyncPayload::new, MenuSyncPayload::buf);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void apply(ClientPlayNetworking.Context context) {
        int syncId = this.buf.readVarInt();
        LocalPlayer player = context.player();
        if (player != null) {
            if (player.containerMenu instanceof MachineMenu<?> menu && syncId == menu.containerId) {
                menu.getData().handle(this.buf);
            } else {
                if (player.containerMenu.containerId != 0) {
                    MachineLib.LOGGER.warn("Received menu sync packet for invalid menu ID: {} (active: {})", syncId, player.containerMenu.containerId);
                } else {
                    MachineLib.LOGGER.debug("Received menu sync packet for '{}' with no menu open", syncId);
                }
            }
        }
    }
}
