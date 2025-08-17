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

package dev.galacticraft.machinelib.testmod.gametest;

import dev.galacticraft.machinelib.api.gametest.MachineGameTest;
import dev.galacticraft.machinelib.api.gametest.TestUtils;
import dev.galacticraft.machinelib.api.gametest.annotation.TestInfo;
import dev.galacticraft.machinelib.api.gametest.annotation.timing.Oneshot;
import dev.galacticraft.machinelib.api.gametest.annotation.type.Machine;
import dev.galacticraft.machinelib.testmod.block.TestModBlocks;
import dev.galacticraft.machinelib.testmod.block.entity.GeneratorBlockEntity;
import dev.galacticraft.machinelib.testmod.item.TestModItems;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.Items;

import java.util.List;

@TestInfo(group = "generator")
public class GeneratorTests extends MachineGameTest<GeneratorBlockEntity> {
    public GeneratorTests() {
        super(TestModBlocks.GENERATOR);
    }

    @Machine
    @Oneshot(time = 10)
    public Runnable generatePower(GeneratorBlockEntity machine) {
        machine.itemStorage().slot(GeneratorBlockEntity.FUEL_SLOT).set(Items.COAL, 1);
        return () -> {
            if (machine.energyStorage().getAmount() != GeneratorBlockEntity.GENERATION_RATE * 10) {
                throw new GameTestAssertException(String.format("Failed to generate power (%s / %s)!", machine.energyStorage().getAmount(), GeneratorBlockEntity.GENERATION_RATE * 10));
            }
        };
    }

    @GameTestGenerator
    public List<TestFunction> generateTests() {
        List<TestFunction> tests = TestUtils.generateTests(this);
        tests.add(this.createDrainToEnergyItemTest(GeneratorBlockEntity.BATTERY_SLOT, TestModItems.BASIC_BATTERY));
        return tests;
    }
}
