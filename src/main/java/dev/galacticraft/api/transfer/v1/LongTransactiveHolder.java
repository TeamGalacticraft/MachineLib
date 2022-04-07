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

import dev.galacticraft.impl.transfer.v1.LongTransactiveHolderImpl;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface LongTransactiveHolder extends TransactiveHolder<Long> {
    @Contract("_ -> new")
    static @NotNull LongTransactiveHolder create(long value) {
        return new LongTransactiveHolderImpl(value);
    }
    /**
     * @deprecated Use {@link #getLong()} instead.
     */
    @Override
    @Deprecated
    default @NotNull Long get() {
        return this.getLong();
    }

    /**
     * @deprecated Use {@link #setLong(long, TransactionContext)} instead.
     */
    @Override
    @Deprecated
    default void set(@NotNull Long value, @NotNull TransactionContext context) {
        this.setLong(value, context);
    }

    @Override
    default void setUnsafe(Long value) {
        this.setLongUnsafe(value);
    }

    long getLong();

    long setLong(long value, @NotNull TransactionContext context);

    long setLongUnsafe(long value);

    long increment(@NotNull TransactionContext context);

    long decrement(@NotNull TransactionContext context);

    long increment(long amount, @NotNull TransactionContext context);

    long decrement(long amount, @NotNull TransactionContext context);
}
