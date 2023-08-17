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

package dev.galacticraft.machinelib.api.block.entity;

import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.menu.RecipeMachineMenu;
import dev.galacticraft.machinelib.impl.Constant;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @ApiStatus.Internal
    private MachineStatus cachedRecipeState = null;

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
     */
    private int progress = 0;

    /**
     * Constructs a new machine block entity that processes recipes.
     *
     * @param type       The type of block entity.
     * @param pos        The position of the machine in the level.
     * @param state      The block state of the machine.
     * @param recipeType The type of recipe to be processed.
     */
    protected RecipeMachineBlockEntity(@NotNull MachineType<? extends RecipeMachineBlockEntity<C, R>, ? extends RecipeMachineMenu<C, R, ? extends RecipeMachineBlockEntity<C, R>>> type, @NotNull BlockPos pos, BlockState state, @NotNull RecipeType<R> recipeType) {
        super(type, pos, state);
        this.recipeType = recipeType;
    }

    /**
     * An inventory for use in finding vanilla recipes for this machine.
     * NOTE: This inventory can assume that it is never modified - do not modify it!
     *
     * @return The crafting inventory of the machine.
     */
    @Contract(pure = true)
    protected abstract @NotNull C craftingInv();

    /**
     * Inserts the active recipe's output into the machine's inventory.
     *
     * @param recipe The recipe to output.
     */
    protected abstract void outputStacks(@NotNull R recipe);

    /**
     * Checks if the machine can output stacks for the given recipe.
     *
     * @param recipe The recipe to check.
     * @return {@code true} if the machine can output stacks for the recipe, {@code false} otherwise.
     */
    protected abstract boolean canOutputStacks(@NotNull R recipe);

    /**
     * Extracts the recipe's input from the machine's inventory.
     *
     * @param recipe The recipe to extract.
     */
    protected abstract void extractCraftingMaterials(@NotNull R recipe);

    /**
     * Returns the machine status to use when the machine is working on a certain recipe.
     *
     * @return The machine status to use when the machine is working on a certain recipe.
     */
    @Contract(pure = true)
    protected abstract @NotNull MachineStatus workingStatus(R recipe);

    /**
     * Tests if the necessary resources to run this machine are available.
     * This can be energy, fuel, or any other resource (or nothing!).
     *
     * @return {@code null} if the machine can run, or a {@link MachineStatus machine status} describing why it cannot.
     * @see #extractResourcesToWork()
     */
    protected abstract @Nullable MachineStatus hasResourcesToWork();

    /**
     * Extracts the necessary resources to run this machine.
     * This can be energy, fuel, or any other resource (or nothing!).
     *
     * @see #hasResourcesToWork()
     */
    protected abstract void extractResourcesToWork();

    @Override
    public @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        profiler.push("resources");
        MachineStatus status = this.hasResourcesToWork();
        profiler.pop();
        if (status == null) {
            profiler.push("recipe");
            MachineStatus recipeFailure = this.testInventoryRecipe(level, profiler);
            profiler.pop();
            if (recipeFailure == null) {
                R recipe = this.getActiveRecipe();
                assert recipe != null;
                profiler.push("working");
                this.extractResourcesToWork();
                if (++this.progress > this.getProcessingTime(recipe)) {
                    profiler.push("crafting");
                    this.craft(profiler, recipe);
                    profiler.pop();
                }
                profiler.pop();
                return this.workingStatus(recipe);
            }
            return recipeFailure;
        }
        return status;
    }

    /**
     * Updates the currently active recipe if the inventory has changed.
     *
     * @param world    The world.
     * @param profiler The world profiler.
     * @return {@code null} if the machine can have a recipe, or a {@link MachineStatus machine status} describing why it cannot.
     */
    @Nullable
    protected MachineStatus testInventoryRecipe(@NotNull ServerLevel world, @NotNull ProfilerFiller profiler) {
        if (this.inventoryModCount != this.itemStorage().getModifications()) { // includes output slots
            this.inventoryModCount = this.itemStorage().getModifications();
            profiler.push("find_recipe");
            R recipe = this.findValidRecipe(world);
            profiler.pop();
            if (recipe != null) {
                if (this.canOutputStacks(recipe)) {
                    this.setActiveRecipe(recipe);
                    this.cachedRecipeState = null;
                } else {
                    this.setActiveRecipe(null);
                    this.cachedRecipeState = MachineStatuses.OUTPUT_FULL;
                }
            } else {
                this.setActiveRecipe(null);
                this.cachedRecipeState = MachineStatuses.INVALID_RECIPE;
            }
        }

        return this.cachedRecipeState;
    }

    /**
     * Crafts the given recipe.
     *
     * @param profiler The world profiler.
     * @param recipe   The recipe to craft.
     */
    protected void craft(@NotNull ProfilerFiller profiler, @NotNull R recipe) {
        profiler.push("extract_materials");
        this.extractCraftingMaterials(recipe);
        profiler.popPush("output_stacks");
        this.outputStacks(recipe);
        profiler.pop();
        this.setActiveRecipe(null);
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
    protected @Nullable R findValidRecipe(@NotNull Level world) {
        if (this.cachedRecipe != null && this.cachedRecipe.matches(this.craftingInv(), world)) {
            return this.cachedRecipe;
        }
        return world.getRecipeManager().getRecipeFor(this.getRecipeType(), this.craftingInv(), world).orElse(null);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, @NotNull FriendlyByteBuf buf) {
        super.writeScreenOpeningData(player, buf);
        if (this.activeRecipe != null) {
            buf.writeInt(this.getProcessingTime(this.activeRecipe));
            buf.writeInt(this.progress);
        } else {
            buf.writeInt(0);
        }
    }

    /**
     * Returns the process time of the given recipe.
     *
     * @param recipe The recipe to get the process time of.
     * @return The process time of the given recipe.
     */
    @Contract(pure = true)
    public abstract int getProcessingTime(@NotNull R recipe);

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
     * Sets and returns the crafting progress of the machine.
     *
     * @param progress The progress to set.
     */
    @Contract(mutates = "this")
    public void setProgress(int progress) {
        this.progress = progress;
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
     * @param recipe The recipe to set.
     */
    @Contract(mutates = "this")
    protected void setActiveRecipe(@Nullable R recipe) {
        if (recipe != null) this.cachedRecipe = recipe;

        if (this.activeRecipe != recipe) {
            this.activeRecipe = recipe;
            this.setProgress(0);
        } else if (recipe == null) {
            this.setProgress(0);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putInt(Constant.Nbt.PROGRESS, this.getProgress());
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        this.progress = nbt.getInt(Constant.Nbt.PROGRESS);
    }
}
