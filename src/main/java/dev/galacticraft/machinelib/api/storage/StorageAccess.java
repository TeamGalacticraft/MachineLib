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

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface StorageAccess<Resource> {
    boolean isEmpty();

    boolean isFull();

    boolean canInsert(@NotNull Resource resource);

    boolean canInsert(@NotNull Resource resource, @Nullable CompoundTag tag);

    boolean canInsert(@NotNull Resource resource, long amount);

    boolean canInsert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    long tryInsert(@NotNull Resource resource, long amount);

    long tryInsert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    long insert(@NotNull Resource resource, long amount);

    long insert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    long insertMatching(@NotNull Resource resource, long amount);

    long insertMatching(@NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    boolean contains(@NotNull Resource resource);

    boolean contains(@NotNull Resource resource, @Nullable CompoundTag tag);

    boolean canExtract(@NotNull Resource resource, long amount);

    boolean canExtract(@NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    long tryExtract(@NotNull Resource resource, long amount);

    long tryExtract(@NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    boolean extractOne(@NotNull Resource resource);

    boolean extractOne(@NotNull Resource resource, @Nullable CompoundTag tag);

    long extract(@NotNull Resource resource, long amount);

    long extract(@NotNull Resource resource, @Nullable CompoundTag tag, long amount);

    // required for FAPI
    long insert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context);

    // required for FAPI
    long extract(@Nullable Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context);
}
