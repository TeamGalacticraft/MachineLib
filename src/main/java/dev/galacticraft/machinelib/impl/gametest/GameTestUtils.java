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

package dev.galacticraft.machinelib.impl.gametest;

import com.google.common.collect.Lists;
import dev.galacticraft.machinelib.api.gametest.TestModifiers;
import dev.galacticraft.machinelib.api.gametest.Step;
import dev.galacticraft.machinelib.api.gametest.annotation.Magic;
import dev.galacticraft.machinelib.api.gametest.annotation.Structure;
import dev.galacticraft.machinelib.api.gametest.annotation.TestInfo;
import dev.galacticraft.machinelib.api.gametest.annotation.timing.Oneshot;
import dev.galacticraft.machinelib.api.gametest.annotation.timing.Timed;
import dev.galacticraft.machinelib.api.gametest.util.GameTestStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestAssertPosException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import org.jetbrains.annotations.Contract;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GameTestUtils {
    public static <T> T invokeUnorderedArguments(Object base, Method method, Object... args) {
        method.setAccessible(true);

        try {
            int len = method.getParameters().length;
            if (len <= args.length) {
                Class<?>[] types = method.getParameterTypes();
                List<Object> params = Lists.newArrayList(args);
                args = new Object[len];
                for (int i = 0; i < args.length; i++) {
                    for (int j = 0; j < params.size(); j++) {
                        Object x = params.get(j);
                        if (types[i].isAssignableFrom(x.getClass())) {
                            args[i] = x;
                            params.remove(j);
                            break;
                        }
                    }
                }
            }

            return (T) method.invoke(base, args);
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

            if (t.getCause() instanceof RuntimeException rt) {
                throw rt;
            } else {
                throw new RuntimeException(t);
            }
        } catch (ClassCastException ex) {
            throw new RuntimeException("Failed to cast test method return value!", ex);
        }
    }

    public static <T> T tryInvokeUnorderedArguments(Object base, Method method, Object... args) {
        method.setAccessible(true);

        try {
            int len = method.getParameters().length;
            if (len <= args.length) {
                Class<?>[] types = method.getParameterTypes();
                List<Object> params = Lists.newArrayList(args);
                args = new Object[len];
                for (int i = 0; i < args.length; i++) {
                    for (int j = 0; j < params.size(); j++) {
                        Object x = params.get(j);
                        if (types[i].isAssignableFrom(x.getClass())) {
                            args[i] = x;
                            params.remove(j);
                            break;
                        }
                    }
                }
            }

            return (T) method.invoke(base, args);
        } catch (InvocationTargetException t) {
            if (t.getCause() instanceof RuntimeException rt) {
                throw rt;
            } else {
                throw new RuntimeException(t);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static String testStructure(Object obj, Method method, String defaultStructure) {
        Structure structure = method.getAnnotation(Structure.class);
        if (structure != null) return structure.value();
        return testStructure(obj != null ? obj.getClass() : method.getDeclaringClass(), defaultStructure);
    }

    private static String testStructure(Class<?> clazz, String defaultStructure) {
        Structure structure = clazz.getAnnotation(Structure.class);
        if (structure != null) return structure.value();

        if (clazz.getEnclosingClass() != null) {
            return testStructure(clazz.getEnclosingClass(), defaultStructure);
        }
        return defaultStructure;
    }

    public static String testBatch(Object obj, Method method) {
        TestInfo info = method.getAnnotation(TestInfo.class);
        if (info != null && !info.batch().isBlank()) return info.batch();
        return testBatch(obj != null ? obj.getClass() : method.getDeclaringClass());
    }

    private static String testBatch(Class<?> clazz) {
        TestInfo[] infos = clazz.getAnnotationsByType(TestInfo.class);
        for (TestInfo info : infos) {
            if (!info.batch().isBlank()) return info.batch();
        }

        if (clazz.getEnclosingClass() != null) {
            String batch = testBatch(clazz.getEnclosingClass());
            if (!batch.isBlank()) return batch;
        }
        return "defaultBatch";
    }

    public static void testGroup(StringBuilder builder, Method method) {
        testGroup(builder, method.getClass());

        TestInfo annotation = method.getAnnotation(TestInfo.class);
        if (annotation != null) {
            if (!annotation.group().isBlank()) {
                builder.append(annotation.group()).append('.');
            }
        }
    }

    public static void testGroup(StringBuilder builder, Class<?> clazz) {
        if (clazz.getEnclosingClass() != null) {
            testGroup(builder, clazz.getEnclosingClass());
        } else {
            TestInfo info = clazz.getPackage().getAnnotation(TestInfo.class);
            if (info != null && !info.group().isBlank()) {
                builder.append(info.group());
            }
        }

        TestInfo[] infos = clazz.getAnnotationsByType(TestInfo.class);
        for (TestInfo info : infos) {
            if (!info.group().isBlank()) builder.append(info.group()).append('.');
        }
    }

    public static String testName(Method method) {
        TestInfo info = method.getAnnotation(TestInfo.class);
        if (info != null && !info.name().isBlank()) return info.name();
        return method.getName();
    }

    public static String generateTestName(Object obj, Method method) {
        StringBuilder builder = new StringBuilder();
        String batch = testBatch(obj, method);
        if (!batch.equals("defaultBatch")) {
            builder.append(batch).append('/');
        }
        testGroup(builder, method);
        builder.append(testName(method));
        return builder.toString();
    }

    public static String generateTestName(Class<?> source, String name) {
        StringBuilder builder = new StringBuilder();
        String batch = testBatch(source);
        if (!batch.equals("defaultBatch")) {
            builder.append(batch).append('/');
        }
        testGroup(builder, source);
        builder.append(name);
        return builder.toString();
    }

    public static TestFunction createAdditionalTest(Class<?> source, String name, String defaultStructure, int maxTicks, long setupTicks, Consumer<GameTestHelper> test) {
        return new TestFunction(
                testBatch(source),
                generateTestName(source, name),
                testStructure(source, defaultStructure),
                maxTicks,
                setupTicks,
                true,
                test
        );
    }

    public static TestFunction createTest(Object obj, Method method, int maxTicks, long setupTicks, Consumer<GameTestHelper> test) {
        return createTest(obj, method, GameTestStructures.EMPTY_1x1, maxTicks, setupTicks, test);
    }

    public static TestFunction createMatrixTest(Object obj, Method method, String suffix, int maxTicks, long setupTicks, Consumer<GameTestHelper> test) {
        return createMatrixTest(obj, method, suffix, GameTestStructures.EMPTY_1x1, maxTicks, setupTicks, test);
    }

    public static TestFunction createTest(Object obj, Method method, String defaultStructure, int maxTicks, long setupTicks, Consumer<GameTestHelper> test) {
        return new TestFunction(
                testBatch(obj, method),
                generateTestName(obj, method),
                testStructure(obj, method, defaultStructure),
                maxTicks,
                setupTicks,
                true,
                test
        );
    }

    public static TestFunction createMatrixTest(Object obj, Method method, String suffix, String defaultStructure, int maxTicks, long setupTicks, Consumer<GameTestHelper> test) {
        return new TestFunction(
                testBatch(obj, method),
                generateTestName(obj, method) + '.' + suffix,
                testStructure(obj, method, defaultStructure),
                maxTicks,
                setupTicks,
                true,
                test
        );
    }

    public static Consumer<GameTestHelper> invokeWithTiming(Method method, Object instance, List<Method> before, List<Method> after, int variant) {
        return helper -> {
            List<Object> args = new ArrayList<>();
            args.add(helper);

            for (Annotation annotation : method.getAnnotations()) {
                TestModifiers.invoke(annotation, helper, method.getDeclaringClass(), instance, args, variant);
            }

            for (Method pre : before) {
                GameTestUtils.tryInvokeUnorderedArguments(null, pre, helper, helper.getLevel(), helper.getLevel().getServer());
            }

            if (method.isAnnotationPresent(Timed.class)) {
                Timed annotation = method.getAnnotation(Timed.class);
                assert annotation != null;
                Step r = GameTestUtils.invokeUnorderedArguments(instance, method, args.toArray());
                assert r != null;
                invokeNextStep(helper, annotation.delay(), 0, r, after);
            } else if (method.isAnnotationPresent(Oneshot.class)) {
                Oneshot annotation = method.getAnnotation(Oneshot.class);
                assert annotation != null;
                Runnable r = GameTestUtils.invokeUnorderedArguments(instance, method, args.toArray());
                if (r == null) {
                    helper.succeed();

                    for (Method post : after) {
                        GameTestUtils.tryInvokeUnorderedArguments(null, post, helper, helper.getLevel(), helper.getLevel().getServer());
                    }
                } else {
                    helper.runAfterDelay(annotation.time(), () -> {
                        r.run();
                        helper.succeed();

                        for (Method post : after) {
                            GameTestUtils.tryInvokeUnorderedArguments(null, post, helper, helper.getLevel(), helper.getLevel().getServer());
                        }
                    });
                }
            }
        };
    }

    public static <T> T getMagic(Class<?> clazz, Object instance, Magic magic) {
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Magic annotation = field.getAnnotation(Magic.class);
            if (annotation != null && annotation.value().equals(magic.value())) {
                try {
                    return (T) field.get(instance);
                } catch (Exception ignore) {}
            }
        }
        Class<?> sc = clazz.getSuperclass();
        while (sc != Object.class) {
            T magic1 = getMagic(sc, instance, magic);
            if (magic1 != null) {
                return magic1;
            }
            sc = sc.getSuperclass();
        }

        if (clazz.getEnclosingClass() != null) {
            try {
                Constructor<?> declaredConstructor = clazz.getEnclosingClass().getDeclaredConstructor();
                declaredConstructor.setAccessible(true);
                return getMagic(clazz.getEnclosingClass(), declaredConstructor.newInstance(), magic);
            } catch (Exception ex) {
                if (instance != null) {
                    try {
                        Constructor<?> declaredConstructor = clazz.getEnclosingClass().getDeclaredConstructor(instance.getClass());
                        declaredConstructor.setAccessible(true);
                        return getMagic(clazz.getEnclosingClass(), declaredConstructor.newInstance(instance), magic);
                    } catch (Exception ignore) {}
                }
                return getMagic(clazz.getEnclosingClass(), null, magic);
            }
        }
        return null;
    }

    private static void invokeNextStep(GameTestHelper helper, int[] delay, int i, Step current, List<Method> after) {
        helper.runAfterDelay(delay[i], () -> {
            Step next = current.next();
            if (i + 1 == delay.length) {
                if (next != null) throw new AssertionError("Extra step?");
                helper.succeed();

                for (Method post : after) {
                    GameTestUtils.tryInvokeUnorderedArguments(null, post, helper, helper.getLevel(), helper.getLevel().getServer());
                }
            } else {
                if (next == null) throw new NullPointerException("Missing step?");
                invokeNextStep(helper, delay, i + 1, next, after);
            }
        });
    }

    @Contract("_, _, _ -> fail")
    public static void wrapThrowable(GameTestHelper helper, BlockPos pos, Throwable t) {
        if (t instanceof GameTestAssertException ex) throw ex;

        GameTestAssertPosException ex = new GameTestAssertPosException(t.getMessage(), helper.absolutePos(pos), pos, helper.getTick());
        ex.setStackTrace(t.getStackTrace());
        throw ex;
    }
}
