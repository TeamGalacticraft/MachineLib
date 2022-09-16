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

package dev.galacticraft.machinelib.api.storage.io;

import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.impl.storage.io.GroupStorageSelection;
import dev.galacticraft.machinelib.impl.storage.io.SlotStorageSelection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A filter for slots to narrow down the number of slots exposed to other blocks.
 * Either a slot or a slot group, never none or both.
 */
public interface StorageSelection {
    /**
     * Constructs a new storage selection that filters for a single slot.
     *
     * @param slot the slot to filter for
     * @return a new storage selection that filters for a single slot.
     */
    @Contract("_ -> new")
    static @NotNull StorageSelection createSlot(int slot) {
        if (slot < 0) throw new IndexOutOfBoundsException();
        return new SlotStorageSelection(slot);
    }

    /**
     * Constructs a new storage selection that filters for a group of slots.
     *
     * @param group the group to filter for
     * @return a new storage selection that filters for a group of slots.
     */
    @Contract("_ -> new")
    static @NotNull StorageSelection createGroup(@NotNull SlotGroup group) {
        return new GroupStorageSelection(group);
    }

    /**
     * Returns whether this selection filters for a slot.
     * If this method returns {@code false} it can be assumed that this selection is for a group.
     *
     * @return whether this selection filters for a slot.
     */
    @Contract(pure = true)
    boolean isSlot();

    /**
     * Returns the slot to filter for.
     *
     * @throws UnsupportedOperationException if this selection does not filter for a slot
     * @return the slot to filter for.
     */
    @Contract(pure = true)
    int getSlot();

    /**
     * Returns whether this selection filters for a group.
     * If this method returns {@code false} it can be assumed that this selection is for a slot.
     *
     * @return whether this selection filters for a group.
     */
    @Contract(pure = true)
    boolean isGroup();

    /**
     * Returns the group to filter for.
     *
     * @throws UnsupportedOperationException if this selection does not filter for a
     * @return the group to filter for.
     */
    @Contract(pure = true)
    @NotNull SlotGroup getGroup();
}
