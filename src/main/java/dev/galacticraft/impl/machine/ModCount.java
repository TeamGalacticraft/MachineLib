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

package dev.galacticraft.impl.machine;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import org.jetbrains.annotations.NotNull;

public class ModCount extends SnapshotParticipant<Integer> {
    private int count;

    /**
     * Warning: Modification count can go down if the transaction fails.
     * Do not utilize the resulting mod count in a wider scope than the transaction, unless you are certain that the transaction (and all parents) succeeded
     * @param transaction the transaction context
     * @return the modification count.
     */
    public int increment(@NotNull TransactionContext transaction) {
        updateSnapshots(transaction);
        return ++this.count;
    }

    /**
     * Warning: Do not call during a transaction.
     * @return the modification count of this storage.
     */
    public int getModCount() {
        if (Transaction.isOpen()) {
            throw new IllegalStateException("getModCount() may not be called during a transaction.");
        }
        return this.count;
    }

    /**
     * Warning: modification count CAN GO DOWN if a transaction is cancelled.
     * Do not trust that an equal mod count is representative of the inventory's state if you are unsure about the transaction status of the current thread.
     * @return the modification count of this storage.
     */
    public int getModCountUnsafe() {
        return this.count;
    }

    @Override
    protected Integer createSnapshot() {
        return this.count;
    }

    @Override
    protected void readSnapshot(Integer snapshot) {
        this.count = snapshot;
    }

    /**
     * Increments the counter without creating a snapshot.
     */
    public int incrementUnsafe() {
        assert !Transaction.isOpen();
        return ++this.count;
    }
}
