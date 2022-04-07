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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public abstract class RecipeMachineBlockEntity<C extends Inventory, R extends Recipe<C>> extends MachineBlockEntity {
    private final @NotNull RecipeType<R> recipeType;

    private int inventoryModCount = -1;
    private @Nullable R activeRecipe = null;
    private int progress = 0;
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

    protected abstract @NotNull MachineStatus workingStatus();

    protected @Nullable MachineStatus extractResourcesToWork(@NotNull TransactionContext context) {
        return null;
    }

    @Override
    public @NotNull MachineStatus tick() {
        if (this.inventoryModCount != this.itemStorage().getModCount()) {
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
        }
        if (this.activeRecipe != null) {
            try (Transaction transaction = Transaction.openOuter()) {
                MachineStatus status = this.extractResourcesToWork(transaction);
                if (status == null) {
                    if (++this.progress >= this.getMaxProgress()) {
                        this.craft(this.activeRecipe, transaction);
                    }
                    transaction.commit();
                    return this.workingStatus();
                } else {
                    return status;
                }
            }
        } else {
            if (this.getStatus() == MachineStatuses.OUTPUT_FULL) return MachineStatuses.OUTPUT_FULL; //preserve full state
            return MachineStatuses.INVALID_RECIPE;
        }
    }


    private void updateRecipe(R recipe) {
        if (this.getActiveRecipe() != recipe) {
            this.setActiveRecipe(recipe);
            this.setMaxProgress(this.getProcessTime(recipe));
            this.setProgress(0);
        }
    }

    /**
     * Crafts the given recipe.
     * @param recipe The recipe to craft.
     * @param transaction The current transaction.
     */
    protected void craft(R recipe, @Nullable TransactionContext transaction) {
        try (Transaction inner = Transaction.openNested(transaction)) {
            if (this.extractCraftingMaterials(recipe, inner)) {
                if (this.outputStacks(recipe, inner)) {
                    inner.commit();
                }
            }
        }
    }

    /**
     * Resets the progress of the machine.
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
     * Returns the active recipe of the machine.
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
