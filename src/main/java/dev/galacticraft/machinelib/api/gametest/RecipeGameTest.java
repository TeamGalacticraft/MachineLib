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

package dev.galacticraft.machinelib.api.gametest;

import dev.galacticraft.machinelib.api.block.entity.RecipeMachineBlockEntity;
import dev.galacticraft.machinelib.api.gametest.annotation.InstantTest;
import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.SlotProvider;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Rotation;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class RecipeGameTest<C extends Container, R extends Recipe<C>, Machine extends RecipeMachineBlockEntity<C, R>> extends MachineGameTest<Machine> {
    private final @NotNull SlotProvider<Item, ItemResourceSlot> inputSlots;
    private final @NotNull SlotProvider<Item, ItemResourceSlot> outputSlots;

    protected RecipeGameTest(@NotNull MachineType<Machine, ?> type, @NotNull SlotProvider<Item, ItemResourceSlot> inputSlots, @NotNull SlotProvider<Item, ItemResourceSlot> outputSlots) {
        super(type);
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
    }

    protected abstract void fulfillRunRequirements(@NotNull Machine machine);
    protected abstract int getRecipeRuntime();
    protected abstract void createValidRecipe(@NotNull MachineItemStorage storage);

    protected boolean recipeCrafted(@NotNull MachineItemStorage storage) {
        return !this.outputSlots.isEmpty();
    }

    protected void createInvalidRecipe(@NotNull MachineItemStorage storage) {
        for (ItemResourceSlot itemResourceSlot : this.inputSlots) {
            itemResourceSlot.set(Items.BARRIER, 1);
        }
    }

    protected void fillOutputSlots(@NotNull MachineItemStorage storage) {
        for (ItemResourceSlot itemResourceSlot : this.outputSlots) {
            itemResourceSlot.set(Items.BARRIER, 1);
        }
    }

    @InstantTest(group = "recipe")
    public Runnable initialize(Machine machine) {
        this.fulfillRunRequirements(machine);
        this.createValidRecipe(machine.itemStorage());
        return () -> {
            if (machine.getMaxProgress() == 0) {
                throw new GameTestAssertException("Failed to find recipe!");
            }
        };
    }

    @InstantTest(group = "recipe")
    public Runnable invalid(Machine machine) {
        this.fulfillRunRequirements(machine);
        this.createInvalidRecipe(machine.itemStorage());
        return () -> {
            if (machine.getMaxProgress() != 0) {
                throw new GameTestAssertException("Crafting something despite recipe being invalid!");
            }
        };
    }

    @InstantTest(group = "recipe")
    public Runnable full(Machine machine) {
        this.fulfillRunRequirements(machine);
        this.fillOutputSlots(machine.itemStorage());
        this.createValidRecipe(machine.itemStorage());
        return () -> {
            if (machine.getMaxProgress() != 0) {
                throw new GameTestAssertException("Crafting something despite the output being full!");
            }
        };
    }

//    @InstantTest(group = "recipe")
//    public Runnable recipeNoResources(Machine machine) {
//        this.createValidRecipe(machine.itemStorage());
//        return () -> {
//            if (machine.getMaxProgress() != 0) { //fixme: recipe detection works differently
//                throw new GameTestAssertException("Machine is crafting a recipe, despite not having relevant resources (e.g. energy)!");
//            }
//        };
//    }

    @InstantTest(group = "recipe")
    public Runnable resourcesNoRecipe(Machine machine) {
        this.fulfillRunRequirements(machine);
        return () -> {
            if (machine.getMaxProgress() != 0) {
                throw new GameTestAssertException("Machine is doing something, despite not having a recipe!");
            }
        };
    }

    @Override
    @MustBeInvokedByOverriders
    public @NotNull List<TestFunction> generateTests() {
        List<TestFunction> functions = super.generateTests();
        functions.add(new TestFunction(this.getBaseId(), this.getBaseId() + ".craft", EMPTY_STRUCTURE, Rotation.NONE, this.getRecipeRuntime(), 1, true, 1, 1, helper -> {
            Machine machine = createMachine(helper);
            this.fulfillRunRequirements(machine);
            this.createValidRecipe(machine.itemStorage());
            helper.runAfterDelay(this.getRecipeRuntime(), () -> {
                if (!this.recipeCrafted(machine.itemStorage())) {
                    helper.fail("Failed to craft recipe!", BlockPos.ZERO);
                }
                helper.succeed();
            });
        }));
        return functions;
    }
}
