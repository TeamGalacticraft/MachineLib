/*
 * Copyright (c) 2021-2024 Team Galacticraft
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

package dev.galacticraft.machinelib.impl.storage.exposed;

import dev.galacticraft.machinelib.api.compat.transfer.ExposedEnergyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import team.reborn.energy.api.EnergyStorage;

/**
 * An {@link EnergyStorage energy storage} implementation that can restrict input and output.
 *
 * @param parent The parent energy storage.
 * @param maxInsertion The maximum amount of energy that can be inserted in one transaction.
 * @param maxExtraction The maximum amount of energy that can be extracted in one transaction.
 *
 * @see EnergyStorage
 */
public record ExposedEnergyStorageImpl(@NotNull EnergyStorage parent, long maxInsertion,
                                       long maxExtraction) implements ExposedEnergyStorage {
    @Override
    public boolean supportsInsertion() {
        return this.maxExtraction > 0;
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        if (this.maxInsertion > 0) {
            return this.parent.insert(Math.min(this.maxInsertion, maxAmount), transaction);
        }
        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return this.maxExtraction > 0;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        if (this.maxExtraction > 0) {
            return this.parent.extract(Math.min(this.maxExtraction, maxAmount), transaction);
        }
        return 0;
    }

    @Override
    public long getAmount() {
        return this.parent.getAmount();
    }

    @Override
    public long getCapacity() {
        return this.parent.getCapacity();
    }
}
