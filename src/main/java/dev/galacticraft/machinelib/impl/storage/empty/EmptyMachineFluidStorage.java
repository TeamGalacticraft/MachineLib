/*
 * Copyright (c) 2021-2022 Team Galacticraft
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

package dev.galacticraft.machinelib.impl.storage.empty;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.screen.MachineMenu;
import dev.galacticraft.machinelib.api.screen.StorageSyncHandler;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.exposed.ExposedStorage;
import dev.galacticraft.machinelib.api.storage.io.ResourceFlow;
import dev.galacticraft.machinelib.api.storage.io.StorageSelection;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.StorageSlot;
import dev.galacticraft.machinelib.impl.fluid.FluidStack;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

@ApiStatus.Internal
public final class EmptyMachineFluidStorage implements MachineFluidStorage, ExposedStorage<Fluid, FluidVariant> {
    public static final MachineFluidStorage INSTANCE = new EmptyMachineFluidStorage();
    private static final SlotGroup[] NO_SLOTS = new SlotGroup[0];

    private EmptyMachineFluidStorage() {}

    @Override
    public boolean allowsGases(int slot) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public <M extends MachineBlockEntity> void addTanks(MachineMenu<M> handler) {
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public long getAmount(int slot) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public boolean canExposedExtract(int slot) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public boolean canExposedInsert(int slot) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public boolean canPlayerInsert(int slot) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public long getModCount() {
        return 0;
    }

    @Override
    public long getModCountUnsafe() {
        return 0;
    }

    @Override
    public @NotNull StorageSlot<Fluid, FluidVariant, FluidStack> getSlot(int slot) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public @NotNull Predicate<FluidVariant> getFilter(int slot) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public boolean canAccess(@NotNull Player player) {
        return false;
    }

    @Override
    public boolean canAccept(int slot, @NotNull FluidVariant variant) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public long count(@NotNull Fluid resource) {
        return 0;
    }

    @Override
    public long count(@NotNull FluidVariant resource) {
        return 0;
    }

    @Override
    public boolean containsAny(@NotNull Collection<Fluid> resources) {
        return false;
    }

    @Override
    public @NotNull Tag writeNbt() {
        return ByteTag.ZERO;
    }

    @Override
    public void readNbt(@NotNull Tag nbt) {
    }

    @Override
    public void clearContent() {
    }

    @Override
    public void setSlot(int slot, FluidVariant variant, long amount) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public Storage<FluidVariant> getExposedStorage(@Nullable StorageSelection either, @NotNull ResourceFlow flow) {
        return this;
    }

    @Override
    public @NotNull FluidVariant getResource(int slot) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public long getCapacity(int slot) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public @NotNull SlotGroup @NotNull [] getGroups() {
        return NO_SLOTS;
    }

    @Override
    public @NotNull StorageSyncHandler createSyncHandler() {
        return StorageSyncHandler.DEFAULT;
    }

    @Override
    public boolean supportsInsertion() {
        return false;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        return ObjectIterators.emptyIterator();
    }
}
