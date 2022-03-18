/*
 * Copyright (c) 2019-2022 Team Galacticraft
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

package dev.galacticraft.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Objects;

public interface MachineLibGametest extends FabricGameTest {
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

    default void assertTrue(@NotNull TestContext context, boolean b, @NotNull String message) {
        if (!b) {
            context.throwGameTestException(format(message, true, false));
        }
    }

    default void assertFalse(@NotNull TestContext context, boolean b, @NotNull String message) {
        if (b) {
            context.throwGameTestException(format(message, false, true));
        }
    }

    default void assertEquals(@NotNull TestContext context, boolean a, boolean b, @NotNull String message) {
        if (a != b) {
            context.throwGameTestException(format(message, a, b));
        }
    }

    default void assertEquals(@NotNull TestContext context, byte a, byte b, @NotNull String message) {
        if (a != b) {
            context.throwGameTestException(format(message, a, b));
        }
    }

    default void assertEquals(@NotNull TestContext context, short a, short b, @NotNull String message) {
        if (a != b) {
            context.throwGameTestException(format(message, a, b));
        }
    }

    default void assertEquals(@NotNull TestContext context, int a, int b, @NotNull String message) {
        if (a != b) {
            context.throwGameTestException(format(message, a, b));
        }
    }

    default void assertEquals(@NotNull TestContext context, long a, long b, @NotNull String message) {
        if (a != b) {
            context.throwGameTestException(format(message, a, b));
        }
    }

    default void assertEquals(@NotNull TestContext context, float a, float b, @NotNull String message) {
        if (a != b) {
            context.throwGameTestException(format(message, a, b));
        }
    }

    default void assertEquals(@NotNull TestContext context, double a, double b, @NotNull String message) {
        if (a != b) {
            context.throwGameTestException(format(message, a, b));
        }
    }

    default void assertEquals(@NotNull TestContext context, Object a, Object b, @NotNull String message) {
        if (!Objects.equals(a, b)) {
            context.throwGameTestException(format(message, a, b));
        }
    }

    //apparently itemstack does not implement Object#equals()
    default void assertEquals(@NotNull TestContext context, ItemStack a, ItemStack b, @NotNull String message) {
        if (a == null || b == null || !ItemStack.canCombine(a, b) || a.getCount() != b.getCount()) {
            context.throwGameTestException(format(message, a, b));
        }
    }

    default void assertSame(@NotNull TestContext context, Object a, Object b, @NotNull String message) {
        if (a != b) {
            String aStr = "null";
            String bStr = "null";
            if (a != null) {
                aStr = a.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(a)) + "[" + a + "]";
            }
            if (b != null) {
                bStr = b.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(b)) + "[" + b + "]";
            }
            context.throwGameTestException(format(message, aStr, bStr));
        }
    }

    default <T extends Throwable> void assertThrows(@NotNull TestContext context, Class<T> clazz, Runnable runnable, @NotNull String message) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            if (!clazz.isInstance(throwable)) {
                GameTestException gameTestException = new GameTestException(format(message, clazz.getName(), throwable.getClass().getName()));
                gameTestException.addSuppressed(throwable);
                throw gameTestException;
            }
        }
    }

    default <T extends Throwable> void assertThrowsExactly(@NotNull TestContext context, Class<T> clazz, Runnable runnable, @NotNull String message) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            if (!clazz.equals(throwable.getClass())) {
                GameTestException gameTestException = new GameTestException(format(message, clazz.getName(), throwable.getClass().getName()));
                gameTestException.addSuppressed(throwable);
                throw gameTestException;
            }
        }
    }

    default String format(@NotNull String message, Object expected, Object found) {
        return message  + " [Expected: " + expected + ", Found: " + found + "] (Line: " + StackWalker.getInstance().walk(s -> s.skip(2).findFirst().map(StackWalker.StackFrame::getLineNumber).orElse(-1)) + ")";
    }
}
