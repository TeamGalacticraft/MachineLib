/*
 * Copyright (c) 2021-2023 Team Galacticraft
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

package dev.galacticraft.machinelib.api.compat.transfer;

import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.impl.compat.transfer.ExposedFluidSlotImpl;
import dev.galacticraft.machinelib.impl.compat.transfer.ExposedFullFluidSlotImpl;
import dev.galacticraft.machinelib.impl.compat.transfer.ExposedFullItemSlotImpl;
import dev.galacticraft.machinelib.impl.compat.transfer.ExposedItemSlotImpl;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface ExposedSlot<Resource, Variant extends TransferVariant<Resource>> extends ExposedStorage<Resource, Variant>, SingleSlotStorage<Variant> {
    @Contract("_, _ -> new")
    static @NotNull ExposedSlot<Item, ItemVariant> createItem(@NotNull ResourceSlot<Item> slot, @NotNull ResourceFlow flow) {
        return new ExposedItemSlotImpl(slot, flow.canFlowIn(ResourceFlow.INPUT), flow.canFlowIn(ResourceFlow.OUTPUT));
    }

    @Contract("_, _ -> new")
    static @NotNull ExposedSlot<Fluid, FluidVariant> createFluid(@NotNull ResourceSlot<Fluid> slot, @NotNull ResourceFlow flow) {
        return new ExposedFluidSlotImpl(slot, flow.canFlowIn(ResourceFlow.INPUT), flow.canFlowIn(ResourceFlow.OUTPUT));
    }

    @Contract("_ -> new")
    static @NotNull ExposedSlot<Item, ItemVariant> createFullItem(@NotNull ResourceSlot<Item> slot) {
        return new ExposedFullItemSlotImpl(slot);
    }

    @Contract("_ -> new")
    static @NotNull ExposedSlot<Fluid, FluidVariant> createFullFluid(@NotNull ResourceSlot<Fluid> slot) {
        return new ExposedFullFluidSlotImpl(slot);
    }
}
