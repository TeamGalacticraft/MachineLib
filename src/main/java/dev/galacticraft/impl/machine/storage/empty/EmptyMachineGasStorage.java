/*
 * Copyright (c) 2019-2022 Team Galacticraft
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

import com.google.common.base.Predicates;
import com.mojang.datafixers.util.Either;
import dev.galacticraft.api.block.entity.MachineBlockEntity;
import dev.galacticraft.api.gas.Gas;
import dev.galacticraft.api.gas.GasVariant;
import dev.galacticraft.api.machine.storage.MachineGasStorage;
import dev.galacticraft.api.machine.storage.io.ExposedStorage;
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.machine.storage.io.ResourceType;
import dev.galacticraft.api.machine.storage.io.SlotType;
import dev.galacticraft.api.screen.MachineScreenHandler;
import dev.galacticraft.api.screen.StorageSyncHandler;
import dev.galacticraft.impl.gas.GasStack;
import dev.galacticraft.impl.util.EmptyIterator;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtElement;
import net.minecraft.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

public enum EmptyMachineGasStorage implements MachineGasStorage, ExposedStorage<Gas, GasVariant> {
    INSTANCE;

    private static final SlotType<Gas, GasVariant>[] NO_SLOTS = new SlotType[0];

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
    public @NotNull GasStack getStack(int slot) {
        return GasStack.EMPTY;
    }

    @Override
    public @NotNull GasVariant getVariant(int slot) {
        return GasVariant.blank();
    }

    @Override
    public GasVariant getResource(int slot) {
        return GasVariant.blank();
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
    public @NotNull ResourceType<Gas, GasVariant> getResource() {
        return ResourceType.GAS;
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
    public @NotNull GasStack extract(int slot, long amount, @Nullable TransactionContext context) {
        return GasStack.EMPTY;
    }

    @Override
    public @NotNull GasStack extract(int slot, @NotNull Tag<Gas> tag, long amount, @Nullable TransactionContext context) {
        return GasStack.EMPTY;
    }

    @Override
    public long extract(int slot, @NotNull Gas resource, long amount, @Nullable TransactionContext context) {
        return 0;
    }

    @Override
    public @NotNull GasStack replace(int slot, @NotNull GasVariant variant, long amount, @Nullable TransactionContext context) {
        return variant.toStack(amount);
    }

    @Override
    public long insert(int slot, @NotNull GasVariant variant, long amount, @Nullable TransactionContext context) {
        return 0;
    }

    @Override
    public long extract(int slot, @NotNull GasVariant variant, long amount, @Nullable TransactionContext context) {
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
    public SingleVariantStorage<GasVariant> getSlot(int slot) {
        throw new IndexOutOfBoundsException("No slots!");
    }

    @Override
    public Predicate<GasVariant> getFilter(int slot) {
        return Predicates.alwaysFalse();
    }

    @Override
    public boolean canAccess(@NotNull PlayerEntity player) {
        return false;
    }

    @Override
    public boolean canAccept(int slot, @NotNull GasVariant variant) {
        return false;
    }

    @Override
    public long count(@NotNull Gas resource) {
        return 0;
    }

    @Override
    public boolean containsAny(@NotNull Set<Gas> resources) {
        return false;
    }

    @Override
    public boolean containsAny(@NotNull Tag<Gas> tag) {
        return false;
    }

    @Override
    public @NotNull NbtElement writeNbt() {
        return NbtByte.ZERO;
    }

    @Override
    public void readNbt(@NotNull NbtElement nbt) {
    }

    @Override
    public void clear() {
    }

    @Override
    public ExposedStorage<Gas, GasVariant> view() {
        return this;
    }

    @Override
    public Storage<GasVariant> getExposedStorage(@Nullable Either<Integer, SlotType<?, ?>> either, @NotNull ResourceFlow flow) {
        return this;
    }

    @Override
    public @NotNull SlotType<Gas, GasVariant> @NotNull [] getTypes() {
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
    public long insert(GasVariant resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public long extract(GasVariant resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public Iterator<StorageView<GasVariant>> iterator(TransactionContext transaction) {
        return EmptyIterator.getInstance();
    }
}
