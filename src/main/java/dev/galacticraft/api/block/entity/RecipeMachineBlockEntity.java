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

import dev.galacticraft.impl.machine.Constant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public abstract class RecipeMachineBlockEntity<C extends Inventory, R extends Recipe<C>> extends MachineBlockEntity {
    private final @NotNull RecipeType<R> recipeType;
    private @Nullable R activeRecipe;
    private int progress;
    private int maxProgress = 0;

    public RecipeMachineBlockEntity(BlockEntityType<? extends RecipeMachineBlockEntity<C, R>> type, BlockPos pos, BlockState state, @NotNull RecipeType<R> recipeType) {
        super(type, pos, state);
        this.recipeType = recipeType;
    }

    /**
     * The crafting inventory of the machine.
     * Will not be modified.
     * @return The crafting inventory of the machine.
     */
    protected abstract @NotNull C craftingInv();

    /**
     * Inserts the recipe's output into the machine's inventory.
     * @param recipe The recipe to output.
     * @param transaction The current transaction.
     * @return Whether the recipe was successfully output.
     */
    protected abstract boolean outputStacks(R recipe, TransactionContext transaction);

    /**
     * Extracts the recipe's input from the machine's inventory.
     * @param recipe The recipe to extract.
     * @param transaction The current transaction.
     * @return Whether the recipe was successfully extracted.
     */
    protected abstract boolean extractCraftingMaterials(R recipe, TransactionContext transaction);

    @Override
    public void tickWork() {
        if (this.getStatus().getType().isActive()) {
            R recipe = this.findValidRecipe();
            if (this.canOutput(recipe, null)) {
                if (this.activeRecipe() != recipe) {
                    this.setRecipeAndProgress(recipe);
                } else {
                    if (this.progress(this.progress() + 1) >= this.maxProgress()) {
                        try (Transaction transaction = Transaction.openOuter()) {
                            this.craft(recipe, transaction);
                            transaction.commit();
                        }
                    } else {
                        this.resetRecipeProgress();
                    }
                }
            } else {
                this.resetRecipeProgress();
            }
        }
    }

    /**
     * Whether the machine can output the given recipe.
     * @param recipe The recipe to check.
     * @param context The current transaction.
     * @return Whether the machine can output the given recipe.
     */
    protected boolean canOutput(R recipe, @Nullable TransactionContext context) {
        try (Transaction transaction = Transaction.openNested(context)) {
            return outputStacks(recipe, transaction);
        }
    }

    /**
     * Crafts the given recipe.
     * @param recipe The recipe to craft.
     * @param transaction The current transaction.
     */
    protected void craft(R recipe, TransactionContext transaction) {
        try (Transaction inner = Transaction.openNested(transaction)) {
            if (this.extractCraftingMaterials(recipe, inner)) {
                if (this.outputStacks(recipe, inner)) {
                    inner.commit();
                }
            }
        }

        recipe = this.findValidRecipe();
        if (recipe == null) this.resetRecipeProgress();
        else this.setRecipeAndProgress(recipe);
    }

    /**
     * Resets the progress of the machine.
     */
    protected void resetRecipeProgress() {
        this.activeRecipe(null);
        this.progress(0);
        this.maxProgress(0);
    }

    /**
     * Sets the recipe and resets progress of the machine.
     * @param recipe The recipe to set.
     */
    protected void setRecipeAndProgress(@NotNull R recipe) {
        this.activeRecipe(recipe);
        this.maxProgress(this.getProcessTime(recipe));
        this.progress(0);
    }

    /**
     * Returns the recipe type of the machine.
     * @return The recipe type of the machine.
     */
    public RecipeType<R> recipeType() {
        return this.recipeType;
    }

    /**
     * Finds the first valid recipe in the machine's inventory.
     * @return The first valid recipe in the machine's inventory.
     */
    protected @Nullable R findValidRecipe() {
        assert this.world != null;
        return this.world.getRecipeManager().getFirstMatch(this.recipeType(), this.craftingInv(), this.world).orElse(null);
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
     * @return The progress of the machine.
     */
    public int progress(int progress) {
        return this.progress = progress;
    }

    /**
     * Sets the maximum progress of the machine.
     * @param maxProgress The maximum progress to set.
     */
    public void maxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    /**
     * Returns the progress of the machine.
     * @return The progress of the machine.
     */
    public int progress() {
        return this.progress;
    }

    /**
     * Returns the active recipe of the machine.
     * @return The active recipe of the machine.
     */
    public @Nullable R activeRecipe() {
        return this.activeRecipe;
    }

    /**
     * Sets the active recipe of the machine.
     * @param activeRecipe The recipe to set.
     */
    protected void activeRecipe(@Nullable R activeRecipe) {
        this.activeRecipe = activeRecipe;
    }

    /**
     * Returns the maximum progress of the machine.
     * @return The maximum progress of the machine.
     */
    public int maxProgress() {
        return this.maxProgress;
    }

    /**
     * Returns whether the machine is currently processing a recipe.
     * @return Whether the machine is currently processing a recipe.
     */
    public boolean active() {
        return this.maxProgress > 0;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt(Constant.Nbt.PROGRESS, this.progress());
        nbt.putInt(Constant.Nbt.MAX_PROGRESS, this.maxProgress());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.progress(nbt.getInt(Constant.Nbt.PROGRESS));
        this.maxProgress(nbt.getInt(Constant.Nbt.MAX_PROGRESS));
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        this.activeRecipe(this.findValidRecipe());
    }
}
