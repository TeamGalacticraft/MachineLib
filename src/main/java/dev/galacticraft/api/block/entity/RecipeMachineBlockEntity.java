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

package dev.galacticraft.api.block.entity;

import dev.galacticraft.api.machine.MachineStatus;
import dev.galacticraft.api.machine.MachineStatuses;
import dev.galacticraft.impl.MLConstant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A machine block entity that processes recipes.
 *
 * @param <C> The type of inventory the recipe type uses.
 *           This is usually {@link Container} but can be any inventory type.
 * @param <R> The type of recipe the machine uses.
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public abstract class RecipeMachineBlockEntity<C extends Container, R extends Recipe<C>> extends MachineBlockEntity {
    /**
     * The recipe type that this machine processes.
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
     *
     * @return The crafting inventory of the machine.
     */
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
    public @NotNull MachineStatus tick(@NotNull ServerLevel world, @NotNull BlockPos pos, @NotNull BlockState state) {
        if (this.inventoryModCount != this.itemStorage().getModCount()) {
            world.getProfiler().push("recipe_test");
            this.inventoryModCount = this.itemStorage().getModCount();
            world.getProfiler().push("find_recipe");
            Optional<R> optional = this.findValidRecipe(world);
            world.getProfiler().pop();
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
            world.getProfiler().pop();
        }
        if (this.activeRecipe != null) {
            world.getProfiler().push("working_transaction");
            try (Transaction transaction = Transaction.openOuter()) {
                MachineStatus status = this.extractResourcesToWork(transaction);
                if (status == null) {
                    if (++this.progress >= this.getMaxProgress()) {
                        world.getProfiler().push("crafting");
                        this.craft(world, this.activeRecipe, transaction);
                        world.getProfiler().pop();
                    }
                    transaction.commit();
                    return this.workingStatus();
                } else {
                    return status;
                }
            } finally {
                world.getProfiler().pop();
            }
        } else {
            if (this.getStatus() == MachineStatuses.OUTPUT_FULL) return MachineStatuses.OUTPUT_FULL; //preserve full state
            return MachineStatuses.INVALID_RECIPE;
        }
    }

    /**
     * Sets the current recipe to the given recipe.
     * If the recipe is different from the current recipe, the progress is reset.
     *
     * @param recipe The recipe to set.
     *               If {@code null}, the recipe will be reset.
     */
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
     * @param recipe The recipe to craft.
     * @param context The current transaction.
     */
    protected void craft(@NotNull Level world, @NotNull R recipe, @Nullable TransactionContext context) {
        world.getProfiler().push("extract_materials");
        try (Transaction inner = Transaction.openNested(context)) {
            if (this.extractCraftingMaterials(recipe, inner)) {
                world.getProfiler().popPush("output_stacks");
                if (this.outputStacks(recipe, inner)) {
                    inner.commit();
                }
            }
        } finally {
            world.getProfiler().pop();
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
     *
     * @return The first valid recipe in the machine's inventory.
     */
    protected @NotNull Optional<R> findValidRecipe(@NotNull Level world) {
        if (this.getActiveRecipe() != null) {
            if (this.getActiveRecipe().matches(this.craftingInv(), world)) {
                return Optional.of(this.getActiveRecipe());
            }
        }
        return world.getRecipeManager().getRecipeFor(this.getRecipeType(), this.craftingInv(), world);
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
    public void saveAdditional(@NotNull CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putInt(MLConstant.Nbt.PROGRESS, this.getProgress());
        nbt.putInt(MLConstant.Nbt.MAX_PROGRESS, this.getMaxProgress());
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        this.progress = nbt.getInt(MLConstant.Nbt.PROGRESS);
        this.maxProgress = nbt.getInt(MLConstant.Nbt.MAX_PROGRESS);
    }

    @Override
    public void setLevel(Level world) {
        super.setLevel(world);
        this.activeRecipe = this.findValidRecipe(world).orElse(null);
    }
}
