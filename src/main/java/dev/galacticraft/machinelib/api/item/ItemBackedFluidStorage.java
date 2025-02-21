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

package dev.galacticraft.machinelib.api.item;

import dev.galacticraft.machinelib.api.component.MachineLibDataComponents;
import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.Optional;

public class ItemBackedFluidStorage extends GenericItemBackedStorage<Fluid, FluidVariant> {
    public ItemBackedFluidStorage(ItemStack stack, ContainerItemContext context, ResourceFilter<Fluid> filter) {
        super(stack, context, filter);
    }

    protected ItemBackedFluidStorage(ItemStack stack, ContainerItemContext context) {
        super(stack, context);
    }

    @Override
    protected long getRawMaxInput() {
        return this.getComponent(MachineLibDataComponents.MAX_INPUT);
    }

    @Override
    protected long getRawMaxOutput() {
        return this.getComponent(MachineLibDataComponents.MAX_OUTPUT);
    }

    @Override
    protected long getRawCapacity() {
        return this.getComponent(MachineLibDataComponents.CAPACITY);
    }

    @Override
    protected long getRawAmount() {
        return this.getComponent(MachineLibDataComponents.AMOUNT);
    }

    @Override
    protected boolean setRawContents(FluidVariant variant, long amount, TransactionContext transaction) {
        ItemStack stack = this.context.getItemVariant().toStack();
        if (amount == 0) {
            assert variant.isBlank();
            stack.remove(MachineLibDataComponents.FLUID);
            stack.remove(MachineLibDataComponents.AMOUNT);
        } else {
            assert !variant.isBlank();
            stack.set(MachineLibDataComponents.FLUID, variant);
            stack.set(MachineLibDataComponents.AMOUNT, amount);
        }

        long itemCount = this.context.getAmount();
        try (Transaction nested = transaction.openNested()) {
            if (this.context.extract(this.context.getItemVariant(), itemCount, nested) == itemCount && this.context.insert(ItemVariant.of(stack), itemCount, nested) == itemCount) {
                nested.commit();
                return true;
            }
        }
        return false;
    }

    @Override
    public FluidVariant getResource() {
        DataComponentPatch components = this.context.getItemVariant().getComponents();
        if (components != null) {
            Optional<? extends FluidVariant> optional = components.get(MachineLibDataComponents.FLUID);
            return optional != null && optional.isPresent() ? optional.get() : FluidVariant.blank();
        }
        return FluidVariant.blank();
    }

    private long getComponent(DataComponentType<Long> type) {
        DataComponentPatch components = this.context.getItemVariant().getComponents();
        if (components != null) {
            Optional<? extends Long> optional = components.get(type);
            return optional != null && optional.isPresent() ? optional.get() : 0L;
        }
        return 0L;
    }
}
