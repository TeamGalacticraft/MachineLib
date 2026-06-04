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

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;

/**
 * Base menu for machines.
 * Handles the basic synchronization and slot management of machines.
 *
 * @param <Machine> The type of machine block entity this menu is linked to.
 */
public class MachineMenu<Machine extends MachineBlockEntity> extends ConfiguredMenu<Machine> {
    /**
     * The item storage of the machine associated with this menu
     */
    public final @NotNull MachineItemStorage itemStorage;

    /**
     * The fluid storage of the machine associated with this menu
     */
    public final @NotNull MachineFluidStorage fluidStorage;

    /**
     * The energy storage of the machine associated with this menu
     */
    public final @NotNull MachineEnergyStorage energyStorage;

    /**
     * Constructs a new menu for a machine.
     * Called on the logical server
     *
     * @param type The type of menu this is.
     * @param syncId The sync id for this menu.
     * @param player The player who is interacting with this menu.
     * @param machine The machine this menu is for.
     */
    public MachineMenu(MenuType<? extends MachineMenu<Machine>> type, int syncId, @NotNull Player player, @NotNull Machine machine) {
        super(type, syncId, player, machine);

        this.itemStorage = machine.itemStorage();
        this.fluidStorage = machine.fluidStorage();
        this.energyStorage = machine.energyStorage();

        this.generateSlots(this.itemStorage);
        this.generateTanks(this.fluidStorage);
        this.addPlayerInventorySlots(player.getInventory(), 0, 0);
    }

    /**
     * Constructs a new menu for a machine.
     * Called on the logical client.
     * You must add the player inventory slots manually.
     *
     * @param type The type of menu this is.
     * @param syncId The sync id for this menu.
     * @param pos the position of the block being opened
     * @param inventory The inventory of the player interacting with this menu.
     */
    protected MachineMenu(MenuType<? extends MachineMenu<Machine>> type, int syncId, @NotNull Inventory inventory, @NotNull BlockPos pos) {
        super(type, syncId, inventory, pos);

        this.itemStorage = this.be.itemStorage();
        this.fluidStorage = this.be.fluidStorage();
        this.energyStorage = this.be.energyStorage();

        this.generateSlots(this.itemStorage);
        this.generateTanks(this.fluidStorage);
    }

    public MachineMenu(MenuType<? extends MachineMenu<Machine>> type, int syncId, @NotNull Inventory inventory, @NotNull BlockPos pos, int invX, int invY) {
        this(type, syncId, inventory, pos);
        this.addPlayerInventorySlots(inventory, invX, invY);
    }

    /**
     * Registers the sync handlers for the menu.
     */
    @Override
    public void registerData(@NotNull MenuData data) {
        super.registerData(data);

        data.register(this.itemStorage);
        data.register(this.fluidStorage);
        data.register(this.energyStorage);
    }

    /**
     * Calculates the bitmask for the I/O configuration of the machine.
     * Format (bits): NONE, any [any][out][in], fluid [any][out][in], item [any][out][in], energy [any][out][in]
     *
     * @return the i/o bitmask
     */
    @Override
    protected short calculateIoBitmask() {
        // Format: NONE, any [any][out][in], fluid [any][out][in], item [any][out][in], energy [any][out][in]
        short bits = 0b0_000_000_000_000;

        for (FluidResourceSlot slot : this.fluidStorage) {
            TransferType transferType = slot.transferMode();
            if (transferType.externalInsertion()) bits |= 0b001;
            if (transferType.externalExtraction()) bits |= 0b010;
            if ((bits & 0b011) == 0b011) break;
        }
        if ((bits & 0b011) == 0b011) bits |= 0b100;
        bits <<= 3;

        for (ItemResourceSlot slot : this.itemStorage) {
            TransferType transferType = slot.transferMode();
            if (transferType.externalInsertion()) bits |= 0b001;
            if (transferType.externalExtraction()) bits |= 0b010;
            if ((bits & 0b011) == 0b011) break;
        }
        if ((bits & 0b011) == 0b011) bits |= 0b100;
        bits <<= 3;

        if (this.energyStorage.externalInsertionRate() > 0) bits |= 0b001;
        if (this.energyStorage.externalExtractionRate() > 0) bits |= 0b010;
        if ((bits & 0b011) == 0b011) bits |= 0b100;

        if (Integer.bitCount(bits & 0b000_001_001_001) > 1) {
            bits |= 0b001_000_000_000;
        }
        if (Integer.bitCount(bits & 0b000_010_010_010) > 1) {
            bits |= 0b010_000_000_000;
        }
        if (Integer.bitCount(bits & 0b000_100_100_100) > 1) {
            bits |= 0b100_000_000_000;
        }

        bits |= 0b1_000_000_000_000; //set NONE bit
        return bits;
    }
}
