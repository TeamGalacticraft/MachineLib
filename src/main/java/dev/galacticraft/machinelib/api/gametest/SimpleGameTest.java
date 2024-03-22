/*
 * Copyright (c) 2021-2024 Team Galacticraft
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
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Rotation;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @see BasicTest
 * @see MachineTest
 */
public abstract class SimpleGameTest implements FabricGameTest {
    private static final char SEPARATOR = '.';
    public static final String STRUCTURE_3x3 = "machinelib:3x3";

    @MustBeInvokedByOverriders
    @GameTestGenerator
    public @NotNull List<TestFunction> registerTests() {
        List<TestFunction> tests = new ArrayList<>();

        for (Method method : this.getClass().getMethods()) {
            BasicTest basicTest = method.getAnnotation(BasicTest.class);
            if (basicTest != null) {
                tests.add(this.createTest(basicTest.batch(), basicTest.group(), method.getName(), basicTest.structure(), basicTest.workTime(), basicTest.setupTime(), helper -> {
                    Runnable runnable = this.invokeTestMethod(method, helper);
                    if (runnable == null) {
                        if (basicTest.workTime() == 1) helper.succeed();
                    } else {
                        helper.runAfterDelay(1, () -> {
                            runnable.run();
                            helper.succeed();
                        });
                    }
                }));
            }
        }

        return tests;
    }

    protected String getTestBatch() {
        TestSuite annotation = this.getClass().getAnnotation(TestSuite.class);
        if (annotation != null) return annotation.value();
        return "defaultBatch";
    }

    @Override
    public void invokeTestMethod(GameTestHelper context, Method method) {
        // don't invoke fabric if there is no annotation (generated test)
        if (method.getAnnotation(GameTest.class) != null) FabricGameTest.super.invokeTestMethod(context, method);
    }

    private static String generateTestName(@Nullable String batch, @Nullable String group, @NotNull String name) {
        if (batch == null || batch.isBlank() || batch.equals("defaultBatch")) {
            if (group == null || group.isBlank()) return name;
            return group + SEPARATOR + name;
        }
        if (group == null || group.isBlank()) return batch + SEPARATOR + name;
        return batch + SEPARATOR + group + SEPARATOR + name;
    }

    protected <T> T invokeTestMethod(Method method, Object... args) {
        method.setAccessible(true);

        try {
            int params = method.getParameters().length;
            if (params > args.length) throw new IllegalArgumentException("Invalid number of arguments!");
            if (params < args.length) {
                Object[] newArgs = new Object[params];
                System.arraycopy(args, 0, newArgs, 0, params);
                args = newArgs;
            }

            return (T) method.invoke(this, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to invoke test method!", e);
        } catch (InvocationTargetException t) {
            Throwable inner = t;
            while (inner != null) {
                if (inner instanceof GameTestAssertException) {
                    throw (GameTestAssertException) inner;
                }
                inner = inner.getCause();
            }

            if (t.getCause() instanceof RuntimeException rt){
                throw rt;
            } else {
                throw new RuntimeException(t);
            }
        } catch (ClassCastException ex) {
            throw new RuntimeException("Failed to cast test method return value!", ex);
        }
    }

    protected TestFunction createTest(String name, String structure, int ticks, int setupTicks, Consumer<GameTestHelper> test) {
        return this.createTest(null, name, structure, ticks, setupTicks, test);
    }

    protected TestFunction createTest(@Nullable String group, String name, String structure, int ticks, int setupTicks, Consumer<GameTestHelper> test) {
        return this.createTest(null, group, name, structure, ticks, setupTicks, test);
    }

    protected TestFunction createTest(@Nullable String batch, @Nullable String group, String name, String structure, int ticks, int setupTicks, Consumer<GameTestHelper> test) {
        return new TestFunction(
                batch == null || batch.isBlank() ? this.getTestBatch() : batch,
                generateTestName(batch == null || batch.isBlank() ? this.getTestBatch() : batch, group, name),
                structure,
                Rotation.NONE,
                ticks,
                setupTicks,
                true,
                1,
                1,
                test
        );
    }
}
