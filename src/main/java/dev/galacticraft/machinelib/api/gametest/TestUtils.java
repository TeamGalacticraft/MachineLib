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

import dev.galacticraft.machinelib.api.gametest.annotation.AfterEach;
import dev.galacticraft.machinelib.api.gametest.annotation.BeforeEach;
import dev.galacticraft.machinelib.api.gametest.annotation.TestProvider;
import dev.galacticraft.machinelib.api.gametest.annotation.timing.Oneshot;
import dev.galacticraft.machinelib.api.gametest.annotation.timing.Timed;
import dev.galacticraft.machinelib.api.gametest.annotation.type.Matrix;
import dev.galacticraft.machinelib.impl.gametest.GameTestUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TestUtils {
    public static @NotNull List<TestFunction> generateTests(Object base) {
        return generateTests(base.getClass(), base);
    }

    public static @NotNull List<TestFunction> generateTests(Class<?> clazz, Object base) {
        List<Method> before = new ArrayList<>();
        List<Method> after = new ArrayList<>();

        processFunctions(clazz, before, BeforeEach.class);
        processFunctions(clazz, after, AfterEach.class);

        List<TestFunction> tests = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            generateTest(tests, base, base, clazz, method, before, after);
        }
        Class<?> sc = clazz.getSuperclass();
        while (sc != Object.class) {
            for (Method method : sc.getMethods()) {
                if (method.isAnnotationPresent(TestProvider.class)) {
                    method.setAccessible(true);
                    try {
                        method.invoke(base, tests);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    generateTest(tests, base, base, sc, method, before, after);
                }
            }
            sc = sc.getSuperclass();
        }
        for (Class<?> aClass : clazz.getClasses()) {
            generateTests(tests, base, base, aClass, new ArrayList<>(before), new ArrayList<>(after));
        }
        return tests;
    }

    private static <T extends Annotation> void processFunctions(Class<?> clazz, List<Method> methods, Class<T> annotationClass) {
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(annotationClass)) {
                methods.add(method);
            }
        }
    }

    private static void generateTests(List<TestFunction> tests, Object root, Object parent, Class<?> clazz, List<Method> before, List<Method> after) {
        Object base = maybeConstruct(clazz, parent);

        processFunctions(clazz, before, BeforeEach.class);
        processFunctions(clazz, after, AfterEach.class);

        for (Method method : clazz.getMethods()) {
            generateTest(tests, root, base, clazz, method, before, after);
        }

        for (Class<?> aClass : clazz.getClasses()) {
            generateTests(tests, root, base, aClass, new ArrayList<>(before), new ArrayList<>(after));
        }
    }

    private static Object maybeConstruct(Class<?> clazz, Object parent) {
        try {
            try {
                Constructor<?> cons = clazz.getDeclaredConstructor();
                cons.setAccessible(true);
                return cons.newInstance();
            } catch (NoSuchMethodException e) {
                if (parent != null) {
                    Constructor<?> cons = clazz.getDeclaredConstructor(parent.getClass());
                    cons.setAccessible(true);
                    return cons.newInstance(parent);
                }
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ignore) {}
        return null;
    }

    public static void generateTest(List<TestFunction> tests, Object root, Object obj, Class<?> clazz, Method method, List<Method> before, List<Method> after) {
        int max = 0;
        long setup;

        if (method.isAnnotationPresent(Timed.class)) {
            Timed timed = method.getAnnotation(Timed.class);
            for (int i : timed.delay()) {
                max += i;
            }
            setup = timed.setup();
        } else if (method.isAnnotationPresent(Oneshot.class)) {
            Oneshot oneshot = method.getAnnotation(Oneshot.class);
            assert oneshot != null;
            max = oneshot.time();
            setup = oneshot.setup();
        } else {
            return;
        }

        if (method.isAnnotationPresent(Matrix.class)) {
            Matrix matrix = method.getAnnotation(Matrix.class);
            assert matrix != null;
            Object magic = GameTestUtils.getMagic(clazz, obj, matrix.value());
            if (magic instanceof Object[] values) {
                for (int i = 0; i < values.length; i++) {
                    tests.add(GameTestUtils.createMatrixTest(root, method, Objects.toString(values[i]), max, setup, GameTestUtils.invokeWithTiming(method, obj, before, after, i)));
                }
            } else {
                throw new IllegalArgumentException("Expected array of values for matrix");
            }
        }

        tests.add(GameTestUtils.createTest(root, method, max, setup, GameTestUtils.invokeWithTiming(method, obj, before, after, 0)));

    }

    public static @NotNull BlockPos getCenter(GameTestHelper helper) {
        return new BlockPos(Mth.floor(helper.getBounds().getXsize() / 2), Mth.floor(helper.getBounds().getYsize() / 2) + 1, Mth.floor(helper.getBounds().getZsize() / 2));
    }

    public static @NotNull BlockPos getCenterFloor(GameTestHelper helper) {
        return new BlockPos(Mth.floor(helper.getBounds().getXsize() / 2), 1, Mth.floor(helper.getBounds().getZsize() / 2));
    }
}
