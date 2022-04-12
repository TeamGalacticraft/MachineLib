/*
 * Copyright (c) 2021-${year} ${company}
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

package dev.galacticraft.impl.machine.storage.slot;

import dev.galacticraft.api.gas.Gas;
import dev.galacticraft.api.gas.GasVariant;
import dev.galacticraft.impl.gas.GasStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class GasSlot extends ResourceSlot<Gas, GasVariant, GasStack> {
    public GasSlot(long capacity) {
        super(capacity);
    }

    @Override
    protected GasVariant getBlankVariant() {
        return GasVariant.blank();
    }

    @Contract(pure = true)
    @Override
    protected @NotNull GasStack getEmptyStack() {
        return GasStack.EMPTY;
    }

    @Contract(pure = true)
    @Override
    protected @NotNull GasStack createStack(@NotNull GasVariant variant, long amount) {
        return variant.toStack(amount);
    }
}
