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

package dev.galacticraft.machinelib.impl.network;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.block.face.BlockFace;
import dev.galacticraft.machinelib.api.block.face.MachineIOFace;
import dev.galacticraft.machinelib.api.fluid.FluidStack;
import dev.galacticraft.machinelib.api.machine.AccessLevel;
import dev.galacticraft.machinelib.api.machine.RedstoneActivation;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.storage.ResourceStorage;
import dev.galacticraft.machinelib.api.storage.io.ResourceFlow;
import dev.galacticraft.machinelib.api.storage.io.ResourceType;
import dev.galacticraft.machinelib.api.storage.io.StorageSelection;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
import dev.galacticraft.machinelib.api.util.GenericApiUtil;
import dev.galacticraft.machinelib.client.api.screen.Tank;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.storage.slot.InputType;
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

import java.util.List;

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
                    if (player.containerMenu instanceof MachineMenu<?> sHandler) {
                        MachineBlockEntity machine = sHandler.machine;
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
                            machineFace.setSelection(null);
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
                    if (player.containerMenu instanceof MachineMenu<?> sHandler) {
                        MachineBlockEntity machine = sHandler.machine;
                        if (machine.getSecurity().hasAccess(player)) {
                            ServerLevel level = (ServerLevel) machine.getLevel();
                            BlockPos pos = machine.getBlockPos();
                            assert level != null;

                            MachineIOFace machineFace = machine.getIOConfig().get(face);
                            machineFace.setOption(Constant.Cache.RESOURCE_TYPES[type], ResourceFlow.VALUES[flow]);
                            machineFace.setSelection(null);
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

        C2SPacketReceiver.register(Constant.id("match_slot"), (server, player, handler, buf, responseSender) -> {
            byte b = buf.readByte();
            int slot = buf.readInt();

            if (b >= 0 && b < Constant.Cache.BLOCK_FACES.length && slot >= 0) {
                BlockFace face = Constant.Cache.BLOCK_FACES[b];
                server.execute(() -> {
                    if (player.containerMenu instanceof MachineMenu<?> sHandler) {
                        MachineBlockEntity machine = sHandler.machine;
                        if (machine.getSecurity().hasAccess(player)) {
                            MachineIOFace machineFace = machine.getIOConfig().get(face);
                            if (machineFace.getType().matchesSlots() && machineFace.getSelection() != null) {
                                ResourceStorage<?, ?, ?, ?> storage = machine.getResourceStorage(machineFace.getType());
                                if (storage != null) {
                                    SlotGroupType type = machineFace.getSelection().getGroup();
                                    SlotGroup<?, ?, ?> group = storage.getGroup(type);
                                    if (slot < group.size()) {
                                        machineFace.setSelection(StorageSelection.create(type, slot));
                                        machine.setChanged();
                                    } else if (slot == group.size()) {
                                        machineFace.setSelection(StorageSelection.create(type));
                                        machine.setChanged();
                                    }
                                }
                            }
                        }
                    }
                });
            }
        });

        C2SPacketReceiver.register(Constant.id("match_group"), (server, player, handler, buf, responseSender) -> {
            byte b = buf.readByte();
            int group = buf.readInt();

            if (b >= 0 && b < Constant.Cache.BLOCK_FACES.length && group >= 0) {
                BlockFace face = Constant.Cache.BLOCK_FACES[b];
                server.execute(() -> {
                    if (player.containerMenu instanceof MachineMenu<?> sHandler) {
                        MachineBlockEntity machine = sHandler.machine;
                        if (machine.getSecurity().hasAccess(player)) {
                            MachineIOFace machineFace = machine.getIOConfig().get(face);
                            if (machineFace.getType().matchesGroups()) {
                                List<SlotGroupType> groups = machineFace.getFlowMatchingGroups(machine);
                                if (group < groups.size()) {
                                    machineFace.setSelection(StorageSelection.create(groups.get(group)));
                                    machine.setChanged();
                                }
                            }
                        }
                    }
                });
            }
        });

        C2SPacketReceiver.register(Constant.id("redstone_config"), (server, player, handler, buf, responseSender) -> {
            RedstoneActivation redstoneActivation = RedstoneActivation.values()[buf.readByte()];
            server.execute(() -> {
                if (player.containerMenu instanceof MachineMenu<?> sHandler) {
                    MachineBlockEntity machine = sHandler.machine;
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
                if (player.containerMenu instanceof MachineMenu<?> sHandler) {
                    MachineBlockEntity machine = sHandler.machine;
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
                if (player.containerMenu instanceof MachineMenu<?> sHandler) {
                    if (sHandler.containerId == syncId) {
                        acceptStack(sHandler.tanks.get(index), ContainerItemContext.ofPlayerCursor(player, player.containerMenu));
                    }
                }
            });
        });
    }

    private static boolean acceptStack(@NotNull Tank tank, @NotNull ContainerItemContext context) {
        Storage<FluidVariant> storage = context.find(FluidStorage.ITEM);
        if (storage != null) {
            ResourceSlot<Fluid, FluidStack> slot = tank.getSlot();
            InputType type = tank.getInputType();
            if (storage.supportsExtraction() && type.playerInsertion()) {
                    FluidVariant storedResource;
                    if (tank.isEmpty()) {
                        storedResource = StorageUtil.findStoredResource(storage, variant -> slot.getFilter().test(variant.getFluid(), variant.getNbt()));
                    } else {
                        storedResource = tank.createVariant();
                    }
                    if (storedResource != null) {
                        return GenericApiUtil.move(storedResource, storage, slot, Long.MAX_VALUE, null) != 0;
                    }
            } else if (storage.supportsInsertion() && type.playerExtraction()) {
                FluidVariant storedResource = tank.createVariant();
                if (!storedResource.isBlank()) {
                    return GenericApiUtil.move(storedResource, slot, storage, Long.MAX_VALUE, null) != 0;
                }

            }
        }
        return false;
    }
}
