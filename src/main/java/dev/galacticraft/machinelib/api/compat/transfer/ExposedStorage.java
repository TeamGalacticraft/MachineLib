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

import dev.galacticraft.machinelib.api.storage.ResourceStorage;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.impl.compat.transfer.ExposedStorageImpl;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a resource storage exposed to adjacent blocks or items.
 *
 * @param <Resource> the type of resource stored in the storage
 * @param <Variant> the type of variant associated with the resource
 */
public interface ExposedStorage<Resource, Variant extends TransferVariant<Resource>> extends Storage<Variant> {
    /**
     * Creates an exposed item storage.
     *
     * @param storage the backing resource storage
     * @param flow the flow restrictions on the storage
     * @return an exposed item storage
     */
    @Contract("_, _ -> new")
    static @Nullable ExposedStorage<Item, ItemVariant> createItem(ResourceStorage<Item, ? extends ResourceSlot<Item>> storage, ResourceFlow flow) {
        if (storage.size() == 0) return null;

        ResourceSlot<Item>[] rawSlots = storage.getSlots();
        ExposedSlot<Item, ItemVariant>[] slots = new ExposedSlot[rawSlots.length];
        for (int i = 0; i < rawSlots.length; i++) {
            slots[i] = ExposedSlot.createItem(rawSlots[i], flow);
        }
        return new ExposedStorageImpl<>(storage, slots);
    }

    /**
     * Creates an exposed fluid storage.
     *
     * @param storage the backing resource storage
     * @param flow the flow restrictions on the storage
     * @return an exposed fluid storage
     */
    @Contract("_, _ -> new")
    static @Nullable ExposedStorage<Fluid, FluidVariant> createFluid(ResourceStorage<Fluid, ? extends ResourceSlot<Fluid>> storage, ResourceFlow flow) {
        if (storage.size() == 0) return null;

        ResourceSlot<Fluid>[] rawSlots = storage.getSlots();
        ExposedSlot<Fluid, FluidVariant>[] slots = new ExposedSlot[rawSlots.length];
        for (int i = 0; i < rawSlots.length; i++) {
            slots[i] = ExposedSlot.createFluid(rawSlots[i], flow);
        }
        return new ExposedStorageImpl<>(storage, slots);
    }


    @Override
    long getVersion();
}
