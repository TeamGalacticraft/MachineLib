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

package dev.galacticraft.impl.machine;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import org.jetbrains.annotations.NotNull;

public class ParentedModCount extends SnapshotParticipant<Long> implements ModCount {
    private final ModCount parent;
    private long count = 0;

    protected ParentedModCount(ModCount parent) {
        this.parent = parent;
    }

    @Override
    public long getModCount() {
        if (Transaction.isOpen()) {
            throw new IllegalStateException("getModCount() may not be called during a transaction.");
        }
        return this.count;
    }

    @Override
    public void increment(@NotNull TransactionContext context) {
        this.parent.increment(context);
        updateSnapshots(context);
        this.count += 1;
    }

    @Override
    public long getModCountUnsafe() {
        return this.count;
    }

    @Override
    public void incrementUnsafe() {
        assert !Transaction.isOpen();
        this.parent.incrementUnsafe();
        this.count += 1;
    }

    @Override
    protected Long createSnapshot() {
        return this.count;
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        this.count = snapshot;
    }
}
