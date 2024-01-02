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

package dev.galacticraft.machinelib.impl.network;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.configuration.AccessLevel;
import dev.galacticraft.machinelib.api.machine.configuration.MachineIOFace;
import dev.galacticraft.machinelib.api.machine.configuration.RedstoneActivation;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.transfer.InputType;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.api.util.StorageHelper;
import dev.galacticraft.machinelib.client.api.screen.Tank;
import dev.galacticraft.machinelib.impl.Constant;
import io.netty.buffer.ByteBufAllocator;
import lol.bai.badpackets.api.C2SPacketReceiver;
import lol.bai.badpackets.api.PacketSender;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class MachineLibC2SPackets {
    private MachineLibC2SPackets() {
    }

    public static void register() {
        C2SPacketReceiver.register(Constant.id("reset_face"), (server, player, handler, buf, responseSender) -> {
            byte f = buf.readByte();
            boolean type = buf.readBoolean();

            if (f >= 0 && f < Constant.Cache.BLOCK_FACES.length) {
                BlockFace face = Constant.Cache.BLOCK_FACES[f];
                server.execute(() -> {
                    if (player.containerMenu instanceof MachineMenu<?> menu) {
                        MachineBlockEntity machine = menu.machine;
                        if (machine.getSecurity().hasAccess(player)) {
                            MachineIOFace machineFace = machine.getIOConfig().get(face);
                            ServerLevel level = (ServerLevel) machine.getLevel();
                            assert level != null;

                            BlockPos pos = machine.getBlockPos();
                            if (type) {
                                machineFace.setOption(ResourceType.NONE, ResourceFlow.BOTH);
                                FriendlyByteBuf buffer = new FriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer(Long.BYTES + 1)
                                        .writeLong(pos.asLong()).writeByte(f)
                                );
                                for (ServerPlayer tracking : level.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false)) {
                                    PacketSender.s2c(tracking).send(Constant.id("reset_face"), buffer);
                                }
                            }
                            machine.setChanged();
                            BlockState state = level.getBlockState(pos);
                            level.neighborChanged(pos.relative(face.toDirection(state.getValue(BlockStateProperties.HORIZONTAL_FACING))), state.getBlock(), pos);
                        }
                    }
                });
            }
        });

        C2SPacketReceiver.register(Constant.id("face_type"), (server, player, handler, buf, responseSender) -> {
            byte f = buf.readByte();
            byte type = buf.readByte();
            byte flow = buf.readByte();

            if (f >= 0 && f < Constant.Cache.BLOCK_FACES.length
                    && type >= 0 && type < Constant.Cache.RESOURCE_TYPES.length
                    && flow >= 0 && flow < ResourceFlow.VALUES.length
            ) {
                BlockFace face = Constant.Cache.BLOCK_FACES[f];
                server.execute(() -> {
                    if (player.containerMenu instanceof MachineMenu<?> menu) {
                        MachineBlockEntity machine = menu.machine;
                        if (machine.getSecurity().hasAccess(player) && !menu.isFaceLocked(face)) {
                            ServerLevel level = (ServerLevel) machine.getLevel();
                            BlockPos pos = machine.getBlockPos();
                            assert level != null;

                            MachineIOFace machineFace = machine.getIOConfig().get(face);
                            machineFace.setOption(Constant.Cache.RESOURCE_TYPES[type], ResourceFlow.VALUES[flow]);
                            machine.setChanged();
                            BlockState state = level.getBlockState(pos);
                            level.neighborChanged(pos.relative(face.toDirection(state.getValue(BlockStateProperties.HORIZONTAL_FACING))), state.getBlock(), pos);

                            FriendlyByteBuf buffer = new FriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer(Long.BYTES + 3)
                                    .writeLong(pos.asLong()).writeByte(f).writeByte(type).writeByte(flow)
                            );
                            for (ServerPlayer tracking : level.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false)) {
                                PacketSender.s2c(tracking).send(Constant.id("face_type"), buffer);
                            }
                        }
                    }
                });
            }
        });

        C2SPacketReceiver.register(Constant.id("redstone_config"), (server, player, handler, buf, responseSender) -> {
            RedstoneActivation redstoneActivation = RedstoneActivation.values()[buf.readByte()];
            server.execute(() -> {
                if (player.containerMenu instanceof MachineMenu<?> menu) {
                    MachineBlockEntity machine = menu.machine;
                    if (machine.getSecurity().hasAccess(player)) {
                        machine.setRedstone(redstoneActivation);
                        machine.setChanged();
                    }
                }
            });
        });

        C2SPacketReceiver.register(Constant.id("security_config"), (server, player, handler, buf, responseSender) -> {
            AccessLevel accessLevel = AccessLevel.values()[buf.readByte()];
            server.execute(() -> {
                if (player.containerMenu instanceof MachineMenu<?> menu) {
                    MachineBlockEntity machine = menu.machine;
                    if (machine.getSecurity().isOwner(player)) {
                        machine.getSecurity().setAccessLevel(accessLevel);
                        machine.setChanged();
                    }
                }
            });
        });

        C2SPacketReceiver.register(Constant.id("tank_modify"), (server, player, handler, buf, responseSender) -> {
            int syncId = buf.readVarInt();
            int index = buf.readInt();
            server.execute(() -> {
                if (player.containerMenu instanceof MachineMenu<?> menu) {
                    if (menu.containerId == syncId) {
                        acceptStack(menu.tanks.get(index), ContainerItemContext.ofPlayerCursor(player, player.containerMenu));
                    }
                }
            });
        });
    }

    private static boolean acceptStack(@NotNull Tank tank, @NotNull ContainerItemContext context) {
        Storage<FluidVariant> storage = context.find(FluidStorage.ITEM);
        if (storage != null) {
            ResourceSlot<Fluid> slot = tank.getSlot();
            InputType type = tank.getInputType();
            if (storage.supportsExtraction() && type.playerInsertion()) {
                    FluidVariant storedResource;
                    if (tank.isEmpty()) {
                        storedResource = StorageUtil.findStoredResource(storage, variant -> slot.getFilter().test(variant.getFluid(), variant.getNbt()));
                    } else {
                        storedResource = tank.createVariant();
                    }
                    if (storedResource != null) {
                        return StorageHelper.move(storedResource, storage, slot, Long.MAX_VALUE, null) != 0;
                    }
            } else if (storage.supportsInsertion() && type.playerExtraction()) {
                FluidVariant storedResource = tank.createVariant();
                if (!storedResource.isBlank()) {
                    return StorageHelper.move(storedResource, slot, storage, Long.MAX_VALUE, null) != 0;
                }

            }
        }
        return false;
    }
}
