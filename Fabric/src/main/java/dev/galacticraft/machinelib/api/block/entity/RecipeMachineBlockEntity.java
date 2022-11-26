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

package dev.galacticraft.machinelib.api.block.entity;

import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.impl.Constant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A machine block entity that processes recipes.
 *
 * @param <C> The type of inventory the recipe type uses.
 * @param <R> The type of recipe the machine uses.
 */
public abstract class RecipeMachineBlockEntity<C extends Container, R extends Recipe<C>> extends MachineBlockEntity {
    /**
     * The type of recipe that this machine processes.
     */
    private final @NotNull RecipeType<R> recipeType;

    /**
     * The number of times the machine's inventory has been modified.
     * Used to determine if the machine's active recipe must be recalculated.
     */
    @ApiStatus.Internal
    private long inventoryModCount = -1;

    /**
     * The machine's active recipe. If there is no active recipe, this will be {@code null}.
     */
    private @Nullable R activeRecipe = null;

    /**
     * The last recipe to be processed in this machine.
     * Used to speed up the recipe search process.
     */
    @ApiStatus.Internal
    private @Nullable R cachedRecipe = null;

    /**
     * The progress of the machine's current recipe.
     * Counts upwards until it reaches {@link #maxProgress maximum rogress}.
     */
    private int progress = 0;

    /**
     * The time it takes to complete the recipe (in ticks).
     */
    private int maxProgress = 0;

    /**
     * Constructs a new machine block entity that processes recipes.
     *
     * @param type The type of block entity.
     * @param pos The position of the machine in the level.
     * @param state The block state of the machine.
     * @param recipeType The type of recipe to be processed.
     */
    protected RecipeMachineBlockEntity(@NotNull BlockEntityType<? extends RecipeMachineBlockEntity<C, R>> type, @NotNull BlockPos pos, BlockState state, @NotNull RecipeType<R> recipeType) {
        super(type, pos, state);
        this.recipeType = recipeType;
    }

    /**
     * The crafting inventory of the machine.
     * Used to determine the machine's active recipe via the vanilla recipe search system.
     * Should never be modified through this method (modify the inventory directly instead).
     *
     * @return The crafting inventory of the machine.
     */
    @Contract(pure = true)
    protected abstract @NotNull C craftingInv();

    /**
     * Inserts the recipe's output into the machine's inventory.
     *
     * @param recipe The recipe to output.
     * @param context The current transaction.
     * @return Whether the recipe was successfully output.
     */
    protected abstract boolean outputStacks(@NotNull R recipe, @NotNull TransactionContext context);

    /**
     * Extracts the recipe's input from the machine's inventory.
     *
     * @param recipe The recipe to extract.
     * @param context The current transaction.
     * @return Whether the recipe was successfully extracted.
     */
    protected abstract boolean extractCraftingMaterials(@NotNull R recipe, @NotNull TransactionContext context);

    /**
     * Returns the machine status to use when the machine is working.
     * A machine is working if it has an active recipe and the recipe's progress is less than the maximum progress.
     *
     * @return The machine status to use when the machine is working.
     */
    @Contract(pure = true)
    protected abstract @NotNull MachineStatus workingStatus();

    /**
     * Extracts the necessary resources to run this machine.
     * This can be energy, fuel, or any other resource (or nothing!).
     *
     * @param context The current transaction.
     * @return {@code null} if the machine can run, or a {@link MachineStatus machine status} describing why it cannot.
     */
    protected @Nullable MachineStatus extractResourcesToWork(@NotNull TransactionContext context) {
        return null;
    }

    @Override
    public @NotNull MachineStatus tick(@NotNull ServerLevel world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        profiler.push("recipe_test");
        MachineStatus recipeFailure = testInventoryRecipe(world, profiler);
        profiler.pop();
        if (recipeFailure != null) {
            return recipeFailure;
        }

        if (this.getActiveRecipe() != null) {
            profiler.push("working_transaction");
            try (Transaction transaction = Transaction.openOuter()) {
                MachineStatus status = this.extractResourcesToWork(transaction);
                if (status == null) {
                    if (++this.progress >= this.getMaxProgress()) {
                        profiler.push("crafting");
                        this.craft(profiler, this.getActiveRecipe(), transaction);
                        profiler.pop();
                    }
                    transaction.commit();
                    return this.workingStatus();
                } else {
                    return status;
                }
            } finally {
                profiler.pop();
            }
        } else {
            if (this.getStatus() == MachineStatuses.OUTPUT_FULL) return MachineStatuses.OUTPUT_FULL; //preserve full state
            return MachineStatuses.INVALID_RECIPE;
        }
    }

    /**
     * Updates the currently active recipe if the inventory has changed.
     * @param world The world.
     * @param profiler The world profiler.
     * @return {@code null} if the machine can have a recipe, or a {@link MachineStatus machine status} describing why it cannot.
     */
    @Nullable
    protected MachineStatus testInventoryRecipe(@NotNull ServerLevel world, @NotNull ProfilerFiller profiler) {
        if (this.inventoryModCount != this.itemStorage().getModCount()) { // includes output slots
            this.inventoryModCount = this.itemStorage().getModCount();
            profiler.push("find_recipe");
            Optional<R> optional = this.findValidRecipe(world);
            profiler.pop();
            if (optional.isPresent()) {
                R recipe = optional.get();
                try (Transaction transaction = Transaction.openOuter()) {
                    if (this.outputStacks(recipe, transaction)) {
                        this.updateRecipe(recipe);
                    } else {
                        this.resetRecipe();
                        return MachineStatuses.OUTPUT_FULL;
                    }
                }
            } else {
                this.resetRecipe();
                return MachineStatuses.INVALID_RECIPE;
            }
        }
        return null;
    }

    /**
     * Sets the current recipe to the given recipe.
     * If the recipe is different from the current recipe, the progress is reset.
     *
     * @param recipe The recipe to set.
     *               If {@code null}, the recipe will be reset.
     */
    @Contract(mutates = "this")
    private void updateRecipe(@Nullable R recipe) {
        if (recipe == null) {
            this.resetRecipe();
        } else if (this.getActiveRecipe() != recipe) {
            this.setActiveRecipe(recipe);
            this.setMaxProgress(this.getProcessTime(recipe));
            this.setProgress(0);
        }
    }

    /**
     * Crafts the given recipe.
     *
     * @param profiler The world profiler.
     * @param recipe The recipe to craft.
     * @param context The current transaction.
     */
    protected void craft(@NotNull ProfilerFiller profiler, @NotNull R recipe, @Nullable TransactionContext context) {
        profiler.push("extract_materials");
        try (Transaction inner = Transaction.openNested(context)) {
            if (this.extractCraftingMaterials(recipe, inner)) {
                profiler.popPush("output_stacks");
                if (this.outputStacks(recipe, inner)) {
                    this.inventoryModCount = -1; // make sure everything is recalculated
                    this.resetRecipe();
                    inner.commit();
                }
            }
        }
        profiler.pop();
    }

    /**
     * Resets the progress of the machine and clears the active recipe.
     */
    @Contract(mutates = "this")
    protected void resetRecipe() {
        this.setActiveRecipe(null);
        this.setProgress(0);
        this.setMaxProgress(0);
    }

    /**
     * Returns the recipe type of the machine.
     *
     * @return The recipe type of the machine.
     */
    @Contract(pure = true)
    public @NotNull RecipeType<R> getRecipeType() {
        return this.recipeType;
    }

    /**
     * Finds the first valid recipe in the machine's inventory.
     * Will always test for the current recipe first.
     *
     * @param world The world.
     * @return The first valid recipe in the machine's inventory.
     */
    protected @NotNull Optional<R> findValidRecipe(@NotNull Level world) {
        if (this.cachedRecipe != null) {
            if (this.cachedRecipe.matches(this.craftingInv(), world)) {
                return Optional.of(this.cachedRecipe);
            }
        }
        return world.getRecipeManager().getRecipeFor(this.getRecipeType(), this.craftingInv(), world);
    }

    /**
     * Returns the process time of the given recipe.
     *
     * @param recipe The recipe to get the process time of.
     * @return The process time of the given recipe.
     */
    @Contract(pure = true)
    protected abstract int getProcessTime(@NotNull R recipe);

    /**
     * Sets and returns the crafting progress of the machine.
     *
     * @param progress The progress to set.
     */
    @Contract(mutates = "this")
    public void setProgress(int progress) {
        this.progress = progress;
    }

    /**
     * Sets the maximum progress of the machine.
     *
     * @param maxProgress The maximum progress to set.
     */
    @Contract(mutates = "this")
    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    /**
     * Returns the progress of the machine.
     *
     * @return The progress of the machine.
     */
    @Contract(pure = true)
    public int getProgress() {
        return this.progress;
    }

    /**
     * Returns the active recipe of the machine. May be {@code null}.
     *
     * @return The active recipe of the machine.
     */
    @Contract(pure = true)
    public @Nullable R getActiveRecipe() {
        return this.activeRecipe;
    }

    /**
     * Sets the active recipe of the machine.
     *
     * @param activeRecipe The recipe to set.
     */
    @Contract(mutates = "this")
    protected void setActiveRecipe(@Nullable R activeRecipe) {
        if (activeRecipe != null) this.cachedRecipe = activeRecipe;
        this.activeRecipe = activeRecipe;
    }

    /**
     * Returns the maximum progress of the machine.
     *
     * @return The maximum progress of the machine.
     */
    @Contract(pure = true)
    public int getMaxProgress() {
        return this.maxProgress;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putInt(Constant.Nbt.PROGRESS, this.getProgress());
        nbt.putInt(Constant.Nbt.MAX_PROGRESS, this.getMaxProgress());
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        this.progress = nbt.getInt(Constant.Nbt.PROGRESS);
        this.maxProgress = nbt.getInt(Constant.Nbt.MAX_PROGRESS);
    }

    @Override
    public void setLevel(Level world) {
        super.setLevel(world);
        if (!world.isClientSide) {
            this.cachedRecipe = this.activeRecipe = this.findValidRecipe(world).orElse(null);
        }
    }
}
