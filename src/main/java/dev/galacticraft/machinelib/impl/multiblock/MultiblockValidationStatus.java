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

package dev.galacticraft.machinelib.impl.multiblock;

/**
 * Describes the result of validating an already formed multiblock.
 *
 * <p>This is intentionally not a boolean because MachineLib must distinguish a
 * genuinely broken structure from a structure that cannot currently be inspected
 * because one or more of its chunks are unloaded.</p>
 */
public enum MultiblockValidationStatus {

    /**
     * The multiblock is fully loaded and every part still matches its predicate.
     */
    VALID,

    /**
     * The multiblock is fully loaded, but at least one part no longer matches.
     */
    INVALID,

    /**
     * At least one part position is in an unloaded chunk.
     *
     * <p>This should not delete persistent saved data.</p>
     */
    UNLOADED

}