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

package dev.galacticraft.machinelib.api.gametest.annotation;

import dev.galacticraft.machinelib.api.gametest.MachineTestContext;
import dev.galacticraft.machinelib.api.gametest.SimpleGameTest;
import net.minecraft.gametest.framework.GameTest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>{@code @MachineTest} annotated methods should only be found in {@link dev.galacticraft.machinelib.api.gametest.MachineGameTest}s and descendants.
 *
 * <p>{@code @MachineTest} annotated methods must not be {@code private} or {@code static}.
 *
 * <p>{@code @MachineTest} annotated methods can take
 * a {@link net.minecraft.gametest.framework.GameTestHelper},
 * a {@link MachineTestContext},
 * OR a {@link dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity} (and descendant classes).
 *
 * <p>{@code @MachineTest} annotated methods should return a {@link Runnable} that will be executed after {@code workTime} ticks have passed.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MachineTest {
    /**
     * {@return the batch name of the test}
     *
     * @see GameTest#batch()
     */
    String batch() default "";

    /**
     * {@return the group name of the test}
     * Used for test naming only.
     */
    String group() default "";

    /**
     * {@return the structure file to use for the test}
     *
     * @see GameTest#template()
     */
    String structure() default SimpleGameTest.STRUCTURE_3x3;

    /**
     * {@return the setup time of the test}
     */
    int setupTime() default 1;

    /**
     * {@return the work time of the test}
     */
    int workTime() default 1;

    /**
     * {@return whether to capture {@link AssertionError}s to add gametest data}
     */
    boolean captureAssertions() default true;
}
