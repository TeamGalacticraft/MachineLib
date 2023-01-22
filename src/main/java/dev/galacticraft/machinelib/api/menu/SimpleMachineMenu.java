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
import dev.galacticraft.machinelib.api.machine.MachineType;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * A simple menu for a machine.
 *
 * @param <Machine> the type of machine block entity
 */
public class SimpleMachineMenu<Machine extends MachineBlockEntity> extends MachineMenu<Machine> {
    public SimpleMachineMenu(int syncId, @NotNull ServerPlayer player, @NotNull Machine machine, @NotNull MachineType<Machine, ? extends MachineMenu<Machine>> type) {
        super(syncId, player, machine, type);
        this.addPlayerInventorySlots(player.getInventory(), 0, 0); // it's the server so we don't care
    }

    protected SimpleMachineMenu(int syncId, @NotNull Inventory inventory, @NotNull FriendlyByteBuf buf, @NotNull MachineType<Machine, ? extends MachineMenu<Machine>> type, int invX, int invY) {
        super(syncId, inventory, buf, type);
        this.addPlayerInventorySlots(inventory, invX, invY);
    }

    @Contract(value = "_ -> new", pure = true)
    public static <Machine extends MachineBlockEntity> @NotNull MenuType<SimpleMachineMenu<Machine>> createType(@NotNull Supplier<MachineType<Machine, ? extends SimpleMachineMenu<Machine>>> selfReference) {
        return createType(selfReference, 84);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static <Machine extends MachineBlockEntity> @NotNull MenuType<SimpleMachineMenu<Machine>> createType(@NotNull Supplier<MachineType<Machine, ? extends SimpleMachineMenu<Machine>>> selfReference, int invY) {
        return createType(selfReference, 8, invY);
    }

    @Contract(value = "_, _, _-> new", pure = true)
    public static <Machine extends MachineBlockEntity> @NotNull MenuType<SimpleMachineMenu<Machine>> createType(@NotNull Supplier<MachineType<Machine, ? extends SimpleMachineMenu<Machine>>> selfReference, int invX, int invY) {
        return new ExtendedScreenHandlerType<>((syncId, inventory, buf) -> new SimpleMachineMenu<>(syncId, inventory, buf, selfReference.get(), invX, invY));
    }
}
