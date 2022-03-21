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

package dev.galacticraft.api.screen;

import dev.galacticraft.api.block.entity.MachineBlockEntity;
import dev.galacticraft.api.client.screen.Tank;
import dev.galacticraft.impl.machine.Constant;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public abstract class MachineScreenHandler<M extends MachineBlockEntity> extends ScreenHandler {
    public final PlayerEntity player;
    public final M machine;
    protected final List<StorageSyncHandler> syncHandlers = new ArrayList<>(4);
    public List<Tank<?, ?>> tanks = new ArrayList<>();

    protected MachineScreenHandler(int syncId, PlayerEntity player, M machine, ScreenHandlerType<? extends MachineScreenHandler<M>> handlerType) {
        super(handlerType, syncId);
        this.player = player;
        this.machine = machine;

        this.machine.itemStorage().addSlots(this);
        this.machine.fluidStorage().addTanks(this);
        this.machine.gasStorage().addTanks(this);

        this.syncHandlers.add(this.machine.itemStorage().createSyncHandler());
        this.syncHandlers.add(this.machine.fluidStorage().createSyncHandler());
        this.syncHandlers.add(this.machine.gasStorage().createSyncHandler());
        this.syncHandlers.add(this.machine.capacitor().createSyncHandler());

        this.addProperty(new StatusProperty(this.machine));
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int slotId) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotId);

        if (slot.hasStack()) {
            ItemStack stack1 = slot.getStack();
            stack = stack1.copy();

            if (stack.isEmpty()) {
                return stack;
            }

            int size = this.machine.itemStorage().size();
            if (slotId < size) {
                if (!this.insertItem(stack1, size, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(stack1, 0, size, false)) {
                return ItemStack.EMPTY;
            }
            if (stack1.getCount() == 0) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return stack;
    }

    protected void addPlayerInventorySlots(int x, int y) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                super.addSlot(new Slot(this.player.getInventory(), j + i * 9 + 9, x + j * 18, y + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            super.addSlot(new Slot(this.player.getInventory(), i, x + i * 18, y + 58));
        }
    }

    @Override
    public Slot addSlot(Slot slot) {
        return super.addSlot(slot);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return machine.security().hasAccess(player);
    }

    @Override
    public void syncState() {
        super.syncState();
        assert player instanceof ServerPlayerEntity;

        int sync = 0;
        for (StorageSyncHandler syncHandler : this.syncHandlers) {
            if (syncHandler.needsSyncing()) sync++;
        }

        if (sync > 0) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeByte(this.syncId);
            buf.writeVarInt(sync);
            for (int i = 0; i < this.syncHandlers.size(); i++) {
                StorageSyncHandler handler = this.syncHandlers.get(i);
                if (handler.needsSyncing()) {
                    buf.writeVarInt(i);
                    handler.sync(buf);
                }
            }
            ServerPlayNetworking.send(((ServerPlayerEntity) this.player), new Identifier(Constant.MOD_ID, "storage_sync"), buf);
        }
    }

    public void recieveState(@NotNull PacketByteBuf buf) {
        int sync = buf.readVarInt();
        for (int i = 0; i < sync; i++) {
            this.syncHandlers.get(buf.readVarInt()).read(buf);
        }
    }

    public void addTank(@NotNull Tank<?, ?> tank) {
        tank.id = this.tanks.size();
        this.tanks.add(tank);
    }

    private static class StatusProperty extends Property {
        private final MachineBlockEntity machine;

        public StatusProperty(MachineBlockEntity machine) {
            this.machine = machine;
        }

        @Override
        public int get() {
            return machine.getStatus().getIndex();
        }

        @Override
        public void set(int value) {
            machine.setStatusById(value);
        }
    }
}
