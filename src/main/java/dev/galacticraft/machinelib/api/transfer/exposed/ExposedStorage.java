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

package dev.galacticraft.machinelib.api.transfer.exposed;

import dev.galacticraft.machinelib.api.fluid.FluidStack;
import dev.galacticraft.machinelib.api.storage.ResourceStorage;
import dev.galacticraft.machinelib.api.storage.io.ResourceFlow;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.impl.transfer.exposed.ExposedStorageImpl;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ExposedStorage<Resource, Variant extends TransferVariant<Resource>> extends Storage<Variant> {
    @Contract("_, _ -> new")
    static @NotNull ExposedStorage<Item, ItemVariant> createItem(ResourceStorage<Item, ItemStack, ? extends ResourceSlot<Item, ItemStack>, ? extends SlotGroup<Item, ItemStack, ? extends ResourceSlot<Item, ItemStack>>> storage, ResourceFlow flow) {
        ResourceSlot<Item, ItemStack>[] rawSlots = storage.getSlots();
        ExposedSlot<Item, ItemVariant>[] slots = new ExposedSlot[rawSlots.length];
        for (int i = 0; i < rawSlots.length; i++) {
            slots[i] = ExposedSlot.createItem(rawSlots[i], flow);
        }
        return new ExposedStorageImpl<>(storage, slots);
    }

    @Contract("_, _ -> new")
    static @NotNull ExposedStorage<Fluid, FluidVariant> createFluid(ResourceStorage<Fluid, FluidStack, ? extends ResourceSlot<Fluid, FluidStack>, ? extends SlotGroup<Fluid, FluidStack, ? extends ResourceSlot<Fluid, FluidStack>>> storage, ResourceFlow flow) {
        ResourceSlot<Fluid, FluidStack>[] rawSlots = storage.getSlots();
        ExposedSlot<Fluid, FluidVariant>[] slots = new ExposedSlot[rawSlots.length];
        for (int i = 0; i < rawSlots.length; i++) {
            slots[i] = ExposedSlot.createFluid(rawSlots[i], flow);
        }
        return new ExposedStorageImpl<>(storage, slots);
    }

    static @Nullable ExposedStorage<Item, ItemVariant> createFullItem(ResourceStorage<Item, ItemStack, ? extends ResourceSlot<Item, ItemStack>, ? extends SlotGroup<Item, ItemStack, ? extends ResourceSlot<Item, ItemStack>>> storage) {
        if (storage.slots() == 0) return null;
        ResourceSlot<Item, ItemStack>[] rawSlots = storage.getSlots();
        ExposedSlot<Item, ItemVariant>[] slots = new ExposedSlot[rawSlots.length];
        for (int i = 0; i < rawSlots.length; i++) {
            slots[i] = ExposedSlot.createFullItem(rawSlots[i]);
        }
        return new ExposedStorageImpl<>(storage, slots);
    }

    static @Nullable ExposedStorage<Fluid, FluidVariant> createFullFluid(ResourceStorage<Fluid, FluidStack, ? extends ResourceSlot<Fluid, FluidStack>, ? extends SlotGroup<Fluid, FluidStack, ? extends ResourceSlot<Fluid, FluidStack>>> storage) {
        if (storage.slots() == 0) return null;
        ResourceSlot<Fluid, FluidStack>[] rawSlots = storage.getSlots();
        ExposedSlot<Fluid, FluidVariant>[] slots = new ExposedSlot[rawSlots.length];
        for (int i = 0; i < rawSlots.length; i++) {
            slots[i] = ExposedSlot.createFullFluid(rawSlots[i]);
        }
        return new ExposedStorageImpl<>(storage, slots);
    }

    @Override
    long getVersion();
}
