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

package dev.galacticraft.impl.transfer.v1;

import dev.galacticraft.api.transfer.v1.IntTransactiveHolder;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import org.jetbrains.annotations.NotNull;

public class IntTransactiveHolderImpl extends SnapshotParticipant<Integer> implements IntTransactiveHolder {
    private int value;

    public IntTransactiveHolderImpl(int value) {
        this.value = value;
    }

    @Override
    public int getInt() {
        return this.value;
    }

    @Override
    public int setInt(int value, @NotNull TransactionContext context) {
        this.updateSnapshots(context);
        return this.value = value;
    }

    @Override
    public int setIntUnsafe(int value) {
        return this.value = value;
    }

    @Override
    public int increment(@NotNull TransactionContext context) {
        return this.setInt(this.value + 1, context);
    }

    @Override
    public int decrement(@NotNull TransactionContext context) {
        return this.setInt(this.value - 1, context);
    }

    @Override
    public int increment(int amount, @NotNull TransactionContext context) {
        return this.setInt(this.value + amount, context);
    }

    @Override
    public int decrement(int amount, @NotNull TransactionContext context) {
        return this.setInt(this.value - amount, context);
    }

    @Override
    protected @NotNull Integer createSnapshot() {
        return this.value;
    }

    @Override
    protected void readSnapshot(@NotNull Integer snapshot) {
        this.value = snapshot;
    }
}
