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

package dev.galacticraft.machinelib.api.gametest;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.gametest.annotation.Magic;
import dev.galacticraft.machinelib.api.gametest.util.GameTestStructures;
import dev.galacticraft.machinelib.impl.gametest.GameTestUtils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;


/**
 * A class for testing machines.
 *
 * @param <Machine> the type of machine block entity
 */
public abstract class MachineGameTest<Machine extends MachineBlockEntity> extends SimpleGameTest {
    @Magic("machine")
    private final Block block;

    protected MachineGameTest(Block block) {
        this.block = block;
    }

    public TestFunction createChargeFromEnergyItemTest(int slot, Item energyProvider) {
        return GameTestUtils.createAdditionalTest(this.getClass(), "chargeFromItem", GameTestStructures.EMPTY_1x1, 1, 0, helper -> {
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
        return GameTestUtils.createAdditionalTest(this.getClass(), "drainToItem", GameTestStructures.EMPTY_1x1, 1, 0, helper -> {
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
        return GameTestUtils.createAdditionalTest(this.getClass(), "takeFluidFromItem", GameTestStructures.EMPTY_1x1, 1, 0, helper -> {
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
        return GameTestUtils.createAdditionalTest(this.getClass(), "drainFluidIntoItem", GameTestStructures.EMPTY_1x1, 1, 0, helper -> {
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
     * Creates a machine at the center floor position.
     *
     * @param helper the game test helper
     * @return the newly created machine
     */
    protected Machine createMachine(GameTestHelper helper) {
        BlockPos pos = TestUtils.getCenterFloor(helper);
        helper.setBlock(pos, this.block);
        return helper.getBlockEntity(pos);
    }
}
