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

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.gametest.annotation.BasicTest;
import dev.galacticraft.machinelib.api.gametest.annotation.MachineTest;
import dev.galacticraft.machinelib.impl.gametest.GameTestUtils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;

/**
 * A class for testing machines.
 *
 * @param <Machine> the type of machine block entity
 * @see BasicTest
 * @see MachineTest
 */
public abstract class MachineGameTest<Machine extends MachineBlockEntity> extends SimpleGameTest {
    public static final BlockPos MACHINE_POS = new BlockPos(1, 2, 1);
    private final Block block;

    protected MachineGameTest(Block block) {
        this.block = block;
    }

    @GameTestGenerator
    @MustBeInvokedByOverriders
    public @NotNull List<TestFunction> registerTests() {
        List<TestFunction> tests = super.registerTests();

        tests.add(this.createTest("place", STRUCTURE_3x3, 1, 1, helper -> {
            if (this.createMachine(helper) == null) {
                throw new GameTestAssertException("No machine associated with block!");
            }
            helper.succeed();
        }));

        for (Method method : this.getClass().getMethods()) {
            MachineTest machineTest = method.getAnnotation(MachineTest.class);
            if (machineTest != null) {
                tests.add(this.createTest(machineTest.batch(), machineTest.group(), method.getName(), machineTest.structure(), machineTest.workTime(), machineTest.setupTime(), helper -> {
                    Machine machine = this.createMachine(helper);

                    try {
                        Runnable runnable = GameTestUtils.invokeUnorderedArguments(this, method, machine, helper, new MachineTestContext(MACHINE_POS, helper));
                        if (runnable != null) {
                            helper.runAfterDelay(machineTest.workTime(), () -> {
                                try {
                                    runnable.run();
                                    helper.succeed();
                                } catch (AssertionError e) {
                                    GameTestUtils.unwrapAssertions(helper, MACHINE_POS, e, machineTest.captureAssertions());
                                }
                            });
                        }
                    } catch (AssertionError e) {
                        GameTestUtils.unwrapAssertions(helper, MACHINE_POS, e, machineTest.captureAssertions());
                    }
                }));
            }
        }

        return tests;
    }

    public TestFunction createChargeFromEnergyItemTest(int slot, Item energyProvider) {
        return this.createTest("chargeFromItem", STRUCTURE_3x3, 2, 1, helper -> {
            Machine machine = this.createMachine(helper);
            machine.itemStorage().slot(slot).set(energyProvider, 1);
            helper.runAfterDelay(1, () -> {
                if (machine.energyStorage().isEmpty()) {
                    helper.fail("Machine did not charge from the stack!", machine.getBlockPos());
                } else {
                    helper.succeed();
                }
            });
        });
    }

    public TestFunction createDrainToEnergyItemTest(int slot, Item energyConsumer) {
        return this.createTest("drainToItem", STRUCTURE_3x3, 2, 1, helper -> {
            Machine machine = this.createMachine(helper);

            machine.energyStorage().setEnergy(machine.energyStorage().getCapacity());
            machine.itemStorage().slot(slot).set(energyConsumer, 1);

            helper.runAfterDelay(1, () -> {
                if (machine.energyStorage().isFull()) {
                    helper.fail("Machine did not drain energy to the stack!", BlockPos.ZERO);
                } else {
                    helper.succeed();
                }
            });
        });
    }

    public TestFunction createTakeFromFluidItemTest(int slot, Item fluidProvider, int tank) {
        return this.createTest("takeFluidFromItem", STRUCTURE_3x3, 2, 1, helper -> {
            Machine machine = this.createMachine(helper);
            machine.itemStorage().slot(slot).set(fluidProvider, 1);
            helper.runAfterDelay(1, () -> {
                if (machine.fluidStorage().slot(tank).isEmpty()) {
                    helper.fail("Machine did not take fluid from the stack!", machine.getBlockPos());
                } else {
                    helper.succeed();
                }
            });
        });
    }

    public TestFunction createDrainFluidIntoItemTest(int slot, Fluid fluid, int tank) {
        return this.createTest("drainFluidIntoItem", STRUCTURE_3x3, 2, 1, helper -> {
            Machine machine = this.createMachine(helper);

            machine.fluidStorage().slot(tank).set(fluid, FluidConstants.BUCKET);
            machine.itemStorage().slot(slot).set(Items.BUCKET, 1);

            helper.runAfterDelay(1, () -> {
                if (machine.energyStorage().isFull()) {
                    helper.fail("Machine did not drain fluid into the stack!", BlockPos.ZERO);
                } else {
                    helper.succeed();
                }
            });
        });
    }

    /**
     * Creates a machine at the {@link #MACHINE_POS} position.
     *
     * @param helper the game test helper
     * @return the newly created machine
     */
    protected Machine createMachine(GameTestHelper helper) {
        helper.setBlock(MACHINE_POS, this.block);
        return helper.getBlockEntity(MACHINE_POS);
    }
}
