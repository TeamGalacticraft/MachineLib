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

package dev.galacticraft.impl.machine.storage;

import com.google.common.collect.Iterators;
import dev.galacticraft.api.machine.storage.FluidStorage;
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.machine.storage.io.SlotType;
import dev.galacticraft.impl.fluid.FluidStack;
import dev.galacticraft.impl.machine.ModCount;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public class FluidStorageImpl implements FluidStorage {
    private final int size;
    private final FluidSlot[] inventory;
    private final SlotType<Fluid, FluidVariant>[] types;
    private final boolean[] extraction;
    private final boolean[] insertion;

    private final ModCount modCount = new ModCount();

    public FluidStorageImpl(int size, SlotType<Fluid, FluidVariant>[] types, long[] counts) {
        this.size = size;
        this.inventory = new FluidSlot[this.size];
        this.extraction = new boolean[this.size];
        this.insertion = new boolean[this.size];

        for (int i = 0; i < this.inventory.length; i++) {
            this.inventory[i] = new FluidSlot(counts[i]);
            if (types[i].getFlow() == ResourceFlow.INPUT) {
                this.insertion[i] = true;
                this.extraction[i] = false;
            } else if (types[i].getFlow() == ResourceFlow.OUTPUT) {
                this.insertion[i] = false;
                this.extraction[i] = true;
            } else if (types[i].getFlow() == ResourceFlow.BOTH) {
                this.insertion[i] = true;
                this.extraction[i] = true;
            }
        }
        this.types = types;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int getModCount() {
        return this.modCount.getModCount();
    }

    @Override
    public boolean isEmpty() {
        for (FluidSlot fluidSlot : this.inventory) {
            if (!fluidSlot.isResourceBlank()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull FluidStack getStack(int slot) {
        return this.inventory[slot].copyStack();
    }

    @Override
    public boolean canExtract(int slot) {
        return this.extraction[slot];
    }

    @Override
    public boolean canInsert(int slot) {
        return this.insertion[slot];
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, @NotNull TransactionContext context) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        long inserted = 0;
        for (int i = 0; i < this.size(); i++) {
            inserted += this.insert(i, resource, maxAmount - inserted, context);
            if (inserted == maxAmount) {
                break;
            }
        }

        return inserted;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, @NotNull TransactionContext context) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        long extracted = 0;
        for (int i = 0; i < this.size(); i++) {
            extracted += this.extract(i, resource, maxAmount - extracted, context);
            if (extracted == maxAmount) {
                break;
            }
        }

        return extracted;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator(@NotNull TransactionContext context) {
        return new CombinedIterator(context);
    }

    @Override
    public @NotNull FluidStack extract(int slot, long amount, @Nullable TransactionContext context) {
        StoragePreconditions.notNegative(amount);

        return this.extractVariant(this.inventory[slot], amount, context);
    }

    @Override
    public @NotNull FluidStack extract(int slot, @NotNull Tag<Fluid> tag, long amount, @Nullable TransactionContext context) {
        StoragePreconditions.notNegative(amount);

        FluidSlot invSlot = this.inventory[slot];
        if (tag.values().contains(invSlot.variant.getFluid())) {
            return this.extractVariant(invSlot, amount, context);
        } else {
            return FluidStack.EMPTY;
        }
    }

    @Override
    public @NotNull FluidStack extract(int slot, @NotNull Fluid fluid, long amount, @Nullable TransactionContext context) {
        StoragePreconditions.notNegative(amount);

        FluidSlot invSlot = this.inventory[slot];
        if (invSlot.variant.getFluid() == fluid) {
            return this.extractVariant(invSlot, amount, context);
        } else {
            return FluidStack.EMPTY;
        }
    }

    @NotNull
    private FluidStack extractVariant(@NotNull FluidSlot invSlot, long amount, @Nullable TransactionContext context) {
        long extracted = Math.min(invSlot.amount, amount);
        if (extracted > 0) {
            try (Transaction transaction = Transaction.openNested(context)) {
                invSlot.updateSnapshots(transaction);
                FluidStack stack = invSlot.copyStack();
                invSlot.amount -= extracted;

                if (invSlot.amount == 0) {
                    invSlot.variant = FluidVariant.blank();
                }
                modCount.increment(transaction);
                transaction.commit();
                return stack;
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack replace(int slot, @NotNull FluidVariant variant, long amount, @Nullable TransactionContext context) {
        try (Transaction transaction = Transaction.openNested(context)) {
            FluidSlot invSlot = this.inventory[slot];
            invSlot.updateSnapshots(transaction);
            FluidStack currentStack = invSlot.copyStack();
            invSlot.variant = variant;
            invSlot.amount = amount;
            modCount.increment(transaction);
            transaction.commit();
            return currentStack;
        }
    }

    @Override
    public long insert(int slot, @NotNull FluidVariant variant, long amount, @Nullable TransactionContext context) {
        if (amount == 0) return 0;
        FluidSlot invSlot = this.inventory[slot];
        if (invSlot.isResourceBlank()) {
            amount = Math.min(amount, invSlot.getCapacity(variant));
            try (Transaction transaction = Transaction.openNested(context)) {
                invSlot.updateSnapshots(transaction);
                invSlot.variant = variant;
                invSlot.amount = amount;
                modCount.increment(transaction);
                transaction.commit();
                return amount;
            }
        } else if (variant.equals(invSlot.getResource())) {
            try (Transaction transaction = Transaction.openNested(context)) {
                long inserted = Math.min(amount, invSlot.getCapacity(variant) - invSlot.amount);
                if (inserted > 0) {
                    invSlot.updateSnapshots(transaction);
                    invSlot.amount += inserted;
                    modCount.increment(transaction);
                    transaction.commit();
                    return inserted;
                }
                return 0;
            }
        } else {
            return 0;
        }
    }

    @Override
    public long extract(int slot, @NotNull FluidVariant variant, long amount, @Nullable TransactionContext context) {
        FluidSlot invSlot = this.inventory[slot];
        if (invSlot.variant.equals(variant)) {
            long extracted = Math.min(invSlot.amount, amount);
            if (extracted > 0) {
                try (Transaction transaction = Transaction.openNested(context)) {
                    invSlot.updateSnapshots(transaction);
                    invSlot.amount -= extracted;

                    if (invSlot.amount == 0) {
                        invSlot.variant = FluidVariant.blank();
                    }
                    modCount.increment(transaction);
                    transaction.commit();
                    return extracted;
                }
            }
        }
        return 0;
    }

    @Override
    public long getMaxCount(int slot) {
        return this.inventory[slot].getCapacity();
    }

    @Override
    public boolean canAccess(@NotNull PlayerEntity player) {
        return true;
    }

    @Override
    public boolean canAccept(int slot, @NotNull FluidVariant variant) {
        return this.types[slot].willAccept(variant);
    }

    @Override
    public long count(@NotNull Fluid fluid) {
        long count = 0;
        for (FluidSlot fluidSlot : this.inventory) {
            FluidStack stack = fluidSlot.copyStack();
            if (stack.getFluid() == fluid) {
                count += stack.getAmount();
            }
        }
        return count;
    }

    @Override
    public boolean containsAny(@NotNull Set<Fluid> fluids) {
        for (FluidSlot fluidSlot : this.inventory) {
            if (fluids.contains(fluidSlot.copyStack().getFluid())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAny(@NotNull Tag<Fluid> fluids) {
        List<Fluid> values = fluids.values();
        for (FluidSlot fluidSlot : this.inventory) {
            if (values.contains(fluidSlot.copyStack().getFluid())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void clear() {
        assert !Transaction.isOpen();
        for (FluidSlot fluidSlot : this.inventory) {
            fluidSlot.variant = FluidVariant.blank();
            fluidSlot.amount = 0;
        }
        modCount.incrementUnsafe();
    }

    @Override
    public SlotType<Fluid, FluidVariant>[] getTypes() {
        return this.types;
    }

    /*
     * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    private class CombinedIterator implements Iterator<StorageView<FluidVariant>>, Transaction.CloseCallback {
        private boolean open = true;
        private final TransactionContext context;
        private final Iterator<FluidSlot> partIterator = Iterators.forArray(FluidStorageImpl.this.inventory);
        // Always holds the next StorageView<T>, except during next() while the iterator is being advanced.
        private Iterator<StorageView<FluidVariant>> currentPartIterator = null;

        private CombinedIterator(TransactionContext context) {
            this.context = context;
            advanceCurrentPartIterator();
            context.addCloseCallback(this);
        }

        @Override
        public boolean hasNext() {
            return open && currentPartIterator != null && currentPartIterator.hasNext();
        }

        @Override
        public StorageView<FluidVariant> next() {
            if (!open) {
                throw new NoSuchElementException("The transaction for this iterator was closed.");
            }

            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            StorageView<FluidVariant> returned = currentPartIterator.next();

            // Advance the current part iterator
            if (!currentPartIterator.hasNext()) {
                advanceCurrentPartIterator();
            }

            return returned;
        }

        private void advanceCurrentPartIterator() {
            while (partIterator.hasNext()) {
                this.currentPartIterator = partIterator.next().iterator(context);

                if (this.currentPartIterator.hasNext()) {
                    break;
                }
            }
        }

        @Override
        public void onClose(TransactionContext context, Transaction.Result result) {
            // As soon as the transaction is closed, this iterator is not valid anymore.
            open = false;
        }
    }

    private static class FluidSlot extends SingleVariantStorage<FluidVariant> {
        private final long capacity;

        private FluidSlot(long capacity) {
            this.capacity = capacity;
        }

        @Override
        protected FluidVariant getBlankVariant() {
            return FluidVariant.blank();
        }

        @Override
        protected long getCapacity(@NotNull FluidVariant variant) {
            return this.capacity;
        }

        public FluidStack copyStack() {
            if (this.variant.isBlank() || this.amount == 0) return FluidStack.EMPTY;
            return new FluidStack(this.variant, this.amount);
        }
    }
}
