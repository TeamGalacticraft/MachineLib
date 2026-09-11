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

package dev.galacticraft.machinelib.api.gametest;

import dev.galacticraft.machinelib.api.block.entity.RecipeMachineBlockEntity;
import dev.galacticraft.machinelib.api.gametest.annotation.MachineTest;
import dev.galacticraft.machinelib.api.gametest.recipe.IngredientSupplier;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Provides a base implementation for testing recipe-based machines.
 *
 * @param <I> the type of container used by the recipe
 * @param <R> the type of recipe used by the recipe
 * @param <Machine> the type of machine used in the game test
 * @see RecipeMachineBlockEntity
 */
public abstract class RecipeGameTest<I extends RecipeInput, R extends Recipe<I>, Machine extends RecipeMachineBlockEntity<I, R>> extends MachineGameTest<Machine> {
    protected final int recipeRuntime;
    private final int outputSlotsStart;
    private final int outputSlotsLength;
    private final List<IngredientSupplier<I, R, Machine>> conditions;

    protected RecipeGameTest(@NotNull Block block, List<IngredientSupplier<I, R, Machine>> conditions, int recipeRuntime) {
        this(block, conditions, -1, 0, recipeRuntime);
    }

    protected RecipeGameTest(@NotNull Block block, List<IngredientSupplier<I, R, Machine>> conditions, int outputSlot, int recipeRuntime) {
        this(block, conditions, outputSlot, 1, recipeRuntime);
    }

    protected RecipeGameTest(@NotNull Block block, List<IngredientSupplier<I, R, Machine>> conditions, int outputSlotsStart, int outputSlotsLength, int recipeRuntime) {
        super(block);

        this.outputSlotsStart = outputSlotsStart;
        this.outputSlotsLength = outputSlotsLength;
        this.recipeRuntime = recipeRuntime;
        this.conditions = conditions;
    }

    protected boolean anyRecipeCrafted(@NotNull MachineItemStorage storage) {
        for (int i = 0; i < this.outputSlotsLength; i++) {
            if (!storage.slot(this.outputSlotsStart + i).isEmpty()) return true;
        }
        return false;
    }

    protected void fillOutputSlots(@NotNull MachineItemStorage storage) {
        for (int i = 0; i < this.outputSlotsLength; i++) {
            storage.slot(this.outputSlotsStart + i).set(Items.BARRIER, 1);
        }
    }

    protected void fulfillRunConditions(Machine machine) {
        for (IngredientSupplier<I, R, Machine> condition : this.conditions) {
            condition.supplyIngredient(machine);
        }
    }

    @MachineTest(group = "recipe")
    public Runnable initialize(Machine machine) {
        this.fulfillRunConditions(machine);
        return () -> {
            if (machine.getActiveRecipe() == null) {
                throw new GameTestAssertException("Failed to find recipe!");
            }
        };
    }

    @MachineTest(group = "recipe")
    public Runnable full(Machine machine) {
        this.fulfillRunConditions(machine);
        this.fillOutputSlots(machine.itemStorage());

        return () -> {
            if (machine.getActiveRecipe() != null) {
                throw new GameTestAssertException("Crafting something despite the output being full!");
            }
        };
    }

    protected void tryCraft(GameTestHelper helper) {
        Machine machine = createMachine(helper);

        fulfillRunConditions(machine);

        helper.runAfterDelay(this.recipeRuntime, () -> {
            if (!this.anyRecipeCrafted(machine.itemStorage())) {
                helper.fail("Failed to craft recipe!", machine.getBlockPos());
            } else {
                helper.succeed();
            }
        });
    }

    protected void tryCraftPartial(GameTestHelper helper, int ignored) {
        Machine machine = createMachine(helper);

        for (int i = 0; i < this.conditions.size(); i++) {
            if (i == ignored) continue;
            this.conditions.get(i).supplyIngredient(machine);
        }

        helper.runAfterDelay(this.recipeRuntime, () -> {
            if (this.anyRecipeCrafted(machine.itemStorage())) {
                helper.fail("Partial recipe %s crafted!".formatted(ignored), machine.getBlockPos());
            } else {
                helper.succeed();
            }
        });
    }

    @Override
    @MustBeInvokedByOverriders
    @GameTestGenerator
    public @NotNull List<TestFunction> registerTests() {
        List<TestFunction> tests = super.registerTests();
        // variable runtime
        tests.add(this.createTest("recipe", "craft", STRUCTURE_3x3, this.recipeRuntime, 1, this::tryCraft));

        for (int i = 0; i < this.conditions.size(); i++) {
            int finalI = i;
            tests.add(this.createTest("recipe", "craft.partial." + i, STRUCTURE_3x3, this.recipeRuntime, 1, helper -> this.tryCraftPartial(helper, finalI)));
        }
        return tests;
    }
}
