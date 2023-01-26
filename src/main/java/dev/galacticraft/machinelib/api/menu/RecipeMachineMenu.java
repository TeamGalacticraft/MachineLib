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

import dev.galacticraft.machinelib.api.block.entity.RecipeMachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import dev.galacticraft.machinelib.impl.network.DirectDataSlot;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A simple menu that keeps track of recipe progress
 *
 * @param <Machine> The type of machine block entity
 * @param <R> The type of recipe the machine processes
 * @param <C> The type of storage the recipe uses
 * @see MachineMenu
 */
public class RecipeMachineMenu<C extends Container, R extends Recipe<C>, Machine extends RecipeMachineBlockEntity<C, R>> extends MachineMenu<Machine> {
    private int progress = 0;
    private int maxProgress = 0;

    /**
     * Constructs a new recipe menu.
     *
     * @param syncId  The sync id for this menu.
     * @param player  The player who is interacting with this menu.
     * @param machine The machine this menu is for.
     * @param type    The type of menu this is.
     */
    public RecipeMachineMenu(int syncId, @NotNull ServerPlayer player, @NotNull Machine machine, @NotNull MachineType<Machine, ? extends MachineMenu<Machine>> type) {
        super(syncId, player, machine, type);

        this.addPlayerInventorySlots(player.getInventory(), 0, 0); // it's the server so we don't care
    }

    protected RecipeMachineMenu(int syncId, @NotNull Inventory inventory, @NotNull FriendlyByteBuf buf, @NotNull MachineType<Machine, ? extends MachineMenu<Machine>> type, int invX, int invY) {
        super(syncId, inventory, buf, type);

        this.progress = buf.readInt();
        this.maxProgress = buf.readInt();

        this.addPlayerInventorySlots(inventory, invX, invY);
    }

    @Override
    public void registerSyncHandlers(Consumer<MenuSyncHandler> consumer) {
        super.registerSyncHandlers(consumer);

        this.addDataSlot(new DirectDataSlot(this::getProgress, this::setProgress));
        this.addDataSlot(new DirectDataSlot(this::getMaxProgress, this::setMaxProgress));
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    @Contract(value = "_ -> new", pure = true)
    public static <C extends Container, R extends Recipe<C>, Machine extends RecipeMachineBlockEntity<C, R>> @NotNull MenuType<RecipeMachineMenu<C, R, Machine>> createType(@NotNull Supplier<MachineType<Machine, ? extends RecipeMachineMenu<C, R, Machine>>> selfReference) {
        return createType(selfReference, 84);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static <C extends Container, R extends Recipe<C>, Machine extends RecipeMachineBlockEntity<C, R>> @NotNull MenuType<RecipeMachineMenu<C, R, Machine>> createType(@NotNull Supplier<MachineType<Machine, ? extends RecipeMachineMenu<C, R, Machine>>> selfReference, int invY) {
        return createType(selfReference, 8, invY);
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    public static <C extends Container, R extends Recipe<C>, Machine extends RecipeMachineBlockEntity<C, R>> @NotNull MenuType<RecipeMachineMenu<C, R, Machine>> createType(@NotNull Supplier<MachineType<Machine, ? extends RecipeMachineMenu<C, R, Machine>>> selfReference, int invX, int invY) {
        return new ExtendedScreenHandlerType<>((syncId, inventory, buf) -> new RecipeMachineMenu<>(syncId, inventory, buf, selfReference.get(), invX, invY));
    }
}