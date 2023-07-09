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
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public final class ResourceFilters {
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
    private static final ResourceFilter<?> ANY = (resource, tag) -> true;
    private static final ResourceFilter<?> NONE = (resource, tag) -> false;

    private ResourceFilters() {
    }

    public static <Resource> @NotNull ResourceFilter<Resource> ofNBT(CompoundTag tag) {
        return (resource, tag1) -> Utils.tagsEqual(tag, tag1);
    }

    @Contract(pure = true)
    public static <Resource> @NotNull ResourceFilter<Resource> ofResource(@NotNull Resource resource, @Nullable CompoundTag tag) {
        return (r, t) -> r == resource && Utils.tagsEqual(t, tag);
    }

    @Contract(pure = true)
    public static <Resource> @NotNull ResourceFilter<Resource> ofResource(@NotNull Resource resource) {
        return (r, tag) -> r == resource;
    }

    @Contract(pure = true)
    public static @NotNull ResourceFilter<Item> itemTagAnyNBT(@NotNull TagKey<Item> tag) {
        return (r, nbt) -> r != null && r.builtInRegistryHolder().is(tag);
    }

    @Contract(pure = true)
    public static @NotNull ResourceFilter<Item> itemTag(@NotNull TagKey<Item> tag, @Nullable CompoundTag nbt) {
        return (r, nbtC) -> r != null && r.builtInRegistryHolder().is(tag) && Utils.tagsEqual(nbtC, nbt);
    }

    @Contract(pure = true)
    public static @NotNull ResourceFilter<Item> itemTag(@NotNull TagKey<Item> tag) {
        return (r, nbt) -> r != null && r.builtInRegistryHolder().is(tag) && Utils.tagsEqual(nbt, null);
    }

    @Contract(pure = true)
    public static @NotNull ResourceFilter<Fluid> fluidTagAnyNBT(@NotNull TagKey<Fluid> tag) {
        return (r, nbt) -> r != null && r.builtInRegistryHolder().is(tag);
    }

    @Contract(pure = true)
    public static @NotNull ResourceFilter<Fluid> fluidTag(@NotNull TagKey<Fluid> tag, @Nullable CompoundTag nbt) {
        return (r, nbtC) -> r != null && r.builtInRegistryHolder().is(tag) && Utils.tagsEqual(nbtC, nbt);
    }

    @Contract(pure = true)
    public static @NotNull ResourceFilter<Fluid> fluidTag(@NotNull TagKey<Fluid> tag) {
        return (r, nbt) -> r != null && r.builtInRegistryHolder().is(tag) && Utils.tagsEqual(nbt, null);
    }

    @Contract(pure = true)
    public static @NotNull ResourceFilter<Item> isFluidStorage() {
        return (r, nbt) -> {
            if (r == null) return false;
            Storage<FluidVariant> storage = ContainerItemContext.withConstant(ItemVariant.of(r, nbt), 1).find(FluidStorage.ITEM);
            return storage != null;
        };
    }

    @Contract(pure = true)
    public static @NotNull ResourceFilter<Item> canExtractFluidStrict(@NotNull Fluid fluid) {
        return (r, nbt) -> {
            if (r == null) return false;
            Storage<FluidVariant> storage = ContainerItemContext.withConstant(ItemVariant.of(r, nbt), 1).find(FluidStorage.ITEM);
            if (storage == null || !storage.supportsExtraction()) return false;
            try (Transaction transaction = Transaction.openNested(Transaction.getCurrentUnsafe())) {
                if (storage.simulateExtract(FluidVariant.of(fluid), FluidConstants.BUCKET, transaction) > 0) {
                    return true;
                }
            }
            return false;
        };
    }

    @Contract(pure = true)
    public static @NotNull ResourceFilter<Item> canExtractFluidStrict(@NotNull Fluid fluid, @Nullable CompoundTag nbt) {
        return (r, nbtC) -> {
            if (r == null) return false;
            Storage<FluidVariant> storage = ContainerItemContext.withConstant(ItemVariant.of(r, nbtC), 1).find(FluidStorage.ITEM);
            if (storage == null || !storage.supportsExtraction()) return false;
            try (Transaction transaction = Transaction.openNested(Transaction.getCurrentUnsafe())) {
                if (storage.simulateExtract(FluidVariant.of(fluid, nbt), FluidConstants.BUCKET, transaction) > 0) {
                    return true;
                }
            }
            return false;
        };
    }

    @Contract(pure = true)
    public static @NotNull ResourceFilter<Item> canInsertFluidStrict(@NotNull Fluid fluid) {
        return (r, nbt) -> {
            if (r == null) return false;
            Storage<FluidVariant> storage = ContainerItemContext.withConstant(ItemVariant.of(r, nbt), 1).find(FluidStorage.ITEM);
            if (storage == null || !storage.supportsExtraction()) return false;
            try (Transaction transaction = Transaction.openNested(Transaction.getCurrentUnsafe())) {
                if (storage.simulateInsert(FluidVariant.of(fluid), FluidConstants.BUCKET, transaction) > 0) {
                    return true;
                }
            }
            return false;
        };
    }

    @Contract(pure = true)
    public static @NotNull ResourceFilter<Item> canInsertFluidStrict(@NotNull Fluid fluid, @Nullable CompoundTag nbt) {
        return (r, nbtC) -> {
            if (r == null) return false;
            Storage<FluidVariant> storage = ContainerItemContext.withConstant(ItemVariant.of(r, nbtC), 1).find(FluidStorage.ITEM);
            if (storage == null || !storage.supportsExtraction()) return false;
            try (Transaction transaction = Transaction.openNested(Transaction.getCurrentUnsafe())) {
                if (storage.simulateInsert(FluidVariant.of(fluid, nbt), FluidConstants.BUCKET, transaction) > 0) {
                    return true;
                }
            }
            return false;
        };
    }

    @Contract(pure = true)
    public static <Resource> @NotNull ResourceFilter<Resource> any() {
        return (ResourceFilter<Resource>) ANY;
    }

    public static <Resource> @NotNull ResourceFilter<Resource> none() {
        return (ResourceFilter<Resource>) NONE;
    }

    public static <Resource> @NotNull ResourceFilter<Resource> not(ResourceFilter<Resource> filter) {
        return (resource, tag) -> !filter.test(resource, tag);
    }

    public static <Resource> @NotNull ResourceFilter<Resource> and(ResourceFilter<Resource> a, ResourceFilter<Resource> b) {
        return (resource, tag) -> a.test(resource, tag) && b.test(resource, tag);
    }

    public static <Resource> @NotNull ResourceFilter<Resource> or(ResourceFilter<Resource> a, ResourceFilter<Resource> b) {
        return (resource, tag) -> a.test(resource, tag) || b.test(resource, tag);
    }
}
