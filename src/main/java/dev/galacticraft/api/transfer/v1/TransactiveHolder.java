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

import dev.galacticraft.impl.transfer.v1.ImmutableTransactiveHolder;
import dev.galacticraft.impl.transfer.v1.ListeningImmutableTransactiveHolder;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface TransactiveHolder<T> {
    @Contract("_ -> new")
    static <T> @NotNull TransactiveHolder<T> immutableHolder(T value) {
        return new ImmutableTransactiveHolder<>(value);
    }

    @Contract("_ -> new")
    static <T> @NotNull TransactiveHolder<T> immutableHolderListener(T value, CommitListener<T> listener) {
        return new ListeningImmutableTransactiveHolder<>(value, listener);
    }

    T get();

    void set(T value, @NotNull TransactionContext context);

    void setUnsafe(T value);

    interface CommitListener<T> {
        void onCommit(T value);
    }
}
