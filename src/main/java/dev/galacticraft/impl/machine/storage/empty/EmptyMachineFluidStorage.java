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

package dev.galacticraft.impl.machine.storage.empty;

import com.mojang.datafixers.util.Either;
import dev.galacticraft.api.block.entity.MachineBlockEntity;
import dev.galacticraft.api.machine.storage.MachineFluidStorage;
import dev.galacticraft.api.machine.storage.io.ExposedStorage;
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.machine.storage.io.ResourceType;
import dev.galacticraft.api.machine.storage.io.SlotType;
import dev.galacticraft.api.screen.MachineScreenHandler;
import dev.galacticraft.api.screen.StorageSyncHandler;
import dev.galacticraft.impl.fluid.FluidStack;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

public enum EmptyMachineFluidStorage implements MachineFluidStorage, ExposedStorage<Fluid, FluidVariant> {
    INSTANCE;

    private static final SlotType<Fluid, FluidVariant>[] NO_SLOTS = new SlotType[0];

    @Override
    public boolean allowsGases(int slot) {
        return false;
    }

    @Override
    public <M extends MachineBlockEntity> void addTanks(MachineScreenHandler<M> handler) {
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
    public @NotNull FluidStack getStack(int slot) {
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidVariant getVariant(int slot) {
        return FluidVariant.blank();
    }

    @Override
    public @NotNull FluidVariant getResource(int slot) {
        return FluidVariant.blank();
    }

    @Override
    public long getAmount(int slot) {
        return 0;
    }

    @Override
    public long getCapacity(int slot) {
        return 0;
    }

    @Override
    public @NotNull ResourceType<Fluid, FluidVariant> getResource() {
        return ResourceType.FLUID;
    }

    @Override
    public boolean canExposedExtract(int slot) {
        return false;
    }

    @Override
    public boolean canExposedInsert(int slot) {
        return false;
    }

    @Override
    public @NotNull FluidStack extract(int slot, long amount, @Nullable TransactionContext context) {
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack extract(int slot, @NotNull TagKey<Fluid> tag, long amount, @Nullable TransactionContext context) {
        return FluidStack.EMPTY;
    }

    @Override
    public long extract(int slot, @NotNull Fluid resource, long amount, @Nullable TransactionContext context) {
        return 0;
    }

    @Override
    public @NotNull FluidStack replace(int slot, @NotNull FluidVariant variant, long amount, @Nullable TransactionContext context) {
        return new FluidStack(variant.getFluid(), variant.copyNbt(), amount);
    }

    @Override
    public long insert(int slot, @NotNull FluidVariant variant, long amount, @Nullable TransactionContext context) {
        return 0;
    }

    @Override
    public long extract(int slot, @NotNull FluidVariant variant, long amount, @Nullable TransactionContext context) {
        return 0;
    }

    @Override
    public long getMaxCount(int slot) {
        return 0;
    }

    @Override
    public int getModCount() {
        return 0;
    }

    @Override
    public int getModCountUnsafe() {
        return 0;
    }

    @Override
    public int getSlotModCount(int slot) {
        return 0;
    }

    @Override
    public boolean isFull(int slot) {
        return true;
    }

    @Override
    public boolean isEmpty(int slot) {
        return true;
    }

    @Override
    public @NotNull SingleVariantStorage<FluidVariant> getSlot(int slot) {
        throw new IndexOutOfBoundsException("No slots!");
    }

    @Override
    public @NotNull Predicate<FluidVariant> getFilter(int slot) {
        return v -> false;
    }

    @Override
    public boolean canAccess(@NotNull Player player) {
        return false;
    }

    @Override
    public boolean canAccept(int slot, @NotNull FluidVariant variant) {
        return false;
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
    public ExposedStorage<Fluid, FluidVariant> view() {
        return this;
    }

    @Override
    public void setSlot(int slot, FluidVariant variant, long amount, boolean markDirty) {
    }

    @Override
    public Storage<FluidVariant> getExposedStorage(@Nullable Either<Integer, SlotType<?, ?>> either, @NotNull ResourceFlow flow) {
        return this;
    }

    @Override
    public @NotNull SlotType<Fluid, FluidVariant> @NotNull [] getTypes() {
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
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        return ObjectIterators.emptyIterator();
    }
}
