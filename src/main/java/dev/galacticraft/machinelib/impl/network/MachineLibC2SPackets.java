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

package dev.galacticraft.machinelib.impl.network;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.block.face.BlockFace;
import dev.galacticraft.machinelib.api.block.face.MachineIOFaceConfig;
import dev.galacticraft.machinelib.api.machine.AccessLevel;
import dev.galacticraft.machinelib.api.machine.RedstoneActivation;
import dev.galacticraft.machinelib.api.screen.MachineScreenHandler;
import dev.galacticraft.machinelib.api.storage.exposed.ExposedSlot;
import dev.galacticraft.machinelib.api.storage.io.ConfiguredStorage;
import dev.galacticraft.machinelib.api.storage.io.ResourceFlow;
import dev.galacticraft.machinelib.api.storage.io.ResourceType;
import dev.galacticraft.machinelib.api.storage.io.StorageSelection;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.transfer.GenericStorageUtil;
import dev.galacticraft.machinelib.client.api.screen.Tank;
import dev.galacticraft.machinelib.impl.Constant;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class MachineLibC2SPackets {
    private MachineLibC2SPackets() {}

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(Constant.id("reset_face"), (server, player, handler, buf, responseSender) -> {
            byte b = buf.readByte();
            boolean type = buf.readBoolean();

            if (b >= 0 && b < Constant.Cache.BLOCK_FACES.length) {
                BlockFace face = Constant.Cache.BLOCK_FACES[b];
                server.execute(() -> {
                    if (player.containerMenu instanceof MachineScreenHandler<?> sHandler) {
                        MachineBlockEntity machine = sHandler.machine;
                        if (machine.getSecurity().hasAccess(player)) {
                            MachineIOFaceConfig machineFace = machine.getIOConfig().get(face);
                            if (type) machineFace.setOption(ResourceType.NONE, ResourceFlow.BOTH);
                            machineFace.setSelection(null);
                        }
                    }
                });
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(Constant.id("face_type"), (server, player, handler, buf, responseSender) -> {
            byte b = buf.readByte();
            byte type = buf.readByte();
            byte flow = buf.readByte();

            if (b >= 0 && b < Constant.Cache.BLOCK_FACES.length
                    && type >= 0 && type < Constant.Cache.RESOURCE_TYPES.length
                    && flow >= 0 && flow < ResourceFlow.VALUES.size()
            ) {
                BlockFace face = Constant.Cache.BLOCK_FACES[b];
                server.execute(() -> {
                    if (player.containerMenu instanceof MachineScreenHandler<?> sHandler) {
                        MachineBlockEntity machine = sHandler.machine;
                        if (machine.getSecurity().hasAccess(player)) {
                            MachineIOFaceConfig machineFace = machine.getIOConfig().get(face);
                            machineFace.setOption(Constant.Cache.RESOURCE_TYPES[type], ResourceFlow.VALUES.get(flow));
                            machineFace.setSelection(null);
                        }
                    }
                });
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(Constant.id("match_slot"), (server, player, handler, buf, responseSender) -> {
            byte b = buf.readByte();
            int slot = buf.readInt();

            if (b >= 0 && b < Constant.Cache.BLOCK_FACES.length && slot >= 0) {
                BlockFace face = Constant.Cache.BLOCK_FACES[b];
                server.execute(() -> {
                    if (player.containerMenu instanceof MachineScreenHandler<?> sHandler) {
                        MachineBlockEntity machine = sHandler.machine;
                        if (machine.getSecurity().hasAccess(player)) {
                            MachineIOFaceConfig machineFace = machine.getIOConfig().get(face);
                            if (machineFace.getType().matchesSlots()) {
                                machineFace.setSelection(null);
                                ConfiguredStorage storage = machine.getStorage(machineFace.getType());
                                if (storage != null) {
                                    int[] matching = machineFace.getMatching(storage);
                                    if (matching.length == 0) {
                                        return;
                                    }

                                    int count = 0;
                                    for (int j : matching) {
                                        ResourceFlow flow;
                                        if (storage.canExposedInsert(j)) {
                                            if (storage.canExposedExtract(j)) {
                                                flow = ResourceFlow.BOTH;
                                            } else {
                                                flow = ResourceFlow.INPUT;
                                            }
                                        } else if (storage.canExposedExtract(j)) {
                                            flow = ResourceFlow.OUTPUT;
                                        } else {
                                            continue;
                                        }
                                        if (flow.canFlowIn(machineFace.getFlow())) count++;
                                    }
                                    if (count == 0) return;
                                    int[] tmp = new int[count];

                                    int c = 0;
                                    for (int j : matching) {
                                        ResourceFlow flow;
                                        if (storage.canExposedInsert(j)) {
                                            if (storage.canExposedExtract(j)) {
                                                flow = ResourceFlow.BOTH;
                                            } else {
                                                flow = ResourceFlow.INPUT;
                                            }
                                        } else if (storage.canExposedExtract(j)) {
                                            flow = ResourceFlow.OUTPUT;
                                        } else {
                                            continue;
                                        }
                                        if (flow.canFlowIn(machineFace.getFlow())) tmp[c++] = j;
                                    }
                                    matching = tmp;

                                    if (slot < matching.length) {
                                        machineFace.setSelection(StorageSelection.createSlot(matching[slot]));
                                    }
                                }
                            }
                        }
                    }
                });
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(Constant.id("match_group"), (server, player, handler, buf, responseSender) -> {
            byte b = buf.readByte();
            int group = buf.readInt();

            if (b >= 0 && b < Constant.Cache.BLOCK_FACES.length && group >= 0) {
                BlockFace face = Constant.Cache.BLOCK_FACES[b];
                server.execute(() -> {
                    if (player.containerMenu instanceof MachineScreenHandler<?> sHandler) {
                        MachineBlockEntity machine = sHandler.machine;
                        if (machine.getSecurity().hasAccess(player)) {
                            MachineIOFaceConfig machineFace = machine.getIOConfig().get(face);
                            if (machineFace.getType().matchesGroups()) {
                                SlotGroup[] groups = machineFace.getMatchingGroups(machine);
                                if (group < groups.length) {
                                    machineFace.setSelection(StorageSelection.createGroup(groups[group]));
                                }
                            }
                        }
                    }
                });
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(Constant.id("redstone_config"), (server, player, handler, buf, responseSender) -> {
            RedstoneActivation redstoneActivation = RedstoneActivation.values()[buf.readByte()];
            server.execute(() -> {
                if (player.containerMenu instanceof MachineScreenHandler<?> sHandler) {
                    MachineBlockEntity machine = sHandler.machine;
                    if (machine.getSecurity().hasAccess(player)) {
                        machine.setRedstone(redstoneActivation);
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(Constant.id("security_config"), (server, player, handler, buf, responseSender) -> {
            AccessLevel accessLevel = AccessLevel.values()[buf.readByte()];
            server.execute(() -> {
                if (player.containerMenu instanceof MachineScreenHandler<?> sHandler) {
                    MachineBlockEntity machine = sHandler.machine;
                    if (machine.getSecurity().isOwner(player)) {
                        machine.getSecurity().setAccessLevel(accessLevel);
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(Constant.id("tank_modify"), (server, player, handler, buf, responseSender) -> {
            int syncId = buf.readVarInt();
            int index = buf.readInt();
            server.execute(() -> {
                if (player.containerMenu instanceof MachineScreenHandler<?> sHandler) {
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
            ExposedSlot<Fluid, FluidVariant> slot = tank.getStorage();
            if (storage.supportsExtraction() && slot.supportsInsertion()) {
                try (Transaction transaction = Transaction.openOuter()) {
                    FluidVariant storedResource;
                    if (tank.getResource().isBlank()) {
                        storedResource = StorageUtil.findStoredResource(storage, slot.getFilter(tank.getIndex()));
                    } else {
                        storedResource = tank.getResource();
                    }
                    if (storedResource != null) {
                        if (GenericStorageUtil.move(storedResource, storage, slot, Long.MAX_VALUE, transaction) != 0) {
                            transaction.commit();
                            return true;
                        }
                        return false;
                    }
                }
            } else if (storage.supportsInsertion() && slot.supportsExtraction()) {
                FluidVariant storedResource = tank.getResource();
                if (!storedResource.isBlank()) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        if (GenericStorageUtil.move(storedResource, slot, storage, Long.MAX_VALUE, transaction) != 0) {
                            transaction.commit();
                            return true;
                        }
                        return false;
                    }
                }
            }
        }
        return false;
    }
}
