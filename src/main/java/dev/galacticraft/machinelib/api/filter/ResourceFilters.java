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

package dev.galacticraft.machinelib.api.filter;

import dev.galacticraft.machinelib.impl.Utils;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
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

/**
 * Useful built-in resource filters and generators for simple filters
 */
public final class ResourceFilters {
    /**
     * A filter that determines if an item can have energy extracted from it.
     */
    public static final ResourceFilter<Item> CAN_EXTRACT_ENERGY = (item, tag) -> {
        if (item == null) return false;
        EnergyStorage storage = ContainerItemContext.withConstant(ItemVariant.of(item, tag), 1).find(EnergyStorage.ITEM);
        if (storage == null || !storage.supportsExtraction()) return false;
        try (Transaction test = Transaction.openNested(Transaction.getCurrentUnsafe())) { // SAFE: the transaction is immediately cancelled
            if (storage.extract(1, test) == 1) return true;
        }
        return false;
    };

    /**
     * A filter that determines if an item can have energy inserted into it.
     */
    public static final ResourceFilter<Item> CAN_INSERT_ENERGY = (item, tag) -> {
        if (item == null) return false;
        EnergyStorage storage = ContainerItemContext.withConstant(ItemVariant.of(item, tag), 1).find(EnergyStorage.ITEM);
        if (storage == null || !storage.supportsInsertion()) return false;
        try (Transaction test = Transaction.openNested(Transaction.getCurrentUnsafe())) { // SAFE: the transaction is immediately cancelled
            if (storage.insert(1, test) == 1) return true;
        }
        return false;
    };

    /**
     * A constant filter that matches any resource.
     */
    private static final ResourceFilter<?> ANY = (resource, tag) -> true;

    /**
     * A constant filter that rejects all resources.
     */
    private static final ResourceFilter<?> NONE = (resource, tag) -> false;

    /**
     * This class cannot be instantiated as it only provides static utility methods.
     */
    private ResourceFilters() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a resource filter based on the given NBT.
     * The filter will check if the NBT of the resource is equal to the given NBT.
     *
     * @param tag The NBT to match.
     * @param <Resource> The type of the resource being filtered.
     * @return A resource filter that checks the resource's NBT.
     */
    public static <Resource> @NotNull ResourceFilter<Resource> ofNBT(CompoundTag tag) {
        return (resource, tag1) -> Utils.tagsEqual(tag, tag1);
    }

    /**
     * Creates a resource filter based on the given resource and compound tag.
     * The filter will check if the resource is equal to the given resource and if the compound tag of the
     * resource is equal to the given compound tag.
     * If the compound tag is {@code null} it will also check for an empty tag.
     *
     * @param resource The resource to match.
     * @param tag The NBT to match.
     * @param <Resource> The type of the resource being filtered.
     * @return A resource filter that checks the resource and NBT.
     */
    @Contract(pure = true)
    public static <Resource> @NotNull ResourceFilter<Resource> ofResource(@NotNull Resource resource, @Nullable CompoundTag tag) {
        return (r, t) -> r == resource && Utils.tagsEqual(t, tag);
    }

    /**
     * Creates a resource filter based on the given resource.
     * The filter will check if the resource is equal to the given resource and will accept any NBT.
     *
     * @param resource The resource to match.
     * @param <Resource> The type of the resource being filtered.
     * @return A resource filter that checks the resource.
     */
    @Contract(pure = true)
    public static <Resource> @NotNull ResourceFilter<Resource> ofResource(@NotNull Resource resource) {
        return (r, tag) -> r == resource;
    }

    /**
     * Creates a resource filter based on the given item tag.
     * The filter will check if the item has the given tag and will accept any NBT.
     *
     * @param tag The item tag to match.
     * @return A resource filter that checks the item is contained in the given tag.
     */
    @Contract(pure = true)
    public static @NotNull ResourceFilter<Item> itemTag(@NotNull TagKey<Item> tag) {
        return (r, nbt) -> r != null && r.builtInRegistryHolder().is(tag);
    }

    /**
     * Creates a resource filter based on the given item tag and NBT tag.
     * The filter will check if the item has the given tag and the NBT tag matches the provided tag.
     * If the compound tag is {@code null} it will also accept an empty tag.
     *
     * @param tag The item tag to match.
     * @param nbt The NBT to match.
     * @return A resource filter that checks the item is contained in the given tag and has the correct NBT.
     */
    @Contract(pure = true)
    public static @NotNull ResourceFilter<Item> itemTag(@NotNull TagKey<Item> tag, @Nullable CompoundTag nbt) {
        return (r, nbtC) -> r != null && r.builtInRegistryHolder().is(tag) && Utils.tagsEqual(nbtC, nbt);
    }

    /**
     * Creates a resource filter based on the given fluid tag.
     * The filter will check if the fluid is contained in the given tag.
     *
     * @param tag The fluid tag to match.
     * @return A resource filter that checks if the fluid is contained in the given tag.
     */
    @Contract(pure = true)
    public static @NotNull ResourceFilter<Fluid> fluidTag(@NotNull TagKey<Fluid> tag) {
        return (r, nbt) -> r != null && r.builtInRegistryHolder().is(tag);
    }

    /**
     * Creates a resource filter based on the given fluid tag and additional NBT data.
     * The filter will check if the fluid is contained in the given tag and if the NBT data matches.
     *
     * @param tag The fluid tag to match.
     * @param nbt The NBT data to match (can be null).
     * @return A resource filter that checks if the fluid is contained in the given tag and if the NBT data matches.
     */
    @Contract(pure = true)
    public static @NotNull ResourceFilter<Fluid> fluidTag(@NotNull TagKey<Fluid> tag, @Nullable CompoundTag nbt) {
        return (r, nbtC) -> r != null && r.builtInRegistryHolder().is(tag) && Utils.tagsEqual(nbtC, nbt);
    }

    /**
     * Creates a resource filter based on the given API lookup object.
     * The filter checks if the item provides the specified API.
     *
     * @param apiLookup The API lookup object to match.
     * @return A resource filter that checks if the item provides the specified API.
     */
    @Contract(pure = true)
    public static @NotNull ResourceFilter<Item> providesApi(ItemApiLookup<?, ContainerItemContext> apiLookup) {
        return (r, nbt) -> {
            if (r == null) return false;
            return ContainerItemContext.withConstant(ItemVariant.of(r, nbt), 1).find(apiLookup) != null;
        };
    }

    /**
     * Checks if the specified item can have the given fluid extracted from it.
     *
     * @param fluid The desired fluid.
     * @return A resource filter that checks if the item can have the given fluid extracted from it.
     */
    @Contract(pure = true)
    public static @NotNull ResourceFilter<Item> canExtractFluid(@NotNull Fluid fluid) {
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

    /**
     * Checks if the specified item can have the given fluid (with NBT) extracted from it.
     *
     * @param fluid The desired fluid to extract.
     * @param nbt The desired fluid NBT tag.
     * @return A resource filter that checks if the item can have the given fluid (with NBT) extracted from it.
     */
    @Contract(pure = true)
    public static @NotNull ResourceFilter<Item> canExtractFluid(@NotNull Fluid fluid, @Nullable CompoundTag nbt) {
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

    /**
     * Checks if the specified item can have the given fluid inserted into it.
     *
     * @param fluid The desired fluid to insert.
     * @return A resource filter that checks if the item can have the given fluid inserted into it.
     */
    @Contract(pure = true)
    public static @NotNull ResourceFilter<Item> canInsertFluid(@NotNull Fluid fluid) {
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

    /**
     * Checks if the specified item can have the given fluid with NBT inserted into it.
     *
     * @param fluid The desired fluid to insert.
     * @param nbt The associated compound tag of the fluid. If the NBT is {@code null} it will also accept empty compounds.
     * @return A resource filter that checks if the item can have the given fluid inserted into it.
     */
    @Contract(pure = true)
    public static @NotNull ResourceFilter<Item> canInsertFluid(@NotNull Fluid fluid, @Nullable CompoundTag nbt) {
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

    /**
     * Returns a resource filter that accepts any resource.
     *
     * @param <Resource> The type of resource to be filtered.
     * @return A resource filter that accepts any resource.
     */
    @Contract(pure = true)
    public static <Resource> @NotNull ResourceFilter<Resource> any() {
        return (ResourceFilter<Resource>) ANY;
    }

    /**
     * Returns a resource filter that rejects all resources.
     *
     * @param <Resource> The type of resource to be filtered.
     * @return A resource filter that rejects all resources.
     */
    public static <Resource> @NotNull ResourceFilter<Resource> none() {
        return (ResourceFilter<Resource>) NONE;
    }

    /**
     * Returns a resource filter that rejects resources that pass the given filter.
     *
     * @param filter The filter to apply.
     * @param <Resource> The type of resource to be filtered.
     * @return A resource filter that rejects resources that pass the given filter.
     */
    public static <Resource> @NotNull ResourceFilter<Resource> not(ResourceFilter<Resource> filter) {
        return (resource, tag) -> !filter.test(resource, tag);
    }

    /**
     * Returns a resource filter that applies two filters to a resource,
     * and only accepts resources that pass both filters.
     *
     * @param a The first filter to apply.
     * @param b The second filter to apply.
     * @param <Resource> The type of resource to be filtered.
     * @return A resource filter that applies both filters to a resource, and only accepts resources that pass both filters.
     */
    public static <Resource> @NotNull ResourceFilter<Resource> and(ResourceFilter<Resource> a, ResourceFilter<Resource> b) {
        return (resource, tag) -> a.test(resource, tag) && b.test(resource, tag);
    }

    /**
     * Returns a resource filter that applies two filters to a resource,
     * and accepts resources that pass either of the filters.
     *
     * @param a The first filter to apply.
     * @param b The second filter to apply.
     * @param <Resource> The type of resource to be filtered.
     * @return A resource filter that applies both filters to a resource, and accepts resources that pass either of the filters.
     */
    public static <Resource> @NotNull ResourceFilter<Resource> or(ResourceFilter<Resource> a, ResourceFilter<Resource> b) {
        return (resource, tag) -> a.test(resource, tag) || b.test(resource, tag);
    }
}
