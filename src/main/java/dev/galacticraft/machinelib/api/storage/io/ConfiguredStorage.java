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

import dev.galacticraft.machinelib.api.screen.StorageSyncHandler;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A storage that can be configured for use in machine I/O faces and synced to the client.
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public interface ConfiguredStorage {
    /**
     * Returns the slot type of each slot in the storage.
     *
     * @return The slot type of each slot in the storage.
     */
    @Contract(pure = true)
    @NotNull SlotGroup @NotNull [] getGroups();

    /**
     * Returns whether the storage allows extraction from the given slot.
     * @param slot The slot to check.
     * @return Whether the storage allows extraction from the given slot.
     */
    @Contract(pure = true)
    boolean canExposedExtract(int slot);

    /**
     * Returns whether the storage allows insertion into the given slot.
     * @param slot The slot to check.
     * @return Whether the storage allows insertion into the given slot.
     */
    @Contract(pure = true)
    boolean canExposedInsert(int slot);

    /**
     * Creates a storage sync handler for this storage.
     * @return A storage sync handler for this storage.
     */
    @NotNull
    StorageSyncHandler createSyncHandler();
}