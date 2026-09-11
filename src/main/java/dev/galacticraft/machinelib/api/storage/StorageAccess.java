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

package dev.galacticraft.machinelib.api.storage;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.component.DataComponentPatch;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Something that can contains resources.
 *
 * @param <Resource> the type of resource this storage can contain. Must be comparable by identity.
 */
public interface StorageAccess<Resource> {
    /**
     * Returns whether the storage is empty.
     *
     * @return {@code true} if the storage contains no resources, {@code false} otherwise.
     */
    boolean isEmpty();

    /**
     * Returns whether the storage is full.
     *
     * @return {@code true} if the storage cannot accept any more resources, {@code false} otherwise.
     */
    boolean isFull();

    /**
     * {@return whether the storage can accept one of the specified resource}
     * This method is equivalent to {@link #canInsert(Resource, DataComponentPatch)}
     * except it assumes an empty component patch.
     *
     * @param resource the resource being tested
     * @see #canInsert(Resource, DataComponentPatch)
     */
    default boolean canInsert(@NotNull Resource resource) {
        return this.canInsert(resource, DataComponentPatch.EMPTY);
    }

    /**
     * {@return whether the storage can accept one of the specified resource}
     *
     * @param resource the resource being tested
     * @param components the components of the resource
     */
    boolean canInsert(@NotNull Resource resource, @NotNull DataComponentPatch components);

    /**
     * {@return whether the storage can fully accept the specified amount of the resource}
     * This method is equivalent to {@link #canInsert(Resource, DataComponentPatch, long)}
     * except it assumes an empty component patch.
     *
     * @param resource the resource being tested
     * @param amount the amount of the resource being tested
     * @see #canInsert(Resource, DataComponentPatch, long)
     */
    default boolean canInsert(@NotNull Resource resource, long amount) {
        return this.canInsert(resource, DataComponentPatch.EMPTY, amount);
    }

    /**
     * {@return whether the storage can fully accept the specified amount of the resource}
     *
     * @param resource the resource being tested
     * @param components the components of the resource
     * @param amount the amount of the resource being tested
     */
    boolean canInsert(@NotNull Resource resource, @NotNull DataComponentPatch components, long amount);

    /**
     * {@return the amount of the specified resource that the storage can accept} The storage is not modified.
     * This method is equivalent to {@link #tryInsert(Resource, DataComponentPatch, long)}
     * except it assumes an empty component patch.
     *
     * @param resource the resource being inserted
     * @param amount the amount of the resource that can be inserted
     * @see #tryInsert(Resource, DataComponentPatch, long)
     */
    default long tryInsert(@NotNull Resource resource, long amount) {
        return this.tryInsert(resource, DataComponentPatch.EMPTY, amount);
    }

    /**
     * {@return the amount of the specified resource that the storage can accept}
     * The storage is not modified.
     *
     * @param resource the resource being inserted
     * @param components the components of the resource
     * @param amount the amount of the resource that can be inserted
     */
    long tryInsert(@NotNull Resource resource, @NotNull DataComponentPatch components, long amount);

    /**
     * Inserts the specified amount of the resource into the storage.
     * This method is equivalent to {@link #insert(Resource, DataComponentPatch, long)}
     * except it assumes an empty component patch.
     *
     * @param resource the resource being inserted
     * @param amount the amount of the resource being inserted
     * @return the amount of the resource that was inserted
     * @see #insert(Resource, DataComponentPatch, long)
     */
    default long insert(@NotNull Resource resource, long amount) {
        return this.insert(resource, DataComponentPatch.EMPTY, amount);
    }

    /**
     * Inserts the specified amount of the resource into the storage.
     *
     * @param resource the resource being inserted
     * @param components the components of the resource
     * @param amount the amount of the resource being inserted
     * @return the amount of the resource that was inserted
     */
    long insert(@NotNull Resource resource, @NotNull DataComponentPatch components, long amount);

    /**
     * Inserts the specified amount of the resource into the storage.
     * If the storage already contains some resources, the resources will be added to the existing stack first.
     * This method is equivalent to {@link #insertMatching(Resource, DataComponentPatch, long)}
     * except it assumes an empty component patch.
     *
     * @param resource the resource being inserted
     * @param amount the amount of the resource being inserted
     * @return the amount of the resource that was inserted
     */
    default long insertMatching(@NotNull Resource resource, long amount) {
        return this.insertMatching(resource, DataComponentPatch.EMPTY, amount);
    }

    /**
     * Inserts the specified amount of the resource into the storage.
     * If the storage already contains some resources, the resources will be added to the existing stack first.
     *
     * @param resource the resource being inserted
     * @param components the components of the resource
     * @param amount the amount of the resource being inserted
     * @return the amount of the resource that was inserted
     */
    long insertMatching(@NotNull Resource resource, @NotNull DataComponentPatch components, long amount);

    /**
     * Returns whether the storage contains the specified resource.
     * This method ignores the components of the stored resource.
     *
     * @param resource the resource to check for
     * @return {@code true} if the storage contains the resource, {@code false} otherwise
     */
    boolean contains(@NotNull Resource resource);

    /**
     * Returns whether the storage contains the specified resource.
     *
     * @param resource the resource to check for
     * @param components the components of the resource. If {@code null}, the components will be ignored
     * @return {@code true} if the storage contains the resource, {@code false} otherwise
     */
    boolean contains(@NotNull Resource resource, @Nullable DataComponentPatch components);

    /**
     * Returns whether the storage can extract the specified amount of the resource.
     * This method is equivalent to {@link #canExtract(Resource, DataComponentPatch, long)}
     * except it ignores the components of the extracted resources.
     *
     * @param resource the resource being extracted
     * @param amount the amount of the resource being extracted
     * @return {@code true} if the storage can extract the resource, {@code false} otherwise
     */
    default boolean canExtract(@NotNull Resource resource, long amount) {
        return this.canExtract(resource, null, amount);
    }

    /**
     * Returns whether the storage can extract the specified amount of the resource.
     *
     * @param resource the resource being extracted
     * @param components the components of the resource. If {@code null}, the components will be ignored
     * @param amount the amount of the resource being extracted
     * @return {@code true} if the storage can extract the resource, {@code false} otherwise
     */
    boolean canExtract(@NotNull Resource resource, @Nullable DataComponentPatch components, long amount);

    /**
     * {@return the amount of the specified resource that can be extracted from the storage}.
     * This method is equivalent to {@link #tryExtract(Resource, DataComponentPatch, long)}
     * except it ignores the components of the extracted resources.
     *
     * @param resource the resource being extracted
     * @param amount the amount of the resource that can be extracted
     */
    default long tryExtract(@NotNull Resource resource, long amount) {
        return this.tryExtract(resource, null, amount);
    }

    /**
     * {@return the amount of the specified resource that can be extracted from the storage}
     *
     * @param resource the resource being extracted
     * @param components the components of the resource. If {@code null}, the components will be ignored
     * @param amount the amount of the resource that can be extracted
     */
    long tryExtract(@NotNull Resource resource, @Nullable DataComponentPatch components, long amount);

    /**
     * Extracts a single instance of the specified resource from the storage.
     * This method is equivalent to {@link #extractOne(Resource, DataComponentPatch)}
     * except it ignores the components of the extracted resources.
     *
     * @param resource the resource being extracted
     * @return {@code true} if one resource was extracted, {@code false} otherwise
     */
    default boolean extractOne(@NotNull Resource resource) {
        return this.extractOne(resource, null);
    }

    /**
     * Extracts a single instance of the specified resource from the storage.
     *
     * @param resource the resource being extracted
     * @param components the components of the resource. If {@code null}, the components will be ignored
     * @return {@code true} if one resource was extracted, {@code false} otherwise
     */
    boolean extractOne(@NotNull Resource resource, @Nullable DataComponentPatch components);

    /**
     * Extracts the specified amount of the resource from the storage.
     * This method is equivalent to {@link #extract(Resource, DataComponentPatch, long)}
     * except it ignores the components of the extracted resources.
     *
     * @param resource the resource being extracted
     * @param amount the amount of the resource being extracted
     * @return the amount of the resource that was extracted
     */
    default long extract(@NotNull Resource resource, long amount) {
        return this.extract(resource, null, amount);
    }

    /**
     * Extracts the specified amount of the resource from the storage.
     *
     * @param resource the resource being extracted
     * @param components the components of the resource. If {@code null}, the components will be ignored
     * @param amount the amount of the resource being extracted
     * @return the amount of the resource that was extracted
     */
    long extract(@NotNull Resource resource, @Nullable DataComponentPatch components, long amount);

    /**
     * Inserts the specified amount of the resource into the storage.
     *
     * @param resource the resource being inserted
     * @param components the components of the resource
     * @param amount the amount of the resource being inserted
     * @param context the transaction context. If {@code null}, the operation will not be part of a transaction
     * @return the amount of the resource that was inserted
     */
    // required for transfer API support
    @ApiStatus.Internal
    long insert(@NotNull Resource resource, @NotNull DataComponentPatch components, long amount, @Nullable TransactionContext context);

    /**
     * Extracts the specified amount of the resource from the storage.
     *
     * @param resource the resource being extracted
     * @param components the components of the resource
     * @param amount the amount of the resource being extracted
     * @param context the transaction context. If {@code null}, the operation will not be part of a transaction
     * @return the amount of the resource that was extracted
     */
    // required for transfer API support
    @ApiStatus.Internal
    long extract(@NotNull Resource resource, @Nullable DataComponentPatch components, long amount, @Nullable TransactionContext context);
}
