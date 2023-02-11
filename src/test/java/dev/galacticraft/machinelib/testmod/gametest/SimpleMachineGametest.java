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

package dev.galacticraft.machinelib.testmod.gametest;

import dev.galacticraft.machinelib.api.gametest.MachineGameTest;
import dev.galacticraft.machinelib.api.gametest.annotation.InstantTest;
import dev.galacticraft.machinelib.api.gametest.annotation.ProcessingTest;
import dev.galacticraft.machinelib.testmod.block.TestModMachineTypes;
import dev.galacticraft.machinelib.testmod.block.entity.SimpleMachineBlockEntity;
import dev.galacticraft.machinelib.testmod.item.TestModItems;
import dev.galacticraft.machinelib.testmod.slot.TestModSlotGroupTypes;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SimpleMachineGametest extends MachineGameTest<SimpleMachineBlockEntity> {
    public SimpleMachineGametest() {
        super(TestModMachineTypes.SIMPLE_MACHINE);
    }

    @InstantTest(requiresEnergy = true)
    public Runnable detect(SimpleMachineBlockEntity machine) {
        machine.itemStorage().getSlot(TestModSlotGroupTypes.DIRT).set(Items.DIRT, 1);
        return () -> {
            if (machine.ticks == -1) throw new GameTestAssertException("machine not working");
        };
    }

    @ProcessingTest(workTime = 5 * 20 + 1, requiresEnergy = true)
    public void craft(SimpleMachineBlockEntity machine, GameTestHelper helper) {
        machine.itemStorage().getSlot(TestModSlotGroupTypes.DIRT).set(Items.DIRT, 1);
        helper.runAfterDelay(5 * 20 + 1, () -> {
            if (machine.itemStorage().getGroup(TestModSlotGroupTypes.DIAMONDS).isEmpty()) throw new GameTestAssertException("machine did not craft");
            helper.succeed();
        });
    }

    @Override
    @GameTestGenerator
    public @NotNull List<TestFunction> generateTests() {
        List<TestFunction> functions = super.generateTests();
        functions.add(this.createChargeFromEnergyItemTest(TestModSlotGroupTypes.CHARGE, TestModItems.INFINITE_BATTERY));
        return functions;
    }
}
