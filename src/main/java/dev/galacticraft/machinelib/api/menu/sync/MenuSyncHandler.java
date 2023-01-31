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

import dev.galacticraft.machinelib.impl.menu.sync.simple.BooleansMenuSyncHandler;
import dev.galacticraft.machinelib.impl.menu.sync.simple.EnumMenuSyncHandler;
import dev.galacticraft.machinelib.impl.menu.sync.simple.IntMenuSyncHandler;
import dev.galacticraft.machinelib.impl.menu.sync.simple.LongMenuSyncHandler;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.*;

/**
 * Handles syncing of storage contents between the server and client.
 */
public interface MenuSyncHandler {
    @Contract(value = "_, _ -> new", pure = true)
    static @NotNull MenuSyncHandler simple(LongSupplier supplier, LongConsumer consumer) {
        return new LongMenuSyncHandler(supplier, consumer);
    }

    @Contract(value = "_, _ -> new", pure = true)
    static @NotNull MenuSyncHandler simple(IntSupplier supplier, IntConsumer consumer) {
        return new IntMenuSyncHandler(supplier, consumer);
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    static @NotNull <E extends Enum<E>> MenuSyncHandler simple(Supplier<E> supplier, Consumer<E> consumer, E[] world) {
        return new EnumMenuSyncHandler<>(supplier, consumer, world);
    }

    @Contract(value = "_, _ -> new", pure = true)
    static @NotNull MenuSyncHandler booleans(boolean[] input, boolean[] output) {
        return new BooleansMenuSyncHandler(input, output);
    }

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
