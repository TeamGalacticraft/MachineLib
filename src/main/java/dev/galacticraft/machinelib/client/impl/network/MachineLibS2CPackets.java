/*
 * Copyright (c) 2021-2024 Team Galacticraft
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

package dev.galacticraft.machinelib.client.impl.network;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.configuration.MachineIOFace;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.impl.Constant;
import lol.bai.badpackets.api.S2CPacketReceiver;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class MachineLibS2CPackets {
    private MachineLibS2CPackets() {
    }

    public static void register() {
        S2CPacketReceiver.register(Constant.id("storage_sync"), (client, handler, buf, responseSender) -> {
            FriendlyByteBuf packet = new FriendlyByteBuf(buf.copy());
            client.execute(() -> {
                if (client.player.containerMenu instanceof MachineMenu<?> menu) {
                    if (menu.containerId == packet.readByte()) {
                        menu.receiveState(packet);
                    }
                }
            });
        });

        S2CPacketReceiver.register(Constant.id("reset_face"), (client, handler, buf, responseSender) -> {
            BlockPos pos = BlockPos.of(buf.readLong());
            byte f = buf.readByte();

            if (f >= 0 && f < Constant.Cache.BLOCK_FACES.length) {
                BlockFace face = Constant.Cache.BLOCK_FACES[f];
                client.execute(() -> {
                    if (client.level != null && client.level.isLoaded(pos)) {
                        if (client.level.getBlockEntity(pos) instanceof MachineBlockEntity machine) {
                            MachineIOFace machineFace = machine.getIOConfig().get(face);
                            machineFace.setOption(ResourceType.NONE, ResourceFlow.BOTH);
                            client.levelRenderer.blockChanged(client.level, pos, machine.getBlockState(), machine.getBlockState(), 0);
                        }
                    }
                });
            }
        });

        S2CPacketReceiver.register(Constant.id("face_type"), (client, handler, buf, responseSender) -> {
            BlockPos pos = BlockPos.of(buf.readLong());
            byte f = buf.readByte();
            byte type = buf.readByte();
            byte flow = buf.readByte();

            if (f >= 0 && f < Constant.Cache.BLOCK_FACES.length
                    && type >= 0 && type < Constant.Cache.RESOURCE_TYPES.length
                    && flow >= 0 && flow < ResourceFlow.VALUES.length
            ) {
                BlockFace face = Constant.Cache.BLOCK_FACES[f];
                client.execute(() -> {
                    if (client.level != null && client.level.isLoaded(pos)) {
                        if (client.level.getBlockEntity(pos) instanceof MachineBlockEntity machine) {
                            MachineIOFace machineFace = machine.getIOConfig().get(face);
                            machineFace.setOption(Constant.Cache.RESOURCE_TYPES[type], ResourceFlow.VALUES[flow]);
                            client.levelRenderer.blockChanged(client.level, pos, machine.getBlockState(), machine.getBlockState(), 0);
                        }
                    }
                });
            }
        });


        //todo: badpackets?
        ClientPlayNetworking.registerGlobalReceiver(Constant.id("machine_sync"), (client, handler, buffer, responseSender) -> {
            FriendlyByteBuf buf = new FriendlyByteBuf(buffer.copy());
            client.execute(() -> {
                BlockPos pos = buf.readBlockPos();
                if (client.level != null && client.level.isLoaded(pos)) {
                    if (client.level.getBlockEntity(pos) instanceof MachineBlockEntity machine) {
                        machine.readClientSyncData(new FriendlyByteBuf(buf));
                        client.levelRenderer.blockChanged(client.level, pos, machine.getBlockState(), machine.getBlockState(), 8);
                    }
                }
            });
        });
    }
}
