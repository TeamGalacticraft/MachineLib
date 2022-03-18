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

package dev.galacticraft.api.machine.storage;

import dev.galacticraft.api.machine.storage.io.ConfiguredStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.Tag;
import net.minecraft.util.Clearable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface ResourceStorage<T, V extends TransferVariant<T>, S> extends ConfiguredStorage<T, V>, Storage<V>, Clearable {
    int size();

    boolean isEmpty();

    @NotNull S getStack(int slot);

    boolean canExposedExtract(int slot);

    boolean canExposedInsert(int slot);

    default @NotNull S extract(int slot) {
        return this.extract(slot, (TransactionContext) null);
    }

    default @NotNull S extract(int slot, long amount) {
        return this.extract(slot, amount, null);
    }

    default @NotNull S extract(int slot, @NotNull Tag<T> tag) {
        return this.extract(slot, tag, null);
    }

    default @NotNull S extract(int slot, @NotNull Tag<T> tag, long amount) {
        return this.extract(slot, tag, amount, null);
    }

    default @NotNull S extract(int slot, @NotNull T resource) {
        return this.extract(slot, resource, null);
    }

    default @NotNull S extract(int slot, @NotNull T resource, long amount) {
        return this.extract(slot, resource, amount, null);
    }

    default @NotNull S replace(int slot, @NotNull V variant, long amount) {
        return this.replace(slot, variant, amount, null);
    }

    default long insert(int slot, @NotNull V variant, long amount) {
        return this.insert(slot, variant, amount, null);
    }

    default long extract(int slot, @NotNull V variant) {
        return this.extract(slot, variant, null);
    }

    default long extract(int slot, @NotNull V variant, long amount) {
        return this.extract(slot, variant, null);
    }

    default @NotNull S extract(int slot, @Nullable TransactionContext context) {
        return this.extract(slot, Long.MAX_VALUE, context);
    }

    @NotNull S extract(int slot, long amount, @Nullable TransactionContext context);

    default @NotNull S extract(int slot, @NotNull Tag<T> tag, @Nullable TransactionContext context) {
        return this.extract(slot, tag, Long.MAX_VALUE, context);
    }

    @NotNull S extract(int slot, @NotNull Tag<T> tag, long amount, @Nullable TransactionContext context);

    default @NotNull S extract(int slot, @NotNull T resource, @Nullable TransactionContext context) {
        return this.extract(slot, resource, Long.MAX_VALUE, context);
    }

    @NotNull S extract(int slot, @NotNull T resource, long amount, @Nullable TransactionContext context);

    @NotNull S replace(int slot, @NotNull V variant, long amount, @Nullable TransactionContext context);

    long insert(int slot, @NotNull V variant, long amount, @Nullable TransactionContext context);

    default long extract(int slot, @NotNull V variant, @Nullable TransactionContext context) {
        return this.extract(slot, variant, Long.MAX_VALUE, context);
    }

    long extract(int slot, @NotNull V variant, long amount, @Nullable TransactionContext context);

    long getMaxCount(int slot);

    /**
     * Returns the modification count of this inventory.
     * Can go DOWN due to cancelled transactions.
     *
     * @return the modification count of this inventory.
     */
    int getModCount();

    @ApiStatus.Internal
    StorageView<V> getSlot(int index);

    boolean canAccess(@NotNull PlayerEntity player);

    boolean canAccept(int slot, @NotNull V variant);

    long count(@NotNull T resource);

    boolean containsAny(@NotNull Set<T> resources);

    boolean containsAny(@NotNull Tag<T> resources);

    @Override
    void clear();

    @Override
    default long getVersion() {
        return this.getModCount();
    }
//    default ExposedStorage getExposedStorage(@NotNull Direction direction) {
//        return this.getExposedStorages()[direction.ordinal()];
//    }
//    ExposedStorage[] getExposedStorages();
}
