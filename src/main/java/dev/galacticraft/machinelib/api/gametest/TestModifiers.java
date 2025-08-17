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

import dev.galacticraft.machinelib.api.gametest.annotation.type.Machine;
import dev.galacticraft.machinelib.api.gametest.annotation.type.Matrix;
import dev.galacticraft.machinelib.api.gametest.annotation.type.SingleBlock;
import dev.galacticraft.machinelib.impl.gametest.GameTestUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestModifiers {
    public static final Map<Class<? extends Annotation>, TestModifier<?>> PROVIDERS = new HashMap<>();

    public static <T extends Annotation> void register(Class<T> annotation, TestModifier<T> provider) {
        PROVIDERS.put(annotation, provider);
    }

    public static <T extends Annotation> void invoke(T annotation, GameTestHelper helper, Class<?> clazz, Object inst, List<Object> arguments, int variant) {
        TestModifier<T> provider = (TestModifier<T>) PROVIDERS.get(annotation.annotationType());
        if (provider != null) {
            provider.addArguments(arguments, helper, clazz, inst, variant, annotation);
        }
    }

    static {
        register(Matrix.class, (arguments, helper, clazz, inst, variant, annotation) -> {
            Object magic = GameTestUtils.getMagic(clazz, inst, annotation.value());
            if (magic == null) throw new IllegalArgumentException("Missing magic array");
            if (!(magic instanceof Object[] obj)) throw new IllegalArgumentException("Expected array of values");
            arguments.add(obj[variant]);
        });

        register(Machine.class, (arguments, helper, clazz, inst, variant, annotation) -> {
            Object magic = GameTestUtils.getMagic(clazz, inst, annotation.machine());
            if (magic == null) throw new IllegalArgumentException("Missing magic block");

            BlockPos pos = TestUtils.getCenterFloor(helper);
            if (magic instanceof Block block) {
                helper.setBlock(pos, block);
            } else if (magic instanceof BlockState state) {
                helper.setBlock(pos, state);
            } else {
                throw new IllegalArgumentException("Expected block");
            }
            arguments.add(helper.getBlockEntity(pos));
        });

        register(SingleBlock.class, (arguments, helper, clazz, inst, variant, annotation) -> {
            Object magic = GameTestUtils.getMagic(clazz, inst, annotation.block());
            if (magic == null) throw new IllegalArgumentException("Missing magic block");
            BlockPos pos = TestUtils.getCenterFloor(helper);
            if (magic instanceof Block block) {
                helper.setBlock(pos, block);
            } else if (magic instanceof BlockState state) {
                helper.setBlock(pos, state);
            } else {
                throw new IllegalArgumentException("Expected block");
            }
            arguments.add(helper.getBlockEntity(pos));
        });
    }

    public interface TestModifier<T extends Annotation> {
        void addArguments(List<Object> arguments, GameTestHelper helper, Class<?> clazz, Object inst, int variant, T annotation);
    }
}
