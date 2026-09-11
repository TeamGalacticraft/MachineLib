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

package dev.galacticraft.machinelib.impl.menu;

import dev.galacticraft.machinelib.api.menu.MenuData;
import dev.galacticraft.machinelib.api.misc.DeltaPacketSerializable;
import dev.galacticraft.machinelib.impl.network.s2c.MenuSyncPayload;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MenuDataImpl extends MenuData {
    private final ServerPlayer player;
    /**
     * The delta values of the data to be synchronized. Indices correspond to the data list.
     */
    private final List<? super Object> delta;

    public MenuDataImpl(ServerPlayer player, int syncId) {
        super(syncId);
        this.player = player;
        this.delta = new ArrayList<>();
    }

    @Override
    public <T> void register(@NotNull DeltaPacketSerializable<? super RegistryFriendlyByteBuf, T> datum) {
        super.register(datum);
        this.delta.add(datum.createEquivalent());
    }

    @Override
    public void synchronize() {
        int size = this.data.size();

        int n = 0;
        for (int i = 0; i < size; i++) {
            var key = (DeltaPacketSerializable<? super RegistryFriendlyByteBuf, ? super Object>) this.data.get(i);
            if (key.hasChanged(this.delta.get(i))) n++;
        }

        if (n == size) {
            synchronizeFull();
            return;
        }

        if (n > 0) {
            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), this.player.registryAccess());
            buf.writeVarInt(this.syncId);
            buf.writeByte(n);

            for (int i = 0; i < size; i++) {
                var key = (DeltaPacketSerializable<? super RegistryFriendlyByteBuf, ? super Object>) this.data.get(i);
                Object data = this.delta.get(i);

                if (key.hasChanged(data)) {
                    buf.writeByte(i);
                    key.writeDeltaPacket(buf, data);
                    key.copyInto(data);
                }
            }

            ServerPlayNetworking.getSender(this.player).sendPacket(new MenuSyncPayload(buf));
        }
    }

    @Override
    public void synchronizeFull() {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), this.player.registryAccess());
        buf.writeVarInt(this.syncId);
        buf.writeByte(this.data.size());
        for (int i = 0; i < this.data.size(); i++) {
            var datum = (DeltaPacketSerializable<? super RegistryFriendlyByteBuf, ? super Object>) this.data.get(i);
            datum.writePacket(buf);
            datum.copyInto(this.delta.get(i));
        }

        ServerPlayNetworking.getSender(this.player).sendPacket(new MenuSyncPayload(buf));
    }

    @Override
    public void synchronizeInitial(RegistryFriendlyByteBuf buf) {
        for (int i = 0; i < this.data.size(); i++) {
            var datum = (DeltaPacketSerializable<? super RegistryFriendlyByteBuf, ? super Object>) this.data.get(i);
            datum.writePacket(buf);
            datum.copyInto(this.delta.get(i));
        }
    }

    @Override
    public void handle(RegistryFriendlyByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handleInitial(RegistryFriendlyByteBuf buf) {
        throw new UnsupportedOperationException();
    }
}
