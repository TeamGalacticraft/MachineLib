/*
 * Copyright (c) 2021-2026 Team Galacticraft
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

import dev.galacticraft.machinelib.api.block.entity.ConfiguredBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineState;
import dev.galacticraft.machinelib.api.machine.configuration.IOConfig;
import dev.galacticraft.machinelib.api.machine.configuration.IOFace;
import dev.galacticraft.machinelib.api.machine.configuration.RedstoneMode;
import dev.galacticraft.machinelib.api.machine.configuration.SecuritySettings;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.api.util.ItemStackUtil;
import dev.galacticraft.machinelib.impl.compat.vanilla.StorageSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Base menu for machines.
 * Handles the basic synchronization and slot management of machines.
 *
 * @param <Machine> The type of machine block entity this menu is linked to.
 */
public abstract class ConfiguredMenu<Machine extends ConfiguredBlockEntity> extends SynchronizedMenu<Machine> {
    /**
     * The I/O configuration of the machine associated with this menu.
     */
    public final @NotNull IOConfig configuration;

    /**
     * The security settings of the machine associated with this menu.
     */
    public final @NotNull SecuritySettings security;

    /**
     * The state of the machine associated with this menu
     */
    public final @NotNull MachineState state;
    /**
     * The tanks contained in this menu.
     *
     * @see #addTank(Tank)
     */
    public final List<Tank> tanks = new ArrayList<>();
    /**
     * The redstone mode of the machine associated with this menu.
     */
    public @NotNull RedstoneMode redstoneMode;
    protected int internalSlots = 0;

    /**
     * Constructs a new menu for a machine.
     * Called on the logical server
     *
     * @param syncId The sync id for this menu.
     * @param player The player who is interacting with this menu.
     * @param machine The machine this menu is for.
     */
    public ConfiguredMenu(MenuType<? extends ConfiguredMenu<Machine>> type, int syncId, @NotNull Player player, @NotNull Machine machine) {
        super(type, syncId, player, machine);

        this.configuration = machine.getIOConfig();
        this.security = machine.getSecurity();
        this.state = machine.getState();

        this.redstoneMode = machine.getRedstoneMode();
    }

    /**
     * Constructs a new menu for a machine.
     * Called on the logical client
     *
     * @param syncId The sync id for this menu.
     * @param pos the position of the block being opened
     * @param inventory The inventory of the player interacting with this menu.
     * @param type The type of menu this is.
     */
    protected ConfiguredMenu(MenuType<? extends ConfiguredMenu<Machine>> type, int syncId, @NotNull Inventory inventory, @NotNull BlockPos pos) {
        super(type, syncId, inventory, pos);

        this.configuration = this.be.getIOConfig();
        this.security = this.be.getSecurity();
        this.state = this.be.getState();

        this.redstoneMode = this.be.getRedstoneMode();
    }

    /**
     * Registers the sync handlers for the menu.
     */
    @Override
    public void registerData(@NotNull MenuData data) {
        super.registerData(data);

        data.register(this.configuration);
        data.register(this.security);
        data.register(this.state);

        data.registerEnum(RedstoneMode.values(), () -> this.redstoneMode, mode -> this.redstoneMode = mode);
    }

    /**
     * {@return whether the given face can be configured for input or output}
     *
     * @param face the block face to test.
     */
    public boolean isFaceLocked(@NotNull BlockFace face) {
        return false;
    }

    /**
     * Creates player inventory slots for this screen in the default inventory formation.
     *
     * @param x The x position of the top left slot.
     * @param y The y position of the top left slot.
     */
    protected void addPlayerInventorySlots(Inventory inventory, int x, int y) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, x + j * 18, y + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, x + i * 18, y + 58));
        }
    }

    protected void generateSlots(@NotNull MachineItemStorage itemStorage) {
        ItemResourceSlot[] slots = itemStorage.getSlots();
        for (int i = 0; i < slots.length; i++) {
            ItemResourceSlot slot = slots[i];
            if (slot.getDisplay() != null) {
                this.internalSlots++;
                this.addSlot(new StorageSlot(itemStorage, slot, slot.getDisplay(), i, this.player));
            }
        }
    }

    protected void generateTanks(@NotNull MachineFluidStorage fluidStorage) {
        FluidResourceSlot[] tanks = fluidStorage.getSlots();
        for (int i = 0; i < tanks.length; i++) {
            FluidResourceSlot slot = tanks[i];
            if (slot.getDisplay() != null) {
                this.addTank(Tank.create(slot, slot.getDisplay(), slot.transferMode(), i));
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.security.hasAccess(player) && super.stillValid(player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slotFrom = this.slots.get(index);
        if (slotFrom.hasItem()) {
            ItemStack stackFrom = slotFrom.getItem();
            stack = stackFrom.copy();

            // move from machine -> player
            if (index < this.internalSlots && slotFrom instanceof StorageSlot storageSlot) {
                ItemResourceSlot itemSlot = storageSlot.getWrapped();
                if (!this.quickMoveIntoPlayerInventory(itemSlot)) {
                    return ItemStack.EMPTY;
                }
                stack = storageSlot.getItem();
                if (itemSlot.transferMode() == TransferType.OUTPUT) {
                    storageSlot.onQuickCraft(stack, stackFrom);
                }
                return stack;
            }

            // move from player -> machine
            assert !(slotFrom instanceof StorageSlot);

            // try to move it into slots that already contain the same item
            long available = stack.getCount();
            final long originalCount = available;
            for (int i = 0; i < this.internalSlots; i++) {
                ItemResourceSlot slot1 = ((StorageSlot) this.slots.get(i)).getWrapped();
                if (slot1.transferMode().playerInsertion() && slot1.contains(stack.getItem(), stack.getComponentsPatch())) {
                    available -= slot1.insert(stack.getItem(), stack.getComponentsPatch(), available);
                    // if we've moved all the items, we're done
                    if (available == 0) {
                        slotFrom.setByPlayer(ItemStack.EMPTY);
                        return ItemStack.EMPTY;
                    }
                }
            }

            // try to move it into empty slots
            for (int i = 0; i < this.internalSlots; i++) {
                StorageSlot slot1 = ((StorageSlot) this.slots.get(i));
                if (slot1.mayPlace(stack)) {
                    available -= slot1.getWrapped().insert(stack.getItem(), stack.getComponentsPatch(), available);
                    if (available == 0) {
                        slotFrom.setByPlayer(ItemStack.EMPTY);
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (available == originalCount) {
                final int size = this.slots.size();
                if (index >= this.internalSlots && index < size - 9) {
                    if (!this.moveItemStackTo(stackFrom, size - 9, size, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= size - 9 && index < size) {
                    if (!this.moveItemStackTo(stackFrom, this.internalSlots, size - 9, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else {
                // if we still have items left, keep them in the player's inventory
                assert available > 0;
                stackFrom.setCount((int) available);
            }

            if (stackFrom.isEmpty()) {
                slotFrom.set(ItemStack.EMPTY);
            } else {
                slotFrom.setChanged();
            }

            if (stackFrom.getCount() == stack.getCount() || available != originalCount) {
                return ItemStack.EMPTY;
            }
        }

        return stack;
    }

    /**
     * Quick-moves a stack from the machine's inventory into the player's inventory
     *
     * @param fromSlot the slot that was shift-clicked
     * @return whether the quick-move was successful
     */
    private boolean quickMoveIntoPlayerInventory(ResourceSlot<Item> fromSlot) {
        // if the slot is empty, nothing moves
        if (fromSlot.isEmpty()) return false;

        // reverse the order for output slots only to match vanilla behaviour
        boolean reverse = fromSlot.transferMode() == TransferType.OUTPUT;
        ItemStack stack = ItemStackUtil.create(fromSlot);

        final int total = stack.getCount();
        final int size = this.slots.size();
        final int start = reverse ? size - 1 : this.internalSlots;

        // try to move it into slots that already contain the same item
        int i = start;
        while (i >= this.internalSlots && i < size) {
            Slot slot = this.slots.get(i);
            assert !(slot instanceof StorageSlot);
            if (ItemStack.isSameItemSameComponents(stack, slot.getItem())) {
                stack = slot.safeInsert(stack);
                if (stack.isEmpty()) {
                    // take items from the machine slot
                    long extract = fromSlot.extract(total);
                    assert extract == total;
                    return true;
                }
            }
            i += reverse ? -1 : 1;
        }

        // try to move it into empty slots
        i = start;
        while (i >= this.internalSlots && i < size) {
            Slot slot1 = this.slots.get(i);
            stack = slot1.safeInsert(stack);
            if (stack.isEmpty()) {
                // take items from the machine slot
                long extract = fromSlot.extract(total);
                assert extract == total;
                return true;
            }
            i += reverse ? -1 : 1;
        }

        long extract = fromSlot.extract(total - stack.getCount());
        assert extract == total - stack.getCount();
        return false;
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverse) {
        if (startIndex < this.internalSlots) {
            throw new UnsupportedOperationException(String.format("invalid startIndex of %s, must be at least %s", startIndex, this.internalSlots));
        }
        return super.moveItemStackTo(stack, startIndex, endIndex, reverse);
    }

    @Override
    public void sendAllDataToRemote() {
        for (int i = 0; i < this.internalSlots; i++) {
            if (this.slots.get(i) instanceof StorageSlot storageSlot) {
                storageSlot.handleStackMutation();
            }
        }
        super.sendAllDataToRemote();
    }

    /**
     * Cycles the I/O configuration of a machine face.
     *
     * @param face the face to cycle
     * @param reverse whether to cycle in reverse
     * @param reset whether to reset the face to the default configuration
     */
    @ApiStatus.Internal
    public void cycleFaceConfig(BlockFace face, boolean reverse, boolean reset) {
        IOFace option = this.configuration.get(face);

        int numBits = 12;
        short bits = calculateIoBitmask();
        if (this.be.faceHasOverride(face)) {
            numBits = 13;
            bits |= 0b10_000_000_000_000; //set OVERRIDE bit
        }
        if (bits != 0b01_000_000_000_000 && !reset && !isFaceLocked(face)) {
            ResourceType type = option.getType();
            ResourceFlow flow = option.getFlow();
            int index = switch (type) {
                case NONE -> 12;
                case OVERRIDE -> 13;
                case ENERGY, ITEM, FLUID, ANY -> (type.ordinal() - 1) * 3 + flow.ordinal();
            };
            int i = index + (reverse ? -1 : 1);
            while (i != index) {
                if (i == -1) {
                    i = numBits;
                } else if (i > numBits) {
                    i = 0;
                }

                if ((bits >>> i & 0b1) != 0) {
                    break;
                }
                if (reverse) {
                    i--;
                } else {
                    i++;
                }
            }

            if (i == 12) {
                option.setOption(ResourceType.NONE, ResourceFlow.BOTH);
            } else if (i == 13) {
                option.setOption(ResourceType.OVERRIDE, ResourceFlow.BOTH);
            } else {
                byte flowIndex = (byte) (i % 3);
                byte typeIndex = (byte) ((i - flowIndex) / 3 + 1);
                type = ResourceType.getFromOrdinal(typeIndex);
                flow = ResourceFlow.getFromOrdinal(flowIndex);

                option.setOption(type, flow);
            }
        } else {
            option.setOption(ResourceType.NONE, ResourceFlow.BOTH);
        }

        this.be.setChanged();
    }

    /**
     * Calculates the bitmask for the I/O configuration of the machine.
     * Format (bits): NONE, any [any][out][in], fluid [any][out][in], item [any][out][in], energy [any][out][in]
     *
     * @return the i/o bitmask
     */
    protected abstract short calculateIoBitmask();

    /**
     * Adds a tank to the screen.
     * Tanks are the fluid equivalent to item slots.
     *
     * @param tank The tank to add.
     */
    public void addTank(@NotNull Tank tank) {
        tank.setId(this.tanks.size());
        this.tanks.add(tank);
    }
}
