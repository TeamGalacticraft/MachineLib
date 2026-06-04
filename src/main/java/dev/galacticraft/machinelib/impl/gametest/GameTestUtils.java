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

package dev.galacticraft.machinelib.impl.gametest;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestAssertPosException;
import net.minecraft.gametest.framework.GameTestHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class GameTestUtils {
    private static final char SEPARATOR = '.';

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

    public static String generateTestName(@Nullable String batch, @Nullable String group, @NotNull String name) {
        if (batch == null || batch.isBlank() || batch.equals("defaultBatch")) {
            if (group == null || group.isBlank()) return name;
            return group + SEPARATOR + name;
        }
        if (group == null || group.isBlank()) return batch + SEPARATOR + name;
        return batch + SEPARATOR + group + SEPARATOR + name;
    }

    @Contract("_, _, _, _ -> fail")
    public static void unwrapAssertions(GameTestHelper helper, BlockPos pos, AssertionError e, boolean capture) throws AssertionError {
        if (capture) {
            GameTestAssertPosException ex = new GameTestAssertPosException(e.getMessage(), helper.absolutePos(pos), pos, helper.getTick());
            ex.setStackTrace(e.getStackTrace());
            throw ex;
        } else {
            throw e;
        }
    }
}
