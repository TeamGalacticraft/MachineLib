/*
 * Copyright (c) 2019-2022 Team Galacticraft
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

package dev.galacticraft.impl.network;

import com.mojang.datafixers.util.Either;
import dev.galacticraft.api.block.entity.MachineBlockEntity;
import dev.galacticraft.api.block.util.BlockFace;
import dev.galacticraft.api.client.screen.Tank;
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.machine.storage.io.ResourceType;
import dev.galacticraft.api.machine.storage.io.SlotType;
import dev.galacticraft.api.screen.MachineScreenHandler;
import dev.galacticraft.api.transfer.v1.gas.GasStorage;
import dev.galacticraft.impl.machine.Constant;
import dev.galacticraft.impl.util.GenericStorageUtil;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class MachineLibC2SPackets {
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(new Identifier(Constant.MOD_ID, "side_config"), (server, player, handler, buf, responseSender) -> {
            BlockFace face = BlockFace.values()[buf.readByte()];
            if (buf.readBoolean()) { //match
                if (buf.readBoolean()) { // int or slottype
                    int i = buf.readInt();
                    server.execute(() -> {
                        if (player.currentScreenHandler instanceof MachineScreenHandler sHandler) {
                            MachineBlockEntity machine = sHandler.machine;
                            if (machine.security().hasAccess(player)) {
                                if (i == -1) {
                                    machine.getConfiguration().getSideConfiguration().get(face).setMatching(null);
                                    return;
                                }
                                machine.getConfiguration().getSideConfiguration().get(face).setMatching(Either.left(i));
                            }
                        }
                    });
                } else {
                    int i = buf.readInt();
                    server.execute(() -> {
                        if (player.currentScreenHandler instanceof MachineScreenHandler sHandler) {
                            MachineBlockEntity machine = sHandler.machine;
                            if (machine.security().hasAccess(player)) {
                                if (i == -1) {
                                    machine.getConfiguration().getSideConfiguration().get(face).setMatching(null);
                                    return;
                                }
                                SlotType<?, ?> type = SlotType.REGISTRY.get(i);
                                machine.getConfiguration().getSideConfiguration().get(face).setMatching(Either.right(type));
                            }
                        }
                    });
                }
            } else {
                byte i = buf.readByte();
                byte j = buf.readByte();
                server.execute(() -> {
                    if (player.currentScreenHandler instanceof MachineScreenHandler sHandler) {
                        MachineBlockEntity machine = sHandler.machine;
                        if (machine.security().hasAccess(player)) {
                            machine.getConfiguration().getSideConfiguration().get(face).setOption(ResourceType.getFromOrdinal(i), ResourceFlow.values()[j]);
                            machine.getConfiguration().getSideConfiguration().get(face).setMatching(null);
                            machine.sync();
                        }
                    }
                });
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(new Identifier(Constant.MOD_ID, "tank_modify"), (server, player, handler, buf, responseSender) -> {
            int syncId = buf.readVarInt();
            int index = buf.readInt();
            server.execute(() -> {
                if (player.currentScreenHandler instanceof MachineScreenHandler sHandler) {
                    if (sHandler.syncId == syncId) {
                        acceptStack((Tank)sHandler.tanks.get(index), ContainerItemContext.ofPlayerCursor(player, player.currentScreenHandler));
                    }
                }
            });
        });
    }

    private static <T, V extends TransferVariant<T>> boolean acceptStack(@NotNull Tank<T, V> tank, @NotNull ContainerItemContext context) {
        Storage<V> storage = context.find(getLookup(tank.getResourceType()));
        if (storage != null) {
            if (storage.supportsExtraction() && tank.storage.supportsInsertion()) {
                try (Transaction transaction = Transaction.openOuter()) {
                    V storedResource;
                    if (tank.getResource().isBlank()) {
                        storedResource = StorageUtil.findStoredResource(storage, tank.storage.getFilter(tank.getIndex()), transaction);
                    } else {
                        storedResource = tank.getResource();
                    }
                    if (storedResource != null) {
                        if (GenericStorageUtil.move(storedResource, storage, tank.storage.getSlot(tank.getIndex()), Long.MAX_VALUE, transaction) != 0) {
                            transaction.commit();
                            return true;
                        }
                        return false;
                    }
                }
            } else if (storage.supportsInsertion() && tank.storage.supportsExtraction()) {
                V storedResource = tank.getResource();
                if (!storedResource.isBlank()) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        if (GenericStorageUtil.move(storedResource, tank.storage.getSlot(tank.getIndex()), storage, Long.MAX_VALUE, transaction) != 0) {
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

    private static <T, V extends TransferVariant<T>> ItemApiLookup<Storage<V>, ContainerItemContext> getLookup(ResourceType<T, V> type) {
        if (type == ResourceType.GAS) {
            return (ItemApiLookup<Storage<V>, ContainerItemContext>) (Object) GasStorage.ITEM;
        } else if (type == ResourceType.GAS){
            return (ItemApiLookup<Storage<V>, ContainerItemContext>) (Object)FluidStorage.ITEM;
        }
        throw new AssertionError();
    }
}
