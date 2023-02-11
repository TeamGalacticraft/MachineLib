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
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.Container;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Rotation;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class RecipeGameTest<C extends Container, R extends Recipe<C>, Machine extends RecipeMachineBlockEntity<C, R>> extends MachineGameTest<Machine> {
    private final @Nullable SlotGroupType inputSlots;
    private final @Nullable SlotGroupType outputSlots;

    protected RecipeGameTest(@NotNull MachineType<Machine, ?> type, @Nullable SlotGroupType inputSlots, @Nullable SlotGroupType outputSlots) {
        super(type);
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
    }

    protected abstract void fulfillRunRequirements(@NotNull Machine machine);
    protected abstract int getRecipeRuntime();
    protected abstract void createValidRecipe(@NotNull MachineItemStorage storage);

    protected boolean recipeCrafted(@NotNull MachineItemStorage storage) {
        assert this.outputSlots != null;
        return !storage.getGroup(this.outputSlots).isEmpty();
    }

    protected void createInvalidRecipe(@NotNull MachineItemStorage storage) {
        assert this.inputSlots != null;
        for (ItemResourceSlot itemResourceSlot : storage.getGroup(this.inputSlots)) {
            itemResourceSlot.set(Items.BARRIER, 1);
        }
    }

    protected void fillOutputSlots(@NotNull MachineItemStorage storage) {
        assert this.outputSlots != null;
        for (ItemResourceSlot itemResourceSlot : storage.getGroup(this.outputSlots)) {
            itemResourceSlot.set(Items.BARRIER, 1);
        }
    }

    @InstantTest(group = "recipe")
    public Runnable initialize(Machine machine) {
        this.fulfillRunRequirements(machine);
        this.createValidRecipe(machine.itemStorage());
        return () -> {
            if (machine.getMaxProgress() == 0) {
                throw new AssertionError("Failed to find recipe!");
            }
        };
    }

    @InstantTest(group = "recipe")
    public Runnable invalid(Machine machine) {
        this.fulfillRunRequirements(machine);
        this.createInvalidRecipe(machine.itemStorage());
        return () -> {
            if (machine.getMaxProgress() != 0) {
                throw new AssertionError("Crafting something despite recipe being invalid!");
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
                throw new AssertionError("Crafting something despite the output being full!");
            }
        };
    }

    @InstantTest(group = "recipe")
    public Runnable recipe_no_resources(Machine machine) {
        this.createValidRecipe(machine.itemStorage());
        return () -> {
            if (machine.getMaxProgress() != 0) {
                throw new AssertionError("Machine is crafting a recipe, despite not having relevant resources (e.g. energy)!");
            }
        };
    }

    @InstantTest(group = "recipe")
    public Runnable resources_no_recipe(Machine machine) {
        this.fulfillRunRequirements(machine);
        return () -> {
            if (machine.getMaxProgress() != 0) {
                throw new AssertionError("Machine is doing something, despite not having a recipe!");
            }
        };
    }

    @Override
    @MustBeInvokedByOverriders
    public @NotNull List<TestFunction> generateTests() {
        List<TestFunction> functions = super.generateTests();
        functions.add(new TestFunction(this.getBaseId() + "/recipe", "craft", EMPTY_STRUCTURE, Rotation.NONE, this.getRecipeRuntime(), 1, true, 1, 1, helper -> {
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
