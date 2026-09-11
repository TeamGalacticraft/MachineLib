/*
 * Copyright (c) 2021-2026 Team Galacticraft
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

import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.component.DataComponentPatch;
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
    public static final ResourceFilter<Item> CAN_EXTRACT_ENERGY = (item, components) -> {
        if (item == null) return false;
        EnergyStorage storage = ContainerItemContext.withConstant(ItemVariant.of(item, components), 1).find(EnergyStorage.ITEM);
        if (storage == null || !storage.supportsExtraction()) return false;
        try (Transaction test = Transaction.openNested(Transaction.getCurrentUnsafe())) { // SAFE: the transaction is immediately canceled
            if (storage.extract(1, test) == 1) return true;
        }
        return false;
    };

    /**
     * A filter that determines if an item can have energy inserted into it.
     */
    public static final ResourceFilter<Item> CAN_INSERT_ENERGY = (item, components) -> {
        if (item == null) return false;
        EnergyStorage storage = ContainerItemContext.withConstant(ItemVariant.of(item, components), 1).find(EnergyStorage.ITEM);
        if (storage == null || !storage.supportsInsertion()) return false;
        try (Transaction test = Transaction.openNested(Transaction.getCurrentUnsafe())) { // SAFE: the transaction is immediately canceled
            if (storage.insert(1, test) == 1) return true;
        }
        return false;
    };

    /**
     * A constant filter that matches any resource.
     */
    private static final ResourceFilter<?> ANY = (resource, components) -> true;

    /**
     * A constant filter that rejects all resources.
     */
    private static final ResourceFilter<?> NONE = (resource, components) -> false;

    /**
     * This class cannot be instantiated as it only provides static utility methods.
     */
    private ResourceFilters() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a resource filter based on the given components.
     * The filter will check if the components of the resource is equal to the given components.
     *
     * @param components The components to match.
     * @param <Resource> The type of the resource being filtered.
     * @return A resource filter that checks the resource's components.
     */
    public static <Resource> @NotNull ResourceFilter<Resource> ofComponents(@NotNull DataComponentPatch components) {
        return (resource, components1) -> components1.equals(components);
    }

    /**
     * Creates a resource filter based on the given resource and components.
     * The filter will check if the resource is equal to the given resource and if the components of the
     * resource is equal to the given components.
     * If the component map is {@code null} it will also check for empty components.
     *
     * @param resource The resource to match.
     * @param components The components to match.
     * @param <Resource> The type of the resource being filtered.
     * @return A resource filter that checks the resource and components.
     */
    @Contract(pure = true)
    public static <Resource> @NotNull ResourceFilter<Resource> ofResource(@NotNull Resource resource, @Nullable DataComponentPatch components) {
        return (r, t) -> r == resource && (components == null || components.equals(t));
    }

    /**
     * Creates a resource filter based on the given resource.
     * The filter will check if the resource is equal to the given resource and will accept any components.
     *
     * @param resource The resource to match.
     * @param <Resource> The type of the resource being filtered.
     * @return A resource filter that checks the resource.
     */
    @Contract(pure = true)
    public static <Resource> @NotNull ResourceFilter<Resource> ofResource(@NotNull Resource resource) {
        return (r, components) -> r == resource;
    }

    /**
     * Creates a resource filter based on the given item tag.
     * The filter will check if the item has the given tag and will accept any components.
     *
     * @param tag The item tag to match.
     * @return A resource filter that checks the item is contained in the given tag.
     */
    @Contract(pure = true)
    public static @NotNull ResourceFilter<Item> itemTag(@NotNull TagKey<Item> tag) {
        return (r, components) -> r != null && r.builtInRegistryHolder().is(tag);
    }

    /**
     * Creates a resource filter based on the given item tag and components.
     * The filter will check if the item has the given tag and the components matches the provided components.
     * If the compound tag is {@code null} it will also accept an empty tag.
     *
     * @param tag The item tag to match.
     * @param components The components to match.
     * @return A resource filter that checks the item is contained in the given tag and has the correct components.
     */
    @Contract(pure = true)
    public static @NotNull ResourceFilter<Item> itemTag(@NotNull TagKey<Item> tag, @Nullable DataComponentPatch components) {
        return (r, componentsC) -> r != null && r.builtInRegistryHolder().is(tag) && (components == null || components.equals(componentsC));
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
        return (r, components) -> r != null && r.builtInRegistryHolder().is(tag);
    }

    /**
     * Creates a resource filter based on the given fluid tag and additional components.
     * The filter will check if the fluid is contained in the given tag and if the components matches.
     *
     * @param tag The fluid tag to match.
     * @param components The components to match (can be null).
     * @return A resource filter that checks if the fluid is contained in the given tag and if the components matches.
     */
    @Contract(pure = true)
    public static @NotNull ResourceFilter<Fluid> fluidTag(@NotNull TagKey<Fluid> tag, @Nullable DataComponentPatch components) {
        return (r, componentsC) -> r != null && r.builtInRegistryHolder().is(tag) && (components == null || components.equals(componentsC));
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
        return (r, components) -> {
            if (r == null) return false;
            return ContainerItemContext.withConstant(ItemVariant.of(r, components), 1).find(apiLookup) != null;
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
        return (r, components) -> {
            if (r == null) return false;
            Storage<FluidVariant> storage = ContainerItemContext.withConstant(ItemVariant.of(r, components), 1).find(FluidStorage.ITEM);
            if (storage == null || !storage.supportsExtraction()) return false;
            try (Transaction transaction = Transaction.openNested(Transaction.getCurrentUnsafe())) {
                if (storage.extract(FluidVariant.of(fluid), FluidConstants.BUCKET, transaction) > 0) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Checks if the specified item can have the given fluid (with components) extracted from it.
     *
     * @param fluid The desired fluid to extract.
     * @param components The desired fluid components.
     * @return A resource filter that checks if the item can have the given fluid (with components) extracted from it.
     */
    @Contract(pure = true)
    public static @NotNull ResourceFilter<Item> canExtractFluid(@NotNull Fluid fluid, @Nullable DataComponentPatch components) {
        return (r, componentsC) -> {
            if (r == null) return false;
            Storage<FluidVariant> storage = ContainerItemContext.withConstant(ItemVariant.of(r, componentsC), 1).find(FluidStorage.ITEM);
            if (storage == null || !storage.supportsExtraction()) return false;
            try (Transaction transaction = Transaction.openNested(Transaction.getCurrentUnsafe())) {
                if (storage.extract(FluidVariant.of(fluid, components), FluidConstants.BUCKET, transaction) > 0) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Checks if the specified item can have the given fluid (with components) extracted from it.
     *
     * @param tag The desired fluids to extract.
     * @return A resource filter that checks if the item can have the given fluid (with components) extracted from it.
     */
    @Contract(pure = true)
    public static @NotNull ResourceFilter<Item> canExtractFluid(@NotNull TagKey<Fluid> tag) {
        return (r, components) -> {
            if (r == null) return false;
            Storage<FluidVariant> storage = ContainerItemContext.withConstant(ItemVariant.of(r, components), 1).find(FluidStorage.ITEM);
            if (storage == null || !storage.supportsExtraction()) return false;
            try (Transaction transaction = Transaction.openNested(Transaction.getCurrentUnsafe())) {
                for (StorageView<FluidVariant> view : storage) {
                    FluidVariant resource = view.getResource();
                    if (!resource.isBlank() && resource.getFluid().is(tag)) {
                        if (storage.extract(resource, FluidConstants.BUCKET, transaction) > 0) {
                            return true;
                        }
                    }
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
        return (r, components) -> {
            if (r == null) return false;
            Storage<FluidVariant> storage = ContainerItemContext.withConstant(ItemVariant.of(r, components), 1).find(FluidStorage.ITEM);
            if (storage == null || !storage.supportsInsertion()) return false;
            try (Transaction transaction = Transaction.openNested(Transaction.getCurrentUnsafe())) {
                if (storage.insert(FluidVariant.of(fluid), FluidConstants.BUCKET, transaction) > 0) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Checks if the specified item can have the given fluid with components inserted into it.
     *
     * @param fluid The desired fluid to insert.
     * @param components The associated compound tag of the fluid. If the components is {@code null} it will also accept empty components.
     * @return A resource filter that checks if the item can have the given fluid inserted into it.
     */
    @Contract(pure = true)
    public static @NotNull ResourceFilter<Item> canInsertFluid(@NotNull Fluid fluid, @Nullable DataComponentPatch components) {
        return (r, componentsC) -> {
            if (r == null) return false;
            Storage<FluidVariant> storage = ContainerItemContext.withConstant(ItemVariant.of(r, componentsC), 1).find(FluidStorage.ITEM);
            if (storage == null || !storage.supportsInsertion()) return false;
            try (Transaction transaction = Transaction.openNested(Transaction.getCurrentUnsafe())) {
                if (storage.insert(FluidVariant.of(fluid, components), FluidConstants.BUCKET, transaction) > 0) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * {@return a resource filter that accepts any resource}
     *
     * @param <Resource> The type of resource to be filtered.
     */
    @Contract(pure = true)
    public static <Resource> @NotNull ResourceFilter<Resource> any() {
        return (ResourceFilter<Resource>) ANY;
    }

    /**
     * {@return a resource filter that rejects all resources}
     *
     * @param <Resource> The type of resource to be filtered.
     */
    public static <Resource> @NotNull ResourceFilter<Resource> none() {
        return (ResourceFilter<Resource>) NONE;
    }

    /**
     * {@return a resource filter that rejects resources that pass the given filter}
     *
     * @param filter The filter to apply.
     * @param <Resource> The type of resource to be filtered.
     */
    public static <Resource> @NotNull ResourceFilter<Resource> not(ResourceFilter<Resource> filter) {
        return (resource, components) -> !filter.test(resource, components);
    }

    /**
     * {@return a resource filter that applies two filters to a resource, and only accepts resources that pass both filters}
     *
     * @param a The first filter to apply.
     * @param b The second filter to apply.
     * @param <Resource> The type of resource to be filtered.
     */
    public static <Resource> @NotNull ResourceFilter<Resource> and(ResourceFilter<Resource> a, ResourceFilter<Resource> b) {
        return (resource, components) -> a.test(resource, components) && b.test(resource, components);
    }

    /**
     * {@return a resource filter that applies both filters to a resource, and accepts resources that pass either of the filters}
     *
     * @param a The first filter to apply.
     * @param b The second filter to apply.
     * @param <Resource> The type of resource to be filtered.
     */
    public static <Resource> @NotNull ResourceFilter<Resource> or(ResourceFilter<Resource> a, ResourceFilter<Resource> b) {
        return (resource, components) -> a.test(resource, components) || b.test(resource, components);
    }
}
