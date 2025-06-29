/*
 * Copyright (c) 2021-2025 Team Galacticraft
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

package dev.galacticraft.machinelib.impl.compat.transfer;

import com.google.common.collect.Iterators;
import dev.galacticraft.machinelib.api.compat.transfer.ExposedSlot;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.component.DataComponentPatch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public abstract class ExposedSlotImpl<Resource, Variant extends TransferVariant<Resource>> implements ExposedSlot<Resource, Variant> {
    private final @NotNull ResourceSlot<Resource> slot;
    private final boolean insertion;
    private final boolean extraction;

    public ExposedSlotImpl(@NotNull ResourceSlot<Resource> slot, @NotNull ResourceFlow flow) {
        this.slot = slot;
        this.insertion = slot.transferMode().externalInsertion() && flow == ResourceFlow.INPUT || flow == ResourceFlow.BOTH;
        this.extraction = slot.transferMode().externalExtraction() && flow == ResourceFlow.OUTPUT || flow == ResourceFlow.BOTH;
    }

    protected abstract @NotNull Variant createVariant(@Nullable Resource resource, @NotNull DataComponentPatch components);

    @Override
    public long insert(Variant variant, long maxAmount, TransactionContext transaction) {
        return this.supportsInsertion() && this.slot.getFilter().test(variant.getObject(), variant.getComponents()) ?
                this.slot.insert(variant.getObject(), variant.getComponents(), maxAmount, transaction)
                : 0;
    }

    @Override
    public long extract(Variant variant, long maxAmount, TransactionContext transaction) {
        if (this.supportsExtraction()) {
            if (this.slot.transferMode() == TransferType.PROCESSING) {
                if (this.slot.getFilter().test(variant.getObject(), variant.getComponents())) {
                    return 0;
                }
            }
            return this.slot.extract(variant.getObject(), variant.getComponents(), maxAmount, transaction);
        }
        return 0;
    }

    @Override
    public boolean supportsInsertion() {
        return this.insertion && this.slot.isValid();
    }

    @Override
    public boolean supportsExtraction() {
        return this.extraction && this.slot.isValid();
    }

    @Override
    public boolean isResourceBlank() {
        return this.slot.isEmpty();
    }

    @Override
    public Variant getResource() {
        return this.createVariant(this.slot.getResource(), this.slot.getComponents());
    }

    @Override
    public @NotNull Iterator<StorageView<Variant>> iterator() {
        return Iterators.singletonIterator(this);
    }

    @Override
    public long getAmount() {
        return this.slot.getAmount();
    }

    @Override
    public long getCapacity() {
        return this.slot.getRealCapacity();
    }

    @Override
    public long getVersion() {
        return this.slot.getModifications();
    }
}
