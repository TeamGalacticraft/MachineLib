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

package dev.galacticraft.machinelib.impl.menu.sync;

import dev.galacticraft.machinelib.api.misc.DeltaPacketSerializable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class EnumPacketSerializable<E extends Enum<E>> implements DeltaPacketSerializable<RegistryFriendlyByteBuf, Void> {
    private final E[] world;
    private final Supplier<E> getter;
    private final Consumer<E> setter;
    private E previous = null;

    public EnumPacketSerializable(E[] world, Supplier<E> getter, Consumer<E> setter) {
        this.world = world;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public boolean hasChanged(Void ignored) {
        return !Objects.equals(this.previous, this.getter.get());
    }

    @Override
    public void copyInto(Void other) {
        this.previous = this.getter.get();
    }

    @Override
    public void readPacket(@NotNull RegistryFriendlyByteBuf buf) {
        int i = buf.readByte();
        if (i == -1) {
            this.setter.accept(null);
        } else {
            this.setter.accept(this.world[i]);
        }
    }

    @Override
    public void writePacket(@NotNull RegistryFriendlyByteBuf buf) {
        E value = this.getter.get();
        if (value == null) {
            buf.writeByte(-1);
        } else {
            buf.writeByte(value.ordinal());
        }
    }

    @Override
    public Void createEquivalent() {
        return null;
    }
}
