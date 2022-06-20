/*
 * Copyright (c) 2021-2022 Team Galacticraft
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

package dev.galacticraft.impl.client.network;

import com.mojang.authlib.GameProfile;
import dev.galacticraft.api.block.entity.MachineBlockEntity;
import dev.galacticraft.api.machine.AccessLevel;
import dev.galacticraft.api.machine.RedstoneActivation;
import dev.galacticraft.api.screen.MachineScreenHandler;
import dev.galacticraft.impl.MLConstant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class MachineLibS2CPackets {
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(new Identifier(MLConstant.MOD_ID, "storage_sync"), (client, handler, buf, responseSender) -> {
            PacketByteBuf packet = PacketByteBufs.copy(buf);
            client.execute(() -> {
                if (client.player.currentScreenHandler instanceof MachineScreenHandler<?> machineHandler) {
                    if (machineHandler.syncId == packet.readByte()) {
                        machineHandler.receiveState(packet);
                    }
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(new Identifier(MLConstant.MOD_ID, "security_update"), (client, handler, buf, responseSender) -> { //todo(marcus): 1.17?
            BlockPos pos = buf.readBlockPos();
            AccessLevel accessLevel = AccessLevel.values()[buf.readByte()];
            GameProfile profile = NbtHelper.toGameProfile(Objects.requireNonNull(buf.readNbt()));

            client.execute(() -> {
                assert client.world != null;
                BlockEntity entity = client.world.getBlockEntity(pos);
                if (entity instanceof MachineBlockEntity machine) {
                    assert profile != null;
                    assert accessLevel != null;
                    machine.getSecurity().setOwner(profile);
                    machine.getSecurity().setAccessLevel(accessLevel);

                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(new Identifier(MLConstant.MOD_ID, "redstone_update"), (client, handler, buf, responseSender) -> { //todo(marcus): 1.17?
            BlockPos pos = buf.readBlockPos();
            RedstoneActivation redstone = RedstoneActivation.values()[buf.readByte()];

            client.execute(() -> {
                assert client.world != null;
                BlockEntity entity = client.world.getBlockEntity(pos);
                if (entity instanceof MachineBlockEntity) {
                    assert redstone != null;
                    ((MachineBlockEntity) entity).setRedstone(redstone);
                }
            });
        });
    }
}
