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

package dev.galacticraft.machinelib.api.gametest;

import com.google.common.base.CaseFormat;
import dev.galacticraft.machinelib.api.gametest.annotation.UnitTest;
import dev.galacticraft.machinelib.api.gametest.annotation.container.DefaultedMetadata;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.Rotation;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class GameUnitTest<T> implements FabricGameTest {
    private static final Function<String, String> NAME_CONVERSION = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);
    private final @NotNull String id;
    private final @Nullable Supplier<T> supplier;

    protected GameUnitTest(@NotNull String id, @Nullable Supplier<T> supplier) {
        this.id = id;
        this.supplier = supplier;
    }

    @Override
    public void invokeTestMethod(GameTestHelper context, Method method) {
        if (method.getAnnotation(GameTest.class) != null) FabricGameTest.super.invokeTestMethod(context, method);
    }
    
    @MustBeInvokedByOverriders
    public @NotNull List<TestFunction> generateTests() {
        List<TestFunction> tests = new ArrayList<>();
        Class<? extends GameUnitTest<?>> clazz = (Class<? extends GameUnitTest<?>>) this.getClass();
        DefaultedMetadata meta = clazz.getAnnotation(DefaultedMetadata.class);
        String structure = meta != null ? meta.structure() : EMPTY_STRUCTURE;
        for (Method method : clazz.getMethods()) {
            UnitTest unitTest = method.getAnnotation(UnitTest.class);
            if (unitTest != null) {
                String subId = "";
                if (!unitTest.group().isBlank()) {
                    subId = "/" + unitTest.group();
                } else {
                    if (meta != null && !meta.group().isBlank()) {
                        subId = "/" + meta.group();
                    }
                }

                if (method.getParameterTypes().length == 1) {
                    assert this.supplier != null;
                    tests.add(new TestFunction(this.id + subId, this.id + subId + '/' + NAME_CONVERSION.apply(method.getName()), structure, Rotation.NONE, 1, 0, true, 1, 1, helper -> {
                        T t = this.supplier.get();
                        try {
                            method.invoke(this, t);
                            helper.succeed();
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            MachineGameTest.handleException(e);
                        }
                    }));
                } else {
                    tests.add(new TestFunction(this.id + subId, this.id + subId + '/' + NAME_CONVERSION.apply(method.getName()), EMPTY_STRUCTURE, Rotation.NONE, 1, 0, true, 1, 1, helper -> {
                        try {
                            method.invoke(this);
                            helper.succeed();
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            MachineGameTest.handleException(e);
                        }
                    }));
                }
            }
        }

        return tests;
    }
}
