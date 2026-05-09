/*
 * Copyright (c) 2021-2025 Team Galacticraft
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

package dev.galacticraft.machinelib.api.multiblock.components;

import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.api.multiblock.MultiblockComponentContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Reusable recipe-processing component for formed multiblock machines.
 *
 * <p>This is the multiblock equivalent of {@code RecipeMachineBlockEntity}. It
 * owns recipe progress and active-recipe caching, but reads storage from the
 * formed multiblock's {@link MultiblockStorageComponent} instead of from a block
 * entity.</p>
 *
 * @param <I> recipe input type
 * @param <R> recipe type
 */
public abstract class MultiblockRecipeProcessingComponent<I extends RecipeInput, R extends Recipe<I>> implements MultiblockComponent {

    private static final String PROGRESS = "Progress";

    private final RecipeType<R> recipeType;

    private MultiblockComponentContext context;
    private MultiblockStorageComponent storage;

    private long inventoryModCount = -1;
    private @Nullable MachineStatus cachedRecipeState = null;
    private @Nullable RecipeHolder<R> activeRecipe = null;
    private @Nullable RecipeHolder<R> cachedRecipe = null;

    private int progress = 0;

    /**
     * Creates a recipe-processing component.
     *
     * @param recipeType recipe type processed by this component
     */
    protected MultiblockRecipeProcessingComponent(final RecipeType<R> recipeType) {
        this.recipeType = recipeType;
    }

    /**
     * Gets the storage component used by this processor.
     *
     * @return storage component
     */
    protected final MultiblockStorageComponent storage() {
        return this.storage;
    }

    /**
     * Gets the component context.
     *
     * @return component context
     */
    protected final MultiblockComponentContext context() {
        return this.context;
    }

    /**
     * Creates the recipe input used to search and match recipes.
     *
     * @return recipe input
     */
    protected abstract @NotNull I craftingInv();

    /**
     * Inserts output stacks for a completed recipe.
     *
     * @param recipe completed recipe
     */
    protected abstract void outputStacks(@NotNull RecipeHolder<R> recipe);

    /**
     * Checks whether a completed recipe can output into storage.
     *
     * @param recipe recipe to test
     * @return {@code true} if output storage can accept the result
     */
    protected abstract boolean canOutputStacks(@NotNull RecipeHolder<R> recipe);

    /**
     * Extracts recipe ingredients from input storage.
     *
     * @param recipe recipe being crafted
     */
    protected abstract void extractCraftingMaterials(@NotNull RecipeHolder<R> recipe);

    /**
     * Gets the status used while the machine is working on a recipe.
     *
     * @param recipe active recipe
     * @return working status
     */
    protected abstract @NotNull MachineStatus workingStatus(@NotNull RecipeHolder<R> recipe);

    /**
     * Checks whether non-recipe resources are available.
     *
     * <p>This can check energy, fluid, fuel, heat, pressure, or any other
     * machine-specific resource. Return {@code null} when the machine can work.</p>
     *
     * @return failure status, or {@code null} if resources are available
     */
    protected abstract @Nullable MachineStatus hasResourcesToWork();

    /**
     * Consumes non-recipe resources for one processing tick.
     */
    protected abstract void extractResourcesToWork();

    /**
     * Gets how much progress should decay when the machine cannot work.
     *
     * @return progress decay amount
     */
    protected abstract int decreaseProgressAmount();

    /**
     * Gets the processing time for a recipe.
     *
     * @param recipe recipe
     * @return processing time in ticks
     */
    public abstract int getProcessingTime(@NotNull RecipeHolder<R> recipe);

    @Override
    public void onFormed(final MultiblockComponentContext context) {
        this.context = context;
        this.storage = context.component(MultiblockStorageComponent.class);

        if (this.storage == null) {
            throw new IllegalStateException("MultiblockRecipeProcessingComponent requires MultiblockStorageComponent");
        }
    }

    @Override
    public void tick(final MultiblockComponentContext context) {
        final MachineStatus resourceStatus = this.hasResourcesToWork();

        if (resourceStatus != null) {
            this.decreaseProgress();
            return;
        }

        final MachineStatus recipeFailure = this.testInventoryRecipe();

        if (recipeFailure != null) {
            this.decreaseProgress();
            return;
        }

        final RecipeHolder<R> recipe = this.activeRecipe;

        if (recipe == null) {
            this.decreaseProgress();
            return;
        }

        this.extractResourcesToWork();

        this.progress++;
        context.setChanged();

        if (this.progress >= this.getProcessingTime(recipe)) {
            this.craft(recipe);
        }
    }

    /**
     * Tests and updates the active recipe when inventory contents changed.
     *
     * @return failure status, or {@code null} when a recipe can run
     */
    protected @Nullable MachineStatus testInventoryRecipe() {
        if (this.inventoryModCount != this.storage.itemStorage().getModifications()) {
            this.inventoryModCount = this.storage.itemStorage().getModifications();

            final RecipeHolder<R> recipe = this.findValidRecipe();

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
     * Finds the first valid recipe for this multiblock.
     *
     * @return valid recipe, or {@code null}
     */
    protected @Nullable RecipeHolder<R> findValidRecipe() {
        if (this.cachedRecipe != null && this.cachedRecipe.value().matches(this.craftingInv(), this.context.level())) {
            return this.cachedRecipe;
        }

        return this.context.level()
                .getRecipeManager()
                .getRecipeFor(this.recipeType, this.craftingInv(), this.context.level())
                .orElse(null);
    }

    /**
     * Completes a recipe by consuming inputs and inserting outputs.
     *
     * @param recipe completed recipe
     */
    protected void craft(final @NotNull RecipeHolder<R> recipe) {
        this.extractCraftingMaterials(recipe);
        this.outputStacks(recipe);
        this.setActiveRecipe(null);
        this.context.setChanged();
    }

    /**
     * Decreases progress when the machine cannot work.
     */
    protected void decreaseProgress() {
        final int previous = this.progress;

        this.progress = Math.max(
                this.progress - this.decreaseProgressAmount(),
                0
        );

        if (this.progress != previous && this.context != null) {
            this.context.setChanged();
        }
    }

    /**
     * Gets the current progress.
     *
     * @return current progress
     */
    @Contract(pure = true)
    public int getProgress() {
        return this.progress;
    }

    /**
     * Sets current progress.
     *
     * @param progress new progress
     */
    public void setProgress(final int progress) {
        this.progress = Math.max(progress, 0);

        if (this.context != null) {
            this.context.setChanged();
        }
    }

    /**
     * Gets the active recipe.
     *
     * @return active recipe, or {@code null}
     */
    @Contract(pure = true)
    public @Nullable RecipeHolder<R> getActiveRecipe() {
        return this.activeRecipe;
    }

    /**
     * Sets the active recipe.
     *
     * @param recipe active recipe, or {@code null}
     */
    protected void setActiveRecipe(final @Nullable RecipeHolder<R> recipe) {
        if (recipe != null) {
            this.cachedRecipe = recipe;
        }

        if (this.activeRecipe != recipe) {
            this.activeRecipe = recipe;
            this.progress = 0;
        } else if (recipe == null) {
            this.progress = 0;
        }

        if (this.context != null) {
            this.context.setChanged();
        }
    }

    /**
     * Gets current progress from {@code 0.0F} to {@code 1.0F}.
     *
     * @return progress ratio
     */
    @Contract(pure = true)
    public float getProgressRatio() {
        if (this.activeRecipe == null) {
            return 0.0F;
        }

        final int max = this.getProcessingTime(this.activeRecipe);

        if (max <= 0) {
            return 0.0F;
        }

        return ((float) this.progress) / ((float) max);
    }

    @Override
    public void save(
            final MultiblockComponentContext context,
            final CompoundTag tag
    ) {
        tag.putInt(PROGRESS, this.progress);
    }

    @Override
    public void load(
            final MultiblockComponentContext context,
            final CompoundTag tag
    ) {
        this.progress = tag.getInt(PROGRESS);
    }
}