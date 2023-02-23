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

package dev.galacticraft.machinelib.api.menu;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.fluid.FluidStack;
import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.machine.configuration.MachineConfiguration;
import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.*;
import dev.galacticraft.machinelib.client.api.screen.Tank;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.storage.slot.AutomatableSlot;
import io.netty.buffer.Unpooled;
import lol.bai.badpackets.api.PacketSender;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Base container menu for machines.
 *
 * @param <Machine> The type of machine block entity this menu is linked to.
 */
public class MachineMenu<Machine extends MachineBlockEntity> extends AbstractContainerMenu {
    public final @NotNull MachineType<?, ?> type;

    @ApiStatus.Internal
    public final @NotNull Machine machine;
    public final boolean server;

    public final @NotNull ContainerLevelAccess levelAccess;
    public final @Nullable ServerPlayer player;
    public final @NotNull Inventory playerInventory;
    public final @NotNull UUID playerUUID;

    public final @NotNull MachineConfiguration configuration;
    public final @NotNull MachineEnergyStorage energyStorage;
    public final @NotNull MachineItemStorage itemStorage;
    public final @NotNull MachineFluidStorage fluidStorage;

    /**
     * The machine this menu is for.
     */
    public final AutomatableSlot[] machineSlots;
    /**
     * The tanks contained in this menu.
     */
    public final List<Tank> tanks = new ArrayList<>();
    /**
     * The storage sync handlers for this menu.
     */
    private final List<MenuSyncHandler> syncHandlers = new ArrayList<>(4);

    /**
     * Constructs a new menu for a machine.
     *
     * @param syncId  The sync id for this menu.
     * @param player  The player who is interacting with this menu.
     * @param machine The machine this menu is for.
     */
    public MachineMenu(int syncId, @NotNull ServerPlayer player, @NotNull Machine machine) {
        super(machine.getMachineType().getMenuType(), syncId);
        assert !Objects.requireNonNull(machine.getLevel()).isClientSide;
        this.type = machine.getMachineType();
        this.machine = machine;
        this.server = true;
        this.player = player;
        this.playerInventory = player.getInventory();
        this.playerUUID = player.getUUID();

        this.configuration = machine.getConfiguration();
        this.energyStorage = machine.energyStorage();
        this.itemStorage = machine.itemStorage();
        this.fluidStorage = machine.fluidStorage();

        this.levelAccess = ContainerLevelAccess.create(machine.getLevel(), machine.getBlockPos());

        int totalSize = 0;
        for (SlotGroupType itemStorageType : this.itemStorage.getTypes()) {
            totalSize += this.itemStorage.getGroup(itemStorageType).size();
        }

        this.machineSlots = new AutomatableSlot[totalSize];

        int index = 0;
        for (SlotGroupType groupType : this.itemStorage.getTypes()) {
            SlotGroup<Item, ItemStack, ItemResourceSlot> group = this.itemStorage.getGroup(groupType);
            ItemResourceSlot[] groupSlots = group.getSlots();
            for (int i = 0; i < groupSlots.length; i++) {
                AutomatableSlot slot1 = new AutomatableSlot((Container) group, groupSlots[i], groupType, i, this.playerUUID);
                this.addSlot(slot1);
                this.machineSlots[index++] = slot1;
            }
        }

        for (SlotGroupType groupType : this.fluidStorage.getTypes()) {
            SlotGroup<Fluid, FluidStack, FluidResourceSlot> group = this.fluidStorage.getGroup(groupType);
            FluidResourceSlot[] groupSlots = group.getSlots();
            for (int i = 0; i < groupSlots.length; i++) {
                FluidResourceSlot slot = groupSlots[i];
                this.addTank(Tank.create(slot, groupType.inputType(), i));
            }
        }

        this.addPlayerInventorySlots(player.getInventory(), 0, 0); // its the server
        this.registerSyncHandlers(this::addSyncHandler);
    }

    /**
     * Constructs a new menu for a machine.
     *
     * @param syncId    The sync id for this menu.
     * @param buf       The synchronization buffer from the server. Should contain exactly one block pos.
     * @param inventory The inventory of the player interacting with this menu.
     * @param type      The type of menu this is.
     */
    protected MachineMenu(int syncId, @NotNull Inventory inventory, @NotNull FriendlyByteBuf buf, int invX, int invY, @NotNull MachineType<Machine, ? extends MachineMenu<Machine>> type) {
        super(type.getMenuType(), syncId);

        this.type = type;
        this.player = null;
        this.server = false;
        this.playerInventory = inventory;
        this.playerUUID = inventory.player.getUUID();

        BlockPos blockPos = buf.readBlockPos();
        this.machine = (Machine) inventory.player.level.getBlockEntity(blockPos); //todo: actually stop using the BE on the client side
        this.levelAccess = ContainerLevelAccess.create(inventory.player.level, blockPos);
        this.configuration = MachineConfiguration.create();
        this.configuration.readPacket(buf);
        this.energyStorage = type.createEnergyStorage();
        this.energyStorage.readPacket(buf);
        this.itemStorage = type.createItemStorage();
        this.itemStorage.readPacket(buf);
        this.fluidStorage = type.createFluidStorage();
        this.fluidStorage.readPacket(buf);

        int totalSize = 0;
        for (SlotGroupType itemStorageType : this.itemStorage.getTypes()) {
            totalSize += this.itemStorage.getGroup(itemStorageType).size();
        }

        this.machineSlots = new AutomatableSlot[totalSize];

        int index = 0;
        for (SlotGroupType groupType : this.itemStorage.getTypes()) {
            SlotGroup<Item, ItemStack, ItemResourceSlot> group = this.itemStorage.getGroup(groupType);
            ItemResourceSlot[] groupSlots = group.getSlots();
            for (int i = 0; i < groupSlots.length; i++) {
                AutomatableSlot slot1 = new AutomatableSlot((Container) group, groupSlots[i], groupType, i, this.playerUUID);
                this.addSlot(slot1);
                this.machineSlots[index++] = slot1;
            }
        }

        for (SlotGroupType groupType : this.fluidStorage.getTypes()) {
            SlotGroup<Fluid, FluidStack, FluidResourceSlot> group = this.fluidStorage.getGroup(groupType);
            FluidResourceSlot[] groupSlots = group.getSlots();
            for (int i = 0; i < groupSlots.length; i++) {
                FluidResourceSlot slot = groupSlots[i];
                this.addTank(Tank.create(slot, groupType.inputType(), i));
            }
        }

        this.addPlayerInventorySlots(inventory, invX, invY);
        this.registerSyncHandlers(this::addSyncHandler);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static <Machine extends MachineBlockEntity, Menu extends MachineMenu<Machine>> @NotNull MenuType<Menu> createType(@NotNull MachineMenuFactory<Machine, Menu> factory, Supplier<MachineType<Machine, Menu>> typeSupplier) {
        return new ExtendedScreenHandlerType<>((syncId, inventory, buf) -> factory.create(syncId, inventory, buf, typeSupplier.get()));
    }

    @Contract(value = "_ -> new", pure = true)
    public static <Machine extends MachineBlockEntity, Menu extends MachineMenu<Machine>> @NotNull MenuType<Menu> createType(@NotNull BasicMachineMenuFactory<Machine, Menu> factory) {
        return new ExtendedScreenHandlerType<>(factory::create);
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    public static <Machine extends MachineBlockEntity> @NotNull MenuType<MachineMenu<Machine>> createSimple(int invX, int invY, Supplier<MachineType<Machine, MachineMenu<Machine>>> typeSupplier) {
        return new ExtendedScreenHandlerType<>((syncId, inventory, buf) -> new MachineMenu<>(syncId, inventory, buf, invX, invY, typeSupplier.get()));
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static <Machine extends MachineBlockEntity> @NotNull MenuType<MachineMenu<Machine>> createSimple(int invY, Supplier<MachineType<Machine, MachineMenu<Machine>>> typeSupplier) {
        return createSimple(8, invY, typeSupplier);
    }

    @Contract(value = "_ -> new", pure = true)
    public static <Machine extends MachineBlockEntity> @NotNull MenuType<MachineMenu<Machine>> createSimple(Supplier<MachineType<Machine, MachineMenu<Machine>>> typeSupplier) {
        return createSimple(84, typeSupplier);
    }

    @MustBeInvokedByOverriders
    public void registerSyncHandlers(Consumer<MenuSyncHandler> consumer) {
        consumer.accept(this.configuration.createSyncHandler());
        consumer.accept(this.itemStorage.createSyncHandler()); //todo: probably synced by vanilla - is this necessary?
        consumer.accept(this.fluidStorage.createSyncHandler());
        consumer.accept(this.energyStorage.createSyncHandler());
    }

    private void addSyncHandler(@Nullable MenuSyncHandler syncHandler) {
        if (syncHandler != null) {
            this.syncHandlers.add(syncHandler);
        }
    }

    @Override
    public void clicked(int i, int j, ClickType clickType, Player player) {
        super.clicked(i, j, clickType, player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int slotId) { //return LEFTOVER (in slot)
        Slot slot = this.slots.get(slotId);
        int size = this.machineSlots.length;

        if (slotId < size) {
            assert slot instanceof AutomatableSlot;
            ResourceSlot<Item, ItemStack> slot1 = ((AutomatableSlot) slot).getSlot();
            this.quickMoveIntoPlayerInventory(slot1);
            return slot1.createStack();
        } else {
            assert !(slot instanceof AutomatableSlot);
            ItemStack stack1 = slot.getItem();
            if (stack1.isEmpty()) return ItemStack.EMPTY;

            int insert = stack1.getCount();
            for (AutomatableSlot slot1 : this.machineSlots) {
                if (slot1.getType().inputType().playerInsertion() && slot1.getSlot().contains(stack1.getItem(), stack1.getTag())) {
                    insert -= slot1.getSlot().insert(stack1.getItem(), stack1.getTag(), insert);
                    if (insert == 0) break;
                }
            }

            if (insert == 0) {
                slot.set(ItemStack.EMPTY);
                return ItemStack.EMPTY;
            } else {
                for (AutomatableSlot slot1 : this.machineSlots) {
                    if (slot1.mayPlace(stack1)) {
                        insert -= slot1.getSlot().insert(stack1.getItem(), stack1.getTag(), insert);
                        if (insert == 0) break;
                    }
                }

                if (insert == 0) {
                    slot.set(ItemStack.EMPTY);
                    return ItemStack.EMPTY;
                } else {
                    stack1.setCount(insert);
                    slot.set(stack1);
                    return ItemStack.EMPTY; //fixme: inf loop if we return actual value, although it doesn't seem to be used beyond looping the call so this should be fine
                }
            }
        }
    }

    private void quickMoveIntoPlayerInventory(ResourceSlot<Item, ItemStack> slot) {
        if (slot.isEmpty()) return;
        ItemStack itemStack = slot.copyStack();
        int extracted = itemStack.getCount();
        int size = this.slots.size() - 1;
        for (int i = size; i >= this.machineSlots.length; i--) {
            Slot slot1 = this.slots.get(i);
            assert !(slot1 instanceof AutomatableSlot);
            if (ItemStack.isSameItemSameTags(itemStack, slot1.getItem())) {
                itemStack = slot1.safeInsert(itemStack);
                if (itemStack.isEmpty()) break;
            }
        }

        if (itemStack.isEmpty()) {
            long extract = slot.extract(extracted);
            assert extract == extracted;
            return;
        }

        for (int i = size; i >= this.machineSlots.length; i--) {
            Slot slot1 = this.slots.get(i);
            itemStack = slot1.safeInsert(itemStack);
            if (itemStack.isEmpty()) break;
        }

        long extract = slot.extract(extracted - itemStack.getCount());
        assert extract == extracted - itemStack.getCount();
    }

    @Override
    protected boolean moveItemStackTo(ItemStack itemStack, int startIndex, int endIndex, boolean reverse) {
        throw new UnsupportedOperationException("you shouldn't call this.");
    }

    /**
     * Creates player inventory slots for this screen in the default inventory formation.
     *
     * @param x The x position of the top left slot.
     * @param y The y position of the top left slot.
     */
    private void addPlayerInventorySlots(Inventory inventory, int x, int y) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, x + j * 18, y + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, x + i * 18, y + 58));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.configuration.getSecurity().hasAccess(player) && stillValid(this.levelAccess, player, this.type.getBlock());
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
        if (this.player != null) {
            int sync = 0;
            for (MenuSyncHandler syncHandler : this.syncHandlers) {
                if (syncHandler.needsSyncing()) sync++;
            }

            if (sync > 0) {
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                buf.writeByte(this.containerId);
                buf.writeVarInt(sync);
                for (int i = 0; i < this.syncHandlers.size(); i++) {
                    MenuSyncHandler handler = this.syncHandlers.get(i);
                    if (handler.needsSyncing()) {
                        buf.writeVarInt(i);
                        handler.sync(buf);
                    }
                }
                PacketSender.s2c(this.player).send(Constant.id("storage_sync"), buf);
            }
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
     *
     * @param tank The tank to add.
     */
    public void addTank(@NotNull Tank tank) {
        tank.setId(this.tanks.size());
        this.tanks.add(tank);
    }

    @FunctionalInterface
    public interface MachineMenuFactory<Machine extends MachineBlockEntity, Menu extends MachineMenu<Machine>> {
        Menu create(int syncId, @NotNull Inventory inventory, @NotNull FriendlyByteBuf buf, @NotNull MachineType<Machine, Menu> type);
    }

    @FunctionalInterface
    public interface BasicMachineMenuFactory<Machine extends MachineBlockEntity, Menu extends MachineMenu<Machine>> {
        Menu create(int syncId, @NotNull Inventory inventory, @NotNull FriendlyByteBuf buf);
    }
}
