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

import dev.galacticraft.machinelib.api.block.entity.RecipeMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

/**
 * A simple menu that keeps track of recipe progress
 *
 * @param <Machine> The type of machine block entity
 * @param <R> The type of recipe the machine processes
 * @param <I> The type of storage the recipe uses
 * @see MachineMenu
 */
public class RecipeMachineMenu<I extends RecipeInput, R extends Recipe<I>, Machine extends RecipeMachineBlockEntity<I, R>> extends MachineMenu<Machine> {
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
     * @param type The type of machine associated with this menu.
     * @param syncId The sync id for this menu.
     * @param player The player who is interacting with this menu.
     * @param machine The machine this menu is for.
     */
    public RecipeMachineMenu(MenuType<? extends RecipeMachineMenu<I, R, Machine>> type, int syncId, @NotNull Player player, @NotNull Machine machine) {
        super(type, syncId, player, machine);
    }

    /**
     * Constructs a new recipe menu for a machine.
     *
     * @param type The type of machine associated with this menu.
     * @param syncId The sync id for this menu.
     * @param inventory The inventory of the player interacting with this menu.
     * @param pos the position of the machine being opened
     */
    protected RecipeMachineMenu(MenuType<? extends RecipeMachineMenu<I, R, Machine>> type, int syncId, @NotNull Inventory inventory, @NotNull BlockPos pos) {
        super(type, syncId, inventory, pos);
    }

    /**
     * Constructs a new recipe menu for a machine.
     *
     * @param type The type of machine associated with this menu.
     * @param syncId The sync id for this menu.
     * @param inventory The inventory of the player interacting with this menu.
     * @param pos the position of the machine being opened
     * @param invX The x position of the inventory in the menu.
     * @param invY The y position of the inventory in the menu.
     */
    public RecipeMachineMenu(MenuType<? extends RecipeMachineMenu<I, R, Machine>> type, int syncId, @NotNull Inventory inventory, @NotNull BlockPos pos, int invX, int invY) {
        super(type, syncId, inventory, pos, invX, invY);
    }

    @Override
    public void registerData(@NotNull MenuData data) {
        super.registerData(data);

        data.registerInt(this.be::getProgress, this::setProgress);
        data.registerInt(() -> {
            RecipeHolder<R> recipe = this.be.getActiveRecipe();
            return recipe != null ? this.be.getProcessingTime(recipe) : 0;
        }, this::setMaxProgress);
    }

    /**
     * {@return the current progress of the machine}
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
