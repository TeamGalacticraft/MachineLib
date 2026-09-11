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

import dev.galacticraft.machinelib.api.misc.Modifiable;
import org.jetbrains.annotations.NotNull;

/**
 * A storage that can store multiple of multiple instances of one type of resource (e.g., 10 sticks and 3 snowballs).
 *
 * @param <Resource> The type of resource (e.g., item) this storage can store. Must be comparable by identity.
 * @param <Slot> The type of slot this storage contains.
 * @see StorageAccess
 */
public interface SlottedStorageAccess<Resource, Slot extends StorageAccess<Resource>> extends StorageAccess<Resource>, Modifiable, Iterable<Slot> {
    /**
     * {@return the number of slots in this storage}
     */
    int size();

    /**
     * {@return a slotted storage that is a subset of this storage}
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     */
    SlottedStorageAccess<Resource, Slot> subStorage(int start, int len);

    /**
     * {@return a slotted storage that is a subset of this storage}
     *
     * @param slots the indices of the slots to include
     */
    SlottedStorageAccess<Resource, Slot> subStorage(int... slots);

    /**
     * {@return the slots in this storage}
     * Do not modify the returned array.
     */
    Slot[] getSlots();

    /**
     * {@return the slot at the given index}
     */
    @NotNull
    Slot slot(int slot);

    @Override
    long getModifications();
}
