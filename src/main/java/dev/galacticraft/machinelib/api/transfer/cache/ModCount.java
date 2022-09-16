/*
 * Copyright (c) 2021-2022 Team Galacticraft
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

package dev.galacticraft.machinelib.api.transfer.cache;

import dev.galacticraft.machinelib.impl.transfer.cache.ParentedModCount;
import dev.galacticraft.machinelib.impl.transfer.cache.RootModCount;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Helpful class to keep track of the number of times something has been modified, transactively.
 */
public interface ModCount {
    /**
     * Constructs a new mod count.
     * @return a new mod count.
     */
    @Contract(value = " -> new", pure = true)
    static @NotNull ModCount root() {
        return new RootModCount();
    }

    /**
     * Constructs a new mod count that modifies its parent when modified.
     * @param parent the parent mod count.
     * @return a new mod count
     */
    @Contract("_ -> new")
    static @NotNull ModCount parented(@NotNull ModCount parent) {
        return new ParentedModCount(parent);
    }

    /**
     * Warning: Do not call during a transaction.
     * @return the modification count of this storage.
     */
    long getModCount();

    /**
     * Warning: Modification count can go down if the transaction fails.
     * Do not utilize the resulting mod count in a wider scope than the transaction, unless you are certain that the transaction (and all parents) succeeded
     *
     * @param context the transaction context
     */
    void increment(@NotNull TransactionContext context);

    /**
     * Warning: modification count CAN GO DOWN if a transaction is cancelled.
     * Do not trust that an equal mod count is representative of the inventory's state if you are unsure about the transaction status of the current thread.
     * @return the modification count of this storage.
     */
    long getModCountUnsafe();

    /**
     * Increments the counter without creating a snapshot.
     */
    void incrementUnsafe();
}
