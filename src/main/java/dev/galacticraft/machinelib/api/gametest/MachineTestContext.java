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

package dev.galacticraft.machinelib.api.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertPosException;
import net.minecraft.gametest.framework.GameTestHelper;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around {@link GameTestHelper} with additional methods for testing machines.
 */
public class MachineTestContext {
    private final BlockPos pos;
    private final GameTestHelper helper;
    private final List<String> logs = new ArrayList<>();

    public MachineTestContext(BlockPos pos, GameTestHelper helper) {
        this.pos = pos;
        this.helper = helper;
    }

    /**
     * {@return the game test helper}
     */
    public GameTestHelper helper() {
        return this.helper;
    }

    public void log(String message) {
        this.logs.add(message);
    }

    public void assertTrue(boolean b, String message) {
        if (!b) {
            throw this.fail(message);
        }
    }

    /**
     * Fails the test with the given message.
     *
     * @param message the message to fail with
     * @param objects the objects to format the message with
     * @return a new {@link GameTestAssertPosException} with the given message
     */
    public GameTestAssertPosException fail(String message, Object... objects) {
        return new GameTestAssertPosException(new MessageFormat(message).format(objects), this.helper.absolutePos(this.pos), this.pos, this.helper.getTick());
    }

    /**
     * Fails the test with the given message.
     *
     * @param message the message to fail with
     * @return a new {@link GameTestAssertPosException} with the given message
     */
    public GameTestAssertPosException fail(String message) {
        return new GameTestAssertPosException(message, this.helper.absolutePos(this.pos), this.pos, this.helper.getTick());
    }
}
