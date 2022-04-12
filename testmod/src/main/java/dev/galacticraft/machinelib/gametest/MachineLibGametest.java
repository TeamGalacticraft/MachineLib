/*
 * Copyright (c) 2021-2022 Team Galacticraft
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

package dev.galacticraft.machinelib.gametest;

import dev.galacticraft.impl.fluid.FluidStack;
import dev.galacticraft.impl.gas.GasStack;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Objects;

public interface MachineLibGametest extends FabricGameTest {
    String EMPTY_STRUCTURE = "machinelib-test:empty";

    default void beforeEach(TestContext context) {
    }

    default void afterEach(TestContext context) {
    }

    @Override
    default void invokeTestMethod(TestContext context, Method method) {
        method.setAccessible(true);
        GameTest annotation = method.getAnnotation(GameTest.class);
        if (annotation == null) throw new AssertionError("Test method without gametest annotation?!");
        if (annotation.tickLimit() == 0) {
            context.addInstantFinalTask(() -> {
                beforeEach(context);
                FabricGameTest.super.invokeTestMethod(context, method);
                afterEach(context);
            });
        } else {
            beforeEach(context);
            FabricGameTest.super.invokeTestMethod(context, method);
            afterEach(context);
        }
    }

    default void assertTrue(boolean b, @NotNull String message) {
        if (!b) {
            throw new GameTestException(format(message, true, false, 1));
        }
    }

    default void assertFalse(boolean b, @NotNull String message) {
        if (b) {
            throw new GameTestException(format(message, false, true, 1));
        }
    }

    default void assertEquals(boolean a, boolean b, @NotNull String message) {
        if (a != b) {
            throw new GameTestException(format(message, a, b, 1));
        }
    }

    default void assertEquals(byte a, byte b, @NotNull String message) {
        if (a != b) {
            throw new GameTestException(format(message, a, b, 1));
        }
    }

    default void assertEquals(short a, short b, @NotNull String message) {
        if (a != b) {
            throw new GameTestException(format(message, a, b, 1));
        }
    }

    default void assertEquals(int a, int b, @NotNull String message) {
        if (a != b) {
            throw new GameTestException(format(message, a, b, 1));
        }
    }

    default void assertEquals(long a, long b, @NotNull String message) {
        if (a != b) {
            throw new GameTestException(format(message, a, b, 1));
        }
    }

    default void assertEquals(float a, float b, @NotNull String message) {
        if (a != b) {
            throw new GameTestException(format(message, a, b, 1));
        }
    }

    default void assertEquals(double a, double b, @NotNull String message) {
        if (a != b) {
            throw new GameTestException(format(message, a, b, 1));
        }
    }

    default void assertEquals(Object a, Object b, @NotNull String message) {
        if (!Objects.equals(a, b)) {
            throw new GameTestException(format(message, a, b, 1));
        }
    }

    //apparently itemstack does not implement Object#equals()
    default void assertEquals(ItemStack a, ItemStack b, @NotNull String message) {
        if (a == null || b == null || !ItemStack.canCombine(a, b) || a.getCount() != b.getCount()) {
            throw new GameTestException(format(message, a, b, 1));
        }
    }

    default void assertEquals(FluidStack a, FluidStack b, @NotNull String message) {
        if (a == null || b == null || !FluidStack.canCombine(a, b) || a.getAmount() != b.getAmount()) {
            throw new GameTestException(format(message, a, b, 1));
        }
    }

    default void assertEquals(GasStack a, GasStack b, @NotNull String message) {
        if (a == null || b == null || !GasStack.canCombine(a, b) || a.getAmount() != b.getAmount()) {
            throw new GameTestException(format(message, a, b, 1));
        }
    }

    default void assertSame(Object a, Object b, @NotNull String message) {
        if (a != b) {
            String aStr = "null";
            String bStr = "null";
            if (a != null) {
                aStr = a.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(a)) + "[" + a + "]";
            }
            if (b != null) {
                bStr = b.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(b)) + "[" + b + "]";
            }
            throw new GameTestException(format(message, aStr, bStr, 1));
        }
    }

    default <T extends Throwable> void assertThrows(Class<T> clazz, Runnable runnable, @NotNull String message) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            if (!clazz.isInstance(throwable)) {
                GameTestException gameTestException = new GameTestException(format(message, clazz.getName(), throwable.getClass().getName(), 1));
                gameTestException.addSuppressed(throwable);
                throw gameTestException;
            } else {
                return;
            }
        }
        throw new GameTestException(format(message, clazz.getName(), "<none>", 1));
    }

    default <T extends Throwable> void assertThrowsExactly(Class<T> clazz, Runnable runnable, @NotNull String message) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            if (!clazz.equals(throwable.getClass())) {
                GameTestException gameTestException = new GameTestException(format(message, clazz.getName(), throwable.getClass().getName(), 1));
                gameTestException.addSuppressed(throwable);
                throw gameTestException;
            }
        }
    }

    default String format(@NotNull String message, Object expected, Object found, int depth) {
        return message  + " [Expected: " + expected + ", Found: " + found + "] (Line: " + StackWalker.getInstance().walk(s -> s.skip(depth + 1).findFirst().map(StackWalker.StackFrame::getLineNumber).orElse(-1)) + ")";
    }
}
