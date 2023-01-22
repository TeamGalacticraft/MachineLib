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

package dev.galacticraft.machinelib.api.menu.sync;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Handles syncing of storage contents between the server and client.
 */
public interface MenuSyncHandler {
    MenuSyncHandler DEFAULT = new MenuSyncHandler() {
        @Override
        public boolean needsSyncing() {
            return false;
        }

        @Override
        public void sync(@NotNull FriendlyByteBuf buf) {
        }

        @Override
        public void read(@NotNull FriendlyByteBuf buf) {
        }
    };

    /**
     * Returns whether the storage needs syncing.
     *
     * @return Whether the storage needs syncing.
     */
    @Contract(pure = true)
    boolean needsSyncing();

    /**
     * Serializes the contents of the storage to the given buffer. Called on the (logical) server.
     *
     * @param buf The buffer to write to.
     */
    void sync(@NotNull FriendlyByteBuf buf);

    /**
     * Deserializes the contents of the storage from the given buffer. Called on the client.
     *
     * @param buf The buffer to read from.
     */
    void read(@NotNull FriendlyByteBuf buf);
}
