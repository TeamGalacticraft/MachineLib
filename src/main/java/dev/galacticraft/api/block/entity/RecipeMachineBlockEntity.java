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

package dev.galacticraft.api.block.entity;

import dev.galacticraft.api.machine.MachineStatus;
import dev.galacticraft.api.machine.MachineStatuses;
import dev.galacticraft.impl.machine.Constant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A machine block entity that processes recipes.
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public abstract class RecipeMachineBlockEntity<C extends Inventory, R extends Recipe<C>> extends MachineBlockEntity {
    /**
     * The recipe type that this machine processes.
     */
    private final @NotNull RecipeType<R> recipeType;

    /**
     * The number of times the machine's inventory has been modified.
     * Used to determine if the machine's active recipe must be recalculated.
     */
    @ApiStatus.Internal
    private int inventoryModCount = -1;

    /**
     * The machine's active recipe. If there is no active recipe, this will be {@code null}.
     */
    private @Nullable R activeRecipe = null;

    /**
     * The progress of the machine's current recipe.
     * Counts upwards until it reaches {@link #maxProgress maximum rogress}.
     */
    private int progress = 0;

    /**
     * The time it takes to complete the recipe (in ticks).
     */
    private int maxProgress = 0;

    protected RecipeMachineBlockEntity(@NotNull BlockEntityType<? extends RecipeMachineBlockEntity<C, R>> type, @NotNull BlockPos pos, BlockState state, @NotNull RecipeType<R> recipeType) {
        super(type, pos, state);
        this.recipeType = recipeType;
    }

    /**
     * The crafting inventory of the machine.
     * Used to determine the machine's active recipe. Will not be modified.
     * @return The crafting inventory of the machine.
     */
    protected abstract @NotNull C craftingInv();

    /**
     * Inserts the recipe's output into the machine's inventory.
     * @param recipe The recipe to output.
     * @param context The current transaction.
     * @return Whether the recipe was successfully output.
     */
    protected abstract boolean outputStacks(R recipe, TransactionContext context);

    /**
     * Extracts the recipe's input from the machine's inventory.
     * @param recipe The recipe to extract.
     * @param context The current transaction.
     * @return Whether the recipe was successfully extracted.
     */
    protected abstract boolean extractCraftingMaterials(R recipe, TransactionContext context);

    /**
     * Returns the machine status to use when the machine is working.
     * @return The machine status to use when the machine is working.
     */
    protected abstract @NotNull MachineStatus workingStatus();

    /**
     * Extracts the neccecary resources to run this machine.
     * This can be energy, fuel, or any other resource.
     * @param context The current transaction.
     * @return {@code null} if the machine can run, or a {@link MachineStatus machine status} describing why it cannot.
     */
    protected @Nullable MachineStatus extractResourcesToWork(@NotNull TransactionContext context) {
        return null;
    }

    @Override
    public @NotNull MachineStatus tick() {
        if (this.inventoryModCount != this.itemStorage().getModCount()) {
            this.world.getProfiler().push("recipe_test");
            this.inventoryModCount = this.itemStorage().getModCount();
            Optional<R> optional = this.findValidRecipe();
            if (optional.isPresent()) {
                R recipe = optional.get();
                try (Transaction transaction = Transaction.openOuter()) {
                    if (this.outputStacks(recipe, transaction)) {
                        this.updateRecipe(recipe);
                    } else {
                        return MachineStatuses.OUTPUT_FULL;
                    }
                }
            } else {
                this.resetRecipe();
                return MachineStatuses.INVALID_RECIPE;
            }
            this.world.getProfiler().pop();
        }
        if (this.activeRecipe != null) {
            this.world.getProfiler().push("working_transaction");
            try (Transaction transaction = Transaction.openOuter()) {
                MachineStatus status = this.extractResourcesToWork(transaction);
                if (status == null) {
                    if (++this.progress >= this.getMaxProgress()) {
                        this.world.getProfiler().push("crafting");
                        this.craft(this.activeRecipe, transaction);
                        this.world.getProfiler().pop();
                    }
                    transaction.commit();
                    return this.workingStatus();
                } else {
                    return status;
                }
            } finally {
                this.world.getProfiler().pop();
            }
        } else {
            if (this.getStatus() == MachineStatuses.OUTPUT_FULL) return MachineStatuses.OUTPUT_FULL; //preserve full state
            return MachineStatuses.INVALID_RECIPE;
        }
    }

    /**
     * Sets the current recipe to the given recipe.
     * If the recipe is different from the current recipe, the progress is reset.
     * @param recipe The recipe to set.
     *               If {@code null}, the recipe will be reset.
     */
    private void updateRecipe(R recipe) {
        if (this.getActiveRecipe() != recipe || recipe == null) {
            this.setActiveRecipe(recipe);
            this.setMaxProgress(this.getProcessTime(recipe));
            this.setProgress(0);
        }
    }

    /**
     * Crafts the given recipe.
     * @param recipe The recipe to craft.
     * @param context The current transaction.
     */
    protected void craft(R recipe, @Nullable TransactionContext context) {
        try (Transaction inner = Transaction.openNested(context)) {
            if (this.extractCraftingMaterials(recipe, inner)) {
                if (this.outputStacks(recipe, inner)) {
                    inner.commit();
                }
            }
        }
    }

    /**
     * Resets the progress of the machine and clears the active recipe.
     */
    protected void resetRecipe() {
        this.setActiveRecipe(null);
        this.setProgress(0);
        this.setMaxProgress(0);
    }

    /**
     * Returns the recipe type of the machine.
     * @return The recipe type of the machine.
     */
    public @NotNull RecipeType<R> getRecipeType() {
        return this.recipeType;
    }

    /**
     * Finds the first valid recipe in the machine's inventory.
     * Will always test for the current recipe first.
     * @return The first valid recipe in the machine's inventory.
     */
    protected @NotNull Optional<R> findValidRecipe() {
        assert this.world != null;
        if (this.getActiveRecipe() != null) {
            Optional<R> match = this.getRecipeType().match(this.getActiveRecipe(), this.world, this.craftingInv());
            if (match.isPresent()) {
                return match;
            }
        }
        return this.world.getRecipeManager().getFirstMatch(this.getRecipeType(), this.craftingInv(), this.world);
    }

    /**
     * Returns the process time of the given recipe.
     * @param recipe The recipe to get the process time of.
     * @return The process time of the given recipe.
     */
    protected abstract int getProcessTime(@NotNull R recipe);

    /**
     * Sets and returns the crafting progress of the machine.
     * @param progress The progress to set.
     */
    public void setProgress(int progress) {
        this.progress = progress;
    }

    /**
     * Sets the maximum progress of the machine.
     * @param maxProgress The maximum progress to set.
     */
    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    /**
     * Returns the progress of the machine.
     * @return The progress of the machine.
     */
    public int getProgress() {
        return this.progress;
    }

    /**
     * Returns the active recipe of the machine. May be {@code null}.
     * @return The active recipe of the machine.
     */
    public @Nullable R getActiveRecipe() {
        return this.activeRecipe;
    }

    /**
     * Sets the active recipe of the machine.
     * @param activeRecipe The recipe to set.
     */
    protected void setActiveRecipe(@Nullable R activeRecipe) {
        this.activeRecipe = activeRecipe;
    }

    /**
     * Returns the maximum progress of the machine.
     * @return The maximum progress of the machine.
     */
    public int getMaxProgress() {
        return this.maxProgress;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt(Constant.Nbt.PROGRESS, this.getProgress());
        nbt.putInt(Constant.Nbt.MAX_PROGRESS, this.getMaxProgress());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.progress = nbt.getInt(Constant.Nbt.PROGRESS);
        this.maxProgress = nbt.getInt(Constant.Nbt.MAX_PROGRESS);
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        this.activeRecipe = this.findValidRecipe().orElse(null);
    }
}
