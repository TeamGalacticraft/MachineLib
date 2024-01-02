/*
 * Copyright (c) 2021-2024 Team Galacticraft
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

package dev.galacticraft.machinelib.impl.menu.sync.simple;

import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class EnumMenuSyncHandler<E extends Enum<E>> implements MenuSyncHandler {
    private final Supplier<E> supplier;
    private final Consumer<E> consumer;
    private final E[] world;
    private E value;

    public EnumMenuSyncHandler(Supplier<E> supplier, Consumer<E> consumer, E[] world) {
        this.supplier = supplier;
        this.consumer = consumer;
        this.world = world;
    }

    @Override
    public boolean needsSyncing() {
        return this.value != this.supplier.get();
    }

    @Override
    public void sync(@NotNull FriendlyByteBuf buf) {
        this.value = this.supplier.get();
        buf.writeVarInt(this.value.ordinal());
    }

    @Override
    public void read(@NotNull FriendlyByteBuf buf) {
        this.value = this.world[buf.readVarInt()];
        this.consumer.accept(this.value);
    }
}
