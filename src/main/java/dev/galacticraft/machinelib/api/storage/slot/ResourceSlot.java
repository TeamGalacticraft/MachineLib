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

package dev.galacticraft.machinelib.api.storage.slot;

import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.misc.MutableModifiable;
import dev.galacticraft.machinelib.api.misc.PacketSerializable;
import dev.galacticraft.machinelib.api.misc.Serializable;
import dev.galacticraft.machinelib.api.storage.ResourceStorage;
import dev.galacticraft.machinelib.api.storage.StorageAccess;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * A slot that can store multiple of a single instance of resource (e.g., sticks).
 * <br>
 * There are no i/o restrictions on this slot,
 * meaning that any resources can be inserted or extracted regardless of filter.
 *
 * @param <Resource> The type of resource (e.g., item) this slot can store. Must be comparable by identity.
 * @see StorageAccess
 */
public interface ResourceSlot<Resource> extends StorageAccess<Resource>, MutableModifiable, Serializable<CompoundTag>, PacketSerializable<RegistryFriendlyByteBuf> {
    /**
     * {@return whether this slot should still be interacted with}
     */
    boolean isValid();

    /**
     * {@return the way this slot is allowed to be interacted with}
     * Governs in-world (block-to-block) and player (menu) interactions.
     */
    TransferType transferMode();

    /**
     * {@return the resource stored in this slot} If the slot is empty, returns null.
     */
    @Nullable
    Resource getResource();

    /**
     * {@return the amount of the resource stored in this slot} If the slot is empty, returns 0.
     */
    long getAmount();

    /**
     * {@return the components of the resource stored in this slot} If the slot is empty, returns an empty patch.
     */
    @NotNull
    DataComponentPatch getComponents();

    /**
     * {@return the capacity of this slot} The real capacity may smaller if the resource stored has a smaller capacity.
     */
    long getCapacity();

    /**
     * {@return the capacity of this slot for a specific resource}
     *
     * @param resource The resource to check the capacity of.
     * @param components The components of the resource to check the capacity of.
     */
    long getCapacityFor(@NotNull Resource resource, @NotNull DataComponentPatch components);

    /**
     * {@return The current capacity of this slot}
     */
    long getRealCapacity();

    /**
     * {@return the filter for this slot} The filter determines what resources can be stored in this slot.
     * The filter may fail on empty ({@code null}) resource types, and it is possible
     * for the storage to contain values that fail this filter.
     *
     * @see ResourceFilter
     */
    @NotNull
    ResourceFilter<Resource> getFilter();

    /**
     * {@return whether this slot contains more than the given amount of the resource}
     *
     * @param amount The amount to check against.
     */
    boolean canExtract(long amount);

    /**
     * {@return the amount of resources that can be extracted from this slot} Does not extract the resources.
     *
     * @param amount The maximum amount to extract.
     */
    long tryExtract(long amount);

    /**
     * Extracts a single resource from this slot. Note that the components of the resource are not returned.
     *
     * @return the extracted resource, or {@code null} if the slot is empty.
     */
    @Nullable
    Resource extractOne();

    /**
     * Extracts the given amount of resources from this slot. Note that the components of the resource are not returned.
     *
     * @param amount The amount to extract.
     * @return the amount of resources extracted.
     */
    long extract(long amount);

    /**
     * Sets the resource stored in this slot to the given resource and amount.
     * For synchronization and testing purposes only.
     * Does not increment the modification counter.
     * Equivalent to {@link #set(Object, DataComponentPatch, long)} but assumes an empty component patch.
     *
     * @param resource The resource to set.
     * @param amount The amount of the resource to set.
     */
    @VisibleForTesting
    void set(@Nullable Resource resource, long amount);

    /**
     * Sets the resource stored in this slot to the given resource, components, and amount.
     * For synchronization and testing purposes only.
     * Does not increment the modification counter.
     *
     * @param resource The resource to set.
     * @param components The components of the resource to set.
     * @param amount The amount of the resource to set.
     */
    @VisibleForTesting
    void set(@Nullable Resource resource, @NotNull DataComponentPatch components, long amount);

    /**
     * Sets the parent of this slot. For internal use only.
     *
     * @param parent The parent to set.
     */
    @ApiStatus.Internal
    void _setParent(ResourceStorage<Resource, ?> parent);
}
