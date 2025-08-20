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

package dev.galacticraft.machinelib.api.gametest.context;

import dev.galacticraft.machinelib.api.machine.MachineStatus;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.GameTestAssertPosException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.slf4j.helpers.MessageFormatter;

import java.util.Arrays;
import java.util.Objects;

public class AssertionContext {
    public final GameTestHelper helper;
    protected BlockPos pos;

    public AssertionContext(GameTestHelper helper, BlockPos pos) {
        this.helper = helper;
        this.pos = pos;
    }

    // T / F - don't append message as it should be implicit what the expected vs actual was.

    public void assertTrue(boolean b, String message) {
        if (!b) throw this.except(message);
    }

    public void assertFalse(boolean b, String message) {
        if (b) throw this.except(message);
    }

    public void assertTrue(boolean b, String message, Object... args) {
        if (!b) throw this.except(MessageFormatter.basicArrayFormat(message, args));
    }

    public void assertFalse(boolean b, String message, Object... args) {
        if (b) throw this.except(MessageFormatter.basicArrayFormat(message, args));
    }

    // equals

    public void assertEquals(byte expected, byte actual, String message) {
        if (expected != actual) throw this.except(message, expected, actual);
    }

    public void assertEquals(short expected, short actual, String message) {
        if (expected != actual) throw this.except(message, expected, actual);
    }

    public void assertEquals(int expected, int actual, String message) {
        if (expected != actual) throw this.except(message, expected, actual);
    }

    public void assertEquals(long expected, long actual, String message) {
        if (expected != actual) throw this.except(message, expected, actual);
    }

    public void assertEquals(float expected, float actual, String message) {
        if (!floatsEqual(expected, actual)) throw this.except(message, expected, actual);
    }

    public void assertEquals(double expected, double actual, String message) {
        if (!doublesEqual(expected, actual)) throw this.except(message, expected, actual);
    }

    public void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) throw this.except(message, expected, actual);
    }

    // for custom messages
    public void _assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) throw this.except(message);
    }

    // for custom messages
    public void _assertEquals(long expected, long actual, String message) {
        if (expected != actual) throw this.except(message);
    }

    // for custom messages
    public void _assertEquals(int expected, int actual, String message) {
        if (expected != actual) throw this.except(message);
    }

    // for custom messages
    public void _assertEquals(float expected, float actual, String message) {
        if (!floatsEqual(expected, actual)) throw this.except(message);
    }

    // for custom messages
    public void _assertEquals(double expected, double actual, String message) {
        if (!doublesEqual(expected, actual)) throw this.except(message);
    }

    // for custom messages
    public <T> void _assertEquals(T expected, T actual, Object2ObjectFunction<T, String> message) {
        if (!Objects.equals(expected, actual)) throw this.except(message.get(actual), expected, actual);
    }

    // for custom messages
    public void _assertEquals(long expected, long actual, Long2ObjectFunction<String> message) {
        if (!Objects.equals(expected, actual)) throw this.except(message.get(actual), expected, actual);
    }

    public void assertEquals(Object[] expected, Object[] actual, String message) {
        if (!Arrays.equals(expected, actual)) throw this.except(message, expected, actual);
    }

    // not equals

    public void assertNotEquals(byte expected, byte actual, String message) {
        if (expected == actual) throw this.except(message, expected, actual);
    }

    public void assertNotEquals(short expected, short actual, String message) {
        if (expected == actual) throw this.except(message, expected, actual);
    }

    public void assertNotEquals(int expected, int actual, String message) {
        if (expected == actual) throw this.except(message, expected, actual);
    }

    public void assertNotEquals(long expected, long actual, String message) {
        if (expected == actual) throw this.except(message, expected, actual);
    }

    public void assertNotEquals(float expected, float actual, String message) {
        if (floatsEqual(expected, actual)) throw this.except(message, expected, actual);
    }

    public void assertNotEquals(double expected, double actual, String message) {
        if (doublesEqual(expected, actual)) throw this.except(message, expected, actual);
    }

    public void assertNotEquals(Object expected, Object actual, String message) {
        if (Objects.equals(expected, actual)) throw this.except(message, expected, actual);
    }

    public void assertNotEquals(Object[] expected, Object[] actual, String message) {
        if (Arrays.equals(expected, actual)) throw this.except(message, expected, actual);
    }

    public void fail(String message) {
        throw this.except(message);
    }

    protected GameTestAssertPosException except(String message) {
        return new GameTestAssertPosException(message, this.helper.absolutePos(pos), this.pos, this.helper.getTick());
    }

    protected GameTestAssertPosException except(String message, Object expected, Object actual) {
        if (message.isEmpty()) {
            return this.except(String.format("Expected: <%s> but was: <%s>", toString(expected), toString(actual)));
        } else {
            return this.except(String.format("%s (expected: <%s> but was: <%s>)", message, toString(expected), toString(actual)));
        }
    }

    protected static String toString(Object o) {
        return switch (o) {
            case null -> "<null>";
            case String s -> s;
            case Vec3i pos -> '[' + pos.toShortString() + ']';
            case Object[] arr -> Arrays.toString(arr);
            case Class<?> c -> c.getName();
            case Item i -> i.builtInRegistryHolder().getRegisteredName();
            case Fluid f -> f.builtInRegistryHolder().getRegisteredName();
            case Block b -> b.builtInRegistryHolder().getRegisteredName();
            case Component c -> c.getString();
            case MachineStatus m -> m.getText().getString().substring(Math.max(0, m.getText().getString().lastIndexOf('.')));
            default -> o.toString();
        };
    }

    protected static String f(String format, Object... objects) {
        for (int i = 0; i < objects.length; i++) {
            objects[i] = toString(objects[i]);
        }
        return MessageFormatter.basicArrayFormat(format, objects);
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    private boolean floatsEqual(float a, float b) {
        return Float.floatToIntBits(a) == Float.floatToIntBits(b);
    }

    private boolean doublesEqual(double a, double b) {
        return Double.doubleToLongBits(a) == Double.doubleToLongBits(b);
    }
}
