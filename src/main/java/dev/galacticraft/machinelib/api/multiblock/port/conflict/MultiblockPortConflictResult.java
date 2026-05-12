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

package dev.galacticraft.machinelib.api.multiblock.port.conflict;

import dev.galacticraft.machinelib.client.api.screen.port.PreviewPortOptionState;
import net.minecraft.network.chat.Component;

/**
 * Result returned by a multiblock port conflict rule.
 *
 * @param state visual and logical validation state
 * @param reason optional tooltip reason shown when the option is disabled or conflicting
 */
public record MultiblockPortConflictResult(
        PreviewPortOptionState state,
        Component reason
) {

    /**
     * Creates a valid result.
     *
     * @return valid result
     */
    public static MultiblockPortConflictResult valid() {
        return new MultiblockPortConflictResult(
                PreviewPortOptionState.VALID,
                null
        );
    }

    /**
     * Creates a current result.
     *
     * @return current result
     */
    public static MultiblockPortConflictResult current() {
        return new MultiblockPortConflictResult(
                PreviewPortOptionState.CURRENT,
                null
        );
    }

    /**
     * Creates a disabled result.
     *
     * @param reason disabled reason
     * @return disabled result
     */
    public static MultiblockPortConflictResult disabled(final Component reason) {
        return new MultiblockPortConflictResult(
                PreviewPortOptionState.DISABLED,
                reason
        );
    }

    /**
     * Creates a conflicting result.
     *
     * @param reason conflict reason
     * @return conflict result
     */
    public static MultiblockPortConflictResult conflict(final Component reason) {
        return new MultiblockPortConflictResult(
                PreviewPortOptionState.CONFLICT,
                reason
        );
    }

    /**
     * Checks whether this result blocks applying the candidate port.
     *
     * @return {@code true} if the candidate should not be applied
     */
    public boolean blocksSelection() {
        return this.state == PreviewPortOptionState.DISABLED
                || this.state == PreviewPortOptionState.CONFLICT;
    }
}