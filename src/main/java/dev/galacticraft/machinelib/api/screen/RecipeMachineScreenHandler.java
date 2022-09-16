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

import dev.galacticraft.machinelib.api.block.entity.RecipeMachineBlockEntity;
import dev.galacticraft.machinelib.impl.network.DirectDataSlot;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * A simple screen handler that keeps track of recipe progress
 *
 * @param <M> The type of machine block entity
 * @param <R> The type of recipe the machine processes
 * @param <C> The type of storage the recipe uses
 * @see MachineScreenHandler
 */
public class RecipeMachineScreenHandler<C extends Container, R extends Recipe<C>, M extends RecipeMachineBlockEntity<C, R>> extends MachineScreenHandler<M> {
    /**
     * Constructs a new recipe screen handler.
     * @param syncId The sync id for this screen handler.
     * @param player The player who is interacting with this screen handler.
     * @param machine The machine this screen handler is for.
     * @param type The type of screen handler this is.
     * @param invX The leftmost x position of the player inventory.
     * @param invY The topmost y position of the player inventory.
     */
    protected RecipeMachineScreenHandler(int syncId, Player player, M machine, MenuType<? extends RecipeMachineScreenHandler<C, R, M>> type, int invX, int invY) {
        super(syncId, player, machine, type);
        this.addDataSlot(new DirectDataSlot(this.machine::getProgress, this.machine::setProgress));
        this.addDataSlot(new DirectDataSlot(this.machine::getMaxProgress, this.machine::setMaxProgress));
        this.addPlayerInventorySlots(invX, invY);
    }

    public static <C extends Container, R extends Recipe<C>, T extends RecipeMachineBlockEntity<C, R>> ExtendedScreenHandlerType.@NotNull ExtendedFactory<RecipeMachineScreenHandler<C, R, T>> createFactory(Supplier<MenuType<? extends RecipeMachineScreenHandler<C, R, T>>> handlerType) {
        return createFactory(handlerType, 8, 84);
    }

    public static <C extends Container, R extends Recipe<C>, T extends RecipeMachineBlockEntity<C, R>> ExtendedScreenHandlerType.@NotNull ExtendedFactory<RecipeMachineScreenHandler<C, R, T>> createFactory(Supplier<MenuType<? extends RecipeMachineScreenHandler<C, R, T>>> handlerType, int invY) {
        return createFactory(handlerType, 8, invY);
    }

    public static <C extends Container, R extends Recipe<C>, T extends RecipeMachineBlockEntity<C, R>> ExtendedScreenHandlerType.@NotNull ExtendedFactory<RecipeMachineScreenHandler<C, R, T>> createFactory(Supplier<MenuType<? extends RecipeMachineScreenHandler<C, R, T>>> handlerType, int invX, int invY) {
        return (syncId, inventory, buf) -> create(syncId, inventory.player, (T)inventory.player.level.getBlockEntity(buf.readBlockPos()), handlerType.get(), invX, invY);
    }

    @Contract("_, _, _, _ -> new")
    public static <C extends Container, R extends Recipe<C>, T extends RecipeMachineBlockEntity<C, R>> @NotNull RecipeMachineScreenHandler<C, R, T> create(int syncId, Player playerEntity, T machine, MenuType<? extends RecipeMachineScreenHandler<C, R, T>> handlerType) {
        return create(syncId, playerEntity, machine, handlerType, 8, 84);
    }

    @Contract("_, _, _, _, _ -> new")
    public static <C extends Container, R extends Recipe<C>, T extends RecipeMachineBlockEntity<C, R>> @NotNull RecipeMachineScreenHandler<C, R, T> create(int syncId, Player playerEntity, T machine, MenuType<? extends RecipeMachineScreenHandler<C, R, T>> handlerType, int invY) {
        return create(syncId, playerEntity, machine, handlerType, 8, invY);
    }

    @Contract("_, _, _, _, _, _ -> new")
    public static <C extends Container, R extends Recipe<C>, T extends RecipeMachineBlockEntity<C, R>> @NotNull RecipeMachineScreenHandler<C, R, T> create(int syncId, Player playerEntity, T machine, MenuType<? extends RecipeMachineScreenHandler<C, R, T>> handlerType, int invX, int invY) {
        return new RecipeMachineScreenHandler<>(syncId, playerEntity, machine, handlerType, invX, invY);
    }
}
