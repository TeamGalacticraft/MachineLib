/*
 * Copyright (c) 2019-2022 Team Galacticraft
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

package dev.galacticraft.api.transfer.v1;

import dev.galacticraft.impl.transfer.v1.IntTransactiveHolderImpl;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface IntTransactiveHolder extends TransactiveHolder<Integer> {
    @Contract("_ -> new")
    static @NotNull IntTransactiveHolder create(int value) {
        return new IntTransactiveHolderImpl(value);
    }
    /**
     * @deprecated Use {@link #getInt()} instead.
     */
    @Override
    @Deprecated
    default @NotNull Integer get() {
        return this.getInt();
    }

    /**
     * @deprecated Use {@link #setInt(int, TransactionContext)} instead.
     */
    @Override
    @Deprecated
    default void set(@NotNull Integer value, @NotNull TransactionContext context) {
        this.setInt(value, context);
    }

    @Override
    default void setUnsafe(Integer value) {
        this.setIntUnsafe(value);
    }

    int getInt();

    int setInt(int value, @NotNull TransactionContext context);

    int setIntUnsafe(int value);

    int increment(@NotNull TransactionContext context);

    int decrement(@NotNull TransactionContext context);

    int increment(int amount, @NotNull TransactionContext context);

    int decrement(int amount, @NotNull TransactionContext context);
}
