/*
 * Copyright (c) 2021-2023 Team Galacticraft
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
import dev.galacticraft.machinelib.api.block.face.BlockFace;
import dev.galacticraft.machinelib.api.block.face.MachineIOFace;
import dev.galacticraft.machinelib.api.machine.AccessLevel;
import dev.galacticraft.machinelib.api.machine.RedstoneActivation;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.storage.io.ResourceFlow;
import dev.galacticraft.machinelib.api.storage.io.ResourceType;
import dev.galacticraft.machinelib.impl.Constant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class MachineLibS2CPackets {
    private MachineLibS2CPackets() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(Constant.id("storage_sync"), (client, handler, buf, responseSender) -> {
            FriendlyByteBuf packet = PacketByteBufs.copy(buf);
            client.execute(() -> {
                if (client.player.containerMenu instanceof MachineMenu<?> machineHandler) {
                    if (machineHandler.containerId == packet.readByte()) {
                        machineHandler.receiveState(packet);
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(Constant.id("reset_face"), (client, handler, buf, responseSender) -> {
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

        ClientPlayNetworking.registerGlobalReceiver(Constant.id("face_type"), (client, handler, buf, responseSender) -> {
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
                            machineFace.setSelection(null);
                            client.levelRenderer.blockChanged(client.level, pos, machine.getBlockState(), machine.getBlockState(), 0);
                        }
                    }
                });
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(Constant.id("security_update"), (client, handler, buf, responseSender) -> { //todo(marcus): 1.17?
            BlockPos pos = buf.readBlockPos();
            AccessLevel accessLevel = AccessLevel.values()[buf.readByte()];
            UUID uuid = buf.readUUID();
            String username = null;
            ResourceLocation team = null;
            String teamName = null;

            if (buf.readBoolean()) username = buf.readUtf();
            if (buf.readBoolean()) team = new ResourceLocation(buf.readUtf());
            if (buf.readBoolean()) teamName = buf.readUtf();


            String finalUsername = username; // for lambda capture
            ResourceLocation finalTeam = team;
            String finalTeamName = teamName;
            client.execute(() -> {
                assert client.level != null;
                BlockEntity entity = client.level.getBlockEntity(pos);
                if (entity instanceof MachineBlockEntity machine) {
                    assert uuid != null;
                    assert accessLevel != null;
                    machine.getSecurity().setOwner(uuid, finalUsername);
                    machine.getSecurity().setTeam(finalTeam, finalTeamName);
                    machine.getSecurity().setAccessLevel(accessLevel);

                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(Constant.id("redstone_update"), (client, handler, buf, responseSender) -> { //todo(marcus): 1.17?
            BlockPos pos = buf.readBlockPos();
            RedstoneActivation redstone = RedstoneActivation.values()[buf.readByte()];

            client.execute(() -> {
                assert client.level != null;
                BlockEntity entity = client.level.getBlockEntity(pos);
                if (entity instanceof MachineBlockEntity) {
                    assert redstone != null;
                    ((MachineBlockEntity) entity).setRedstone(redstone);
                }
            });
        });
    }
}
