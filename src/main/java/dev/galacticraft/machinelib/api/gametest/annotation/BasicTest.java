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

import dev.galacticraft.machinelib.api.gametest.SimpleGameTest;
import net.minecraft.gametest.framework.GameTest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Creates simple one-step game tests when used in conjunction with {@link SimpleGameTest} (and descendants).
 *
 * <p>{@code @BasicTest} annotated methods must not be {@code private} or {@code static}.
 *
 * <p>{@code @BasicTest} annotated methods can either take no arguments or a single {@link net.minecraft.gametest.framework.GameTestHelper} argument.
 *
 * <p>{@code @BasicTest} annotated methods can return void or a {@link Runnable}.
 *
 * <p>If a {@link Runnable} is returned, it will be run after the setup time has passed.
 * And the test will succeed if the runnable executes without failure and work time is 1.
 * Otherwise, if setup time is 1, the test will succeed immediately (assuming no failures raised).
 *
 * @see GameTest
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BasicTest {
    /**
     * {@return the batch name of the test}
     *
     * @see GameTest#batch()
     */
    String batch() default "defaultBatch";

    /**
     * {@return the group name of the test}
     * Used for naming purposes only.
     */
    String group() default "";

    /**
     * {@return the structure to use for the test}
     * Defaults to an empty (all air) 3x3 structure.
     *
     * @see GameTest#template()
     */
    String structure() default SimpleGameTest.STRUCTURE_3x3;

    /**
     * {@return the time to wait before executing the work runnable, or before succeeding if no work runnable is provided}
     * If the value is greater than one and no runnable is provided, the test must be succeeded manually.
     */
    int setupTime() default 1;

    /**
     * {@return the time to wait before succeeding after the work runnable}
     * If the value is greater than one, the test must be succeeded manually.
     */
    int workTime() default 1;
}
