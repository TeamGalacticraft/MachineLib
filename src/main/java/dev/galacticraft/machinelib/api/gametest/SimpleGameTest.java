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

import dev.galacticraft.machinelib.api.gametest.annotation.BasicTest;
import dev.galacticraft.machinelib.api.gametest.annotation.MachineTest;
import dev.galacticraft.machinelib.api.gametest.annotation.TestSuite;
import dev.galacticraft.machinelib.impl.gametest.GameTestUtils;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.Rotation;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A gametest helper class that can be used to create tests with a single method.
 *
 * @see BasicTest
 * @see MachineTest
 */
public abstract class SimpleGameTest implements FabricGameTest {
    public static final String STRUCTURE_3x3 = "machinelib:3x3";

    @MustBeInvokedByOverriders
    @GameTestGenerator
    public @NotNull List<TestFunction> registerTests() {
        List<TestFunction> tests = new ArrayList<>();

        for (Method method : this.getClass().getMethods()) {
            BasicTest basicTest = method.getAnnotation(BasicTest.class);
            if (basicTest != null) {
                tests.add(this.createTest(basicTest.batch(), basicTest.group(), method.getName(), basicTest.structure(), basicTest.workTime(), basicTest.setupTime(), helper -> {
                    Runnable runnable = GameTestUtils.invokeUnorderedArguments(this, method, helper);
                    if (runnable == null) {
                        if (basicTest.setupTime() == 1) helper.succeed();
                    } else {
                        helper.runAfterDelay(basicTest.setupTime(), () -> {
                            runnable.run();
                            if (basicTest.workTime() == 1) helper.succeed();
                        });
                    }
                }));
            }
        }

        return tests;
    }

    @Override
    public void invokeTestMethod(GameTestHelper context, Method method) {
        // don't invoke fabric if there is no annotation (generated test)
        if (method.getAnnotation(GameTest.class) != null) FabricGameTest.super.invokeTestMethod(context, method);
    }

    protected TestFunction createTest(String name, String structure, int ticks, int setupTicks, Consumer<GameTestHelper> test) {
        return this.createTest(null, name, structure, ticks, setupTicks, test);
    }

    protected TestFunction createTest(@Nullable String group, String name, String structure, int ticks, int setupTicks, Consumer<GameTestHelper> test) {
        return this.createTest(null, group, name, structure, ticks, setupTicks, test);
    }

    protected TestFunction createTest(@Nullable String batch, @Nullable String group, String name, String structure, int ticks, int setupTicks, Consumer<GameTestHelper> test) {
        batch = batch == null || batch.isBlank() ? "defaultBatch" : batch;
        return new TestFunction(
                batch,
                GameTestUtils.generateTestName(batch, group == null || group.isBlank() ? getDefaultTestGroup() : group, name),
                structure,
                Rotation.NONE,
                ticks,
                setupTicks,
                true,
                test
        );
    }

    private String getDefaultTestGroup() {
        TestSuite annotation = this.getClass().getAnnotation(TestSuite.class);
        return annotation != null ? annotation.value() : null;
    }
}
