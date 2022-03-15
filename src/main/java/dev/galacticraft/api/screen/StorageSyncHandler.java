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

package dev.galacticraft.api.screen;

import dev.galacticraft.api.gas.Gas;
import dev.galacticraft.api.gas.GasVariant;
import dev.galacticraft.api.machine.storage.slot.SlotType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.function.LongSupplier;
import java.util.function.Supplier;

public interface StorageSyncHandler {
    void addCapacitor(int x, int y, int width, int height, @NotNull LongSupplier amount);

    void addSlot(@NotNull SlotType<Item, ItemVariant> type, int x, int y, int width, int height, @NotNull Supplier<@NotNull FluidVariant> fluid, @NotNull LongSupplier amount);

    void addFluidTank(@NotNull SlotType<Fluid, FluidVariant> type, int x, int y, int width, int height, @NotNull Supplier<@NotNull FluidVariant> fluid, @NotNull LongSupplier amount);

    void addGasTank(@NotNull SlotType<Gas, GasVariant> type, int x, int y, int width, int height, @NotNull Supplier<@NotNull GasVariant> fluid, @NotNull LongSupplier amount);
}
