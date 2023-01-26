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

package dev.galacticraft.machinelib.api.storage;

import dev.galacticraft.machinelib.impl.Utils;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import team.reborn.energy.api.EnergyStorage;

public final class ResourceFilters {
    private static final ResourceFilter<?> ALWAYS = (resource, tag) -> true;
    private static final ResourceFilter<?> NEVER = (resource, tag)-> false;

    public static final ResourceFilter<Item> CAN_EXTRACT_ENERGY = (item, tag) -> {
        if (item == null) return false;
        EnergyStorage storage = ContainerItemContext.withConstant(ItemVariant.of(item, tag), 1).find(EnergyStorage.ITEM);
        return storage != null && storage.supportsExtraction();
    };
    public static final ResourceFilter<Item> CAN_EXTRACT_ENERGY_STRICT = (item, tag) -> {
        if (item == null) return false;
        EnergyStorage storage = ContainerItemContext.withConstant(ItemVariant.of(item, tag), 1).find(EnergyStorage.ITEM);
        if (storage == null || !storage.supportsExtraction()) return false;
        try (Transaction test = Transaction.openNested(Transaction.getCurrentUnsafe())) { // SAFE: the transaction is immediately cancelled
            if (storage.extract(1, test) == 1) return true;
        }
        return false;
    };
    public static final ResourceFilter<Item> CAN_INSERT_ENERGY = (item, tag) -> {
        if (item == null) return false;
        EnergyStorage storage = ContainerItemContext.withConstant(ItemVariant.of(item, tag), 1).find(EnergyStorage.ITEM);
        return storage != null && storage.supportsInsertion();
    };
    public static final ResourceFilter<Item> CAN_INSERT_ENERGY_STRICT = (item, tag) -> {
        if (item == null) return false;
        EnergyStorage storage = ContainerItemContext.withConstant(ItemVariant.of(item, tag), 1).find(EnergyStorage.ITEM);
        if (storage == null || !storage.supportsInsertion()) return false;
        try (Transaction test = Transaction.openNested(Transaction.getCurrentUnsafe())) { // SAFE: the transaction is immediately cancelled
            if (storage.insert(1, test) == 1) return true;
        }
        return false;
    };

    @Contract(pure = true)
    public static <Resource> @NotNull ResourceFilter<Resource> matchAnyNbt(@NotNull Resource resource) {
        return (r, ignored) -> r == resource;
    }

    @Contract(pure = true)
    public static <Resource> @NotNull ResourceFilter<Resource> match(@NotNull Resource resource, @NotNull CompoundTag tag) {
        return (r, t) -> r == resource && Utils.tagsEqual(t, tag);
    }

    @Contract(pure = true)
    public static <Resource> @NotNull ResourceFilter<Resource> matchNoNbt(@NotNull Resource resource) {
        return (r, tag) -> r == resource && Utils.tagsEqual(tag, null);
    }

    @Contract(pure = true)
    public static <Resource> @NotNull ResourceFilter<Resource> always() {
        return (ResourceFilter<Resource>) ALWAYS;
    }

    public static <Resource> @NotNull ResourceFilter<Resource> never() {
        return (ResourceFilter<Resource>) NEVER;
    }

    private ResourceFilters() {}
}
