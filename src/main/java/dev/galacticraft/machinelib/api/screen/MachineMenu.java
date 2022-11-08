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

package dev.galacticraft.machinelib.api.screen;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.client.api.screen.Tank;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.screen.property.StatusProperty;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Base container menu for machines.
 *
 * @param <M> The type of machine block entity this menu is linked to.
 */
public abstract class MachineMenu<M extends MachineBlockEntity> extends AbstractContainerMenu {
    /**
     * The player who is currently interacting with this menu.
     */
    public final Player player;
    /**
     * The machine this menu is for.
     */
    public final M machine;
    /**
     * The storage sync handlers for this menu.
     */
    protected final List<StorageSyncHandler> syncHandlers = new ArrayList<>(4);
    /**
     * The tanks contained in this menu.
     */
    public final List<Tank> tanks = new ArrayList<>();

    /**
     * Constructs a new menu for a machine.
     *
     * @param syncId The sync id for this menu.
     * @param player The player who is interacting with this menu.
     * @param machine The machine this menu is for.
     * @param type The type of menu this is.
     */
    protected MachineMenu(int syncId, @NotNull Player player, @NotNull M machine, @Nullable MenuType<? extends MachineMenu<M>> type) {
        super(type, syncId);
        this.player = player;
        this.machine = machine;

        this.machine.itemStorage().addSlots(this);
        this.machine.fluidStorage().addTanks(this);

        this.syncHandlers.add(this.machine.itemStorage().createSyncHandler());
        this.syncHandlers.add(this.machine.fluidStorage().createSyncHandler());
        this.syncHandlers.add(this.machine.energyStorage().createSyncHandler());

        this.addDataSlot(new StatusProperty(this.machine));
    }

    /**
     * Constructs a new menu for a machine.
     *
     * @param syncId The sync id for this menu.
     * @param buf The synchronization buffer from the server. Should contain exactly one block pos.
     * @param inventory The inventory of the player interacting with this menu.
     * @param type The type of menu this is.
     */
    protected MachineMenu(int syncId, @NotNull Inventory inventory, @NotNull FriendlyByteBuf buf, @Nullable MenuType<? extends MachineMenu<M>> type) {
        this(syncId, inventory.player, (M) inventory.player.level.getBlockEntity(buf.readBlockPos()), type);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotId) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotId);

        if (slot.hasItem()) {
            ItemStack stack1 = slot.getItem();
            stack = stack1.copy();

            if (stack.isEmpty()) {
                return stack;
            }

            int size = this.machine.itemStorage().size();
            if (slotId < size) {
                if (!this.moveItemStackTo(stack1, size, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack1, 0, size, false)) {
                return ItemStack.EMPTY;
            }
            if (stack1.getCount() == 0) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return stack;
    }

    /**
     * Creates player inventory slots for this screen in the default inventory formation.
     *
     * @param x The x position of the top left slot.
     * @param y The y position of the top left slot.
     */
    protected void addPlayerInventorySlots(int x, int y) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(this.player.getInventory(), j + i * 9 + 9, x + j * 18, y + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(this.player.getInventory(), i, x + i * 18, y + 58));
        }
    }

    @Override
    public Slot addSlot(Slot slot) {
        return super.addSlot(slot);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.machine.getSecurity().hasAccess(player);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        this.syncStorages();
    }

    /**
     * Syncs the storages in this menu.
     */
    @ApiStatus.Internal
    private void syncStorages() {
        assert player instanceof ServerPlayer;

        int sync = 0;
        for (StorageSyncHandler syncHandler : this.syncHandlers) {
            if (syncHandler.needsSyncing()) sync++;
        }

        if (sync > 0) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeByte(this.containerId);
            buf.writeVarInt(sync);
            for (int i = 0; i < this.syncHandlers.size(); i++) {
                StorageSyncHandler handler = this.syncHandlers.get(i);
                if (handler.needsSyncing()) {
                    buf.writeVarInt(i);
                    handler.sync(buf);
                }
            }
            ServerPlayNetworking.send(((ServerPlayer) this.player), Constant.id("storage_sync"), buf);
        }
    }

    /**
     * Receives and deserialized storage sync packets from the server.
     *
     * @param buf The packet buffer.
     */
    @ApiStatus.Internal
    public void receiveState(@NotNull FriendlyByteBuf buf) {
        int sync = buf.readVarInt();
        for (int i = 0; i < sync; i++) {
            this.syncHandlers.get(buf.readVarInt()).read(buf);
        }
    }

    /**
     * Adds a tank to the screen.
     * @param tank The tank to add.
     */
    public void addTank(@NotNull Tank tank) {
        tank.setId(this.tanks.size());
        this.tanks.add(tank);
    }
}
