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
 * @param <R>       The type of recipe the machine processes
 * @param <C>       The type of storage the recipe uses
 * @see MachineMenu
 */
public class RecipeMachineMenu<C extends Container, R extends Recipe<C>, Machine extends RecipeMachineBlockEntity<C, R>> extends MachineMenu<Machine> {
    /**
     * The amount of progress the machine has made in crafting a recipe.
     * Counts from zero to {@link #maxProgress}, if {@link #maxProgress} > 0.
     */
    private int progress = 0;
    /**
     * The number of ticks a machine must work before crafting something.
     * If zero, no recipe is active.
     */
    private int maxProgress = 0;

    /**
     * Constructs a new recipe menu.
     *
     * @param syncId  The sync id for this menu.
     * @param player  The player who is interacting with this menu.
     * @param machine The machine this menu is for.
     */
    public RecipeMachineMenu(int syncId, @NotNull ServerPlayer player, @NotNull Machine machine) {
        super(syncId, player, machine);
    }

    /**
     * Constructs a new recipe menu for a machine.
     *
     * @param syncId    The sync id for this menu.
     * @param inventory The inventory of the player interacting with this menu.
     * @param buf       The data buffer containing the information needed to initialize the menu.
     * @param invX      The x-coordinate of the top-left player inventory slot.
     * @param invY      The y-coordinate of the top-left player inventory slot.
     * @param type      The type of machine associated with this menu.
     */
    protected RecipeMachineMenu(int syncId, @NotNull Inventory inventory, @NotNull FriendlyByteBuf buf, int invX, int invY, @NotNull MachineType<Machine, ? extends MachineMenu<Machine>> type) {
        super(syncId, inventory, buf, invX, invY, type);

        this.maxProgress = buf.readInt();
        if (this.maxProgress > 0) {
            this.progress = buf.readInt();
        } else {
            this.progress = 0;
        }
    }

    /**
     * Creates a new menu type.
     *
     * @param selfReference A supplier that provides the machine type associated with this menu.
     * @return The created menu type.
     * @param <C> The container associated with the machine's recipe type.
     * @param <R> The recipe type associated with the machine.
     * @param <Machine> The type of machine associated with this menu.
     */
    @Contract(value = "_ -> new", pure = true)
    public static <C extends Container, R extends Recipe<C>, Machine extends RecipeMachineBlockEntity<C, R>> @NotNull MenuType<RecipeMachineMenu<C, R, Machine>> createType(@NotNull Supplier<MachineType<Machine, ? extends RecipeMachineMenu<C, R, Machine>>> selfReference) {
        return createType(selfReference, 84);
    }

    /**
     * Creates a new menu type with the specified inventory Y coordinate.
     *
     * @param selfReference A supplier that provides the machine type associated with this menu.
     * @param invY The y-coordinate of the top-left player inventory slot.
     * @return The created menu type.
     * @param <C> The container associated with the machine's recipe type.
     * @param <R> The recipe type associated with the machine.
     * @param <Machine> The type of machine associated with this menu.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static <C extends Container, R extends Recipe<C>, Machine extends RecipeMachineBlockEntity<C, R>> @NotNull MenuType<RecipeMachineMenu<C, R, Machine>> createType(@NotNull Supplier<MachineType<Machine, ? extends RecipeMachineMenu<C, R, Machine>>> selfReference, int invY) {
        return createType(selfReference, 8, invY);
    }

    /**
     * Creates a new menu type with the specified inventory X and Y coordinates.
     *
     * @param selfReference A supplier that provides the machine type associated with this menu.
     * @param invX The x-coordinate of the top-left player inventory slot.
     * @param invY The y-coordinate of the top-left player inventory slot.
     * @return The created menu type.
     * @param <C> The container associated with the machine's recipe type.
     * @param <R> The recipe type associated with the machine.
     * @param <Machine> The type of machine associated with this menu.
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    public static <C extends Container, R extends Recipe<C>, Machine extends RecipeMachineBlockEntity<C, R>> @NotNull MenuType<RecipeMachineMenu<C, R, Machine>> createType(@NotNull Supplier<MachineType<Machine, ? extends RecipeMachineMenu<C, R, Machine>>> selfReference, int invX, int invY) {
        return new ExtendedScreenHandlerType<>((syncId, inventory, buf) -> new RecipeMachineMenu<>(syncId, inventory, buf, invX, invY, selfReference.get()));
    }

    @Override
    public void registerSyncHandlers(Consumer<MenuSyncHandler> consumer) {
        super.registerSyncHandlers(consumer);

        consumer.accept(MenuSyncHandler.simple(this.machine::getProgress, this::setProgress));
        consumer.accept(MenuSyncHandler.simple(() -> {
            R recipe = this.machine.getActiveRecipe();
            return recipe != null ? this.machine.getProcessingTime(recipe) : 0;
        }, this::setMaxProgress));
    }

    /**
     * Returns the current progress of the machine.
     *
     * @return The progress value.
     */
    public int getProgress() {
        return this.progress;
    }

    /**
     * Sets the progress value of the machine.
     *
     * @param progress The new progress value.
     */
    public void setProgress(int progress) {
        this.progress = progress;
    }

    /**
     * Gets the maximum progress value of the machine.
     *
     * @return The maximum progress value.
     */
    public int getMaxProgress() {
        return this.maxProgress;
    }

    /**
     * Sets the maximum progress value of the machine.
     *
     * @param maxProgress The new maximum progress value.
     */
    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }
}
