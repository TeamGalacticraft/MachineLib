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

import dev.galacticraft.machinelib.api.compat.transfer.ExposedStorage;
import dev.galacticraft.machinelib.api.misc.DeltaPacketSerializable;
import dev.galacticraft.machinelib.api.misc.MutableModifiable;
import dev.galacticraft.machinelib.api.misc.PacketSerializable;
import dev.galacticraft.machinelib.api.misc.Serializable;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A storage that can store multiple of multiple instances of one type of resource (e.g., 10 sticks and 3 snowballs).
 *
 * @param <Resource> The type of resource (e.g., item or fluid) this storage can store. Must be comparable by identity.
 * @param <Slot> The type of slot this storage contains.
 * @see SlottedStorageAccess
 */
public interface ResourceStorage<Resource, Slot extends ResourceSlot<Resource>> extends Iterable<Slot>, MutableModifiable, SlottedStorageAccess<Resource, Slot>, Serializable<ListTag>, DeltaPacketSerializable<RegistryFriendlyByteBuf, long[]>, PacketSerializable<RegistryFriendlyByteBuf> {
    /**
     * Set the parent for this storage. This parent will be notified whenever the storage is modified.
     *
     * @param parent the parent of this storage.
     */
    void setParent(BlockEntity parent);

    boolean isValid();

    /**
     * Create an exposed storage for this storage.
     *
     * @param flow the flow of resources in the exposed storage.
     * @return the exposed storage, or {@code null} if this storage cannot be exposed in the given way.
     */
    @Nullable
    ExposedStorage<Resource, ? extends TransferVariant<Resource>> getExposedStorage(@NotNull ResourceFlow flow);
}
