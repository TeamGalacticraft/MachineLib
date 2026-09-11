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

package dev.galacticraft.machinelib.api.menu;

import dev.galacticraft.machinelib.api.misc.DeltaPacketSerializable;
import dev.galacticraft.machinelib.api.util.FloatSupplier;
import dev.galacticraft.machinelib.impl.menu.sync.*;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

/**
 * A class that manages the synchronization of machine menu data between the client and server.
 * Use {@link #register(DeltaPacketSerializable)} to register
 * a new piece of interior-mutable data to be synchronized.
 * Use the other methods to register field-mutable, but interior-immutable data.
 *
 * @see DeltaPacketSerializable
 */
public abstract class MenuData {
    /**
     * The data to be synchronized.
     */
    protected final List<DeltaPacketSerializable<? super RegistryFriendlyByteBuf, ?>> data;
    protected final int syncId;

    protected MenuData(int syncId) {
        this.syncId = syncId;
        this.data = new ArrayList<>();
    }

    /**
     * Registers a new piece of data to be synchronized.
     *
     * @param datum the data to be synchronized
     * @param <T> the type of the data
     */
    public <T> void register(@NotNull DeltaPacketSerializable<? super RegistryFriendlyByteBuf, T> datum) {
        this.data.add(datum);
    }

    /**
     * Registers an immutable codec-serializable piece of data to be synchronized.
     *
     * @param codec the codec to serialize the data
     * @param getter provides the current value of the data
     * @param setter sets the value of the data
     * @param <T> the type of the data
     */
    public <T> void register(StreamCodec<? super RegistryFriendlyByteBuf, T> codec, Supplier<T> getter, Consumer<T> setter) {
        this.register(new StreamCodecPacketSerializable<>(codec, getter, setter));
    }

    /**
     * Registers a byte field to be synchronized.
     *
     * @param getter provides the current value of the data
     * @param setter sets the value of the data
     */
    public void registerBoolean(BooleanSupplier getter, BooleanConsumer setter) {
        this.register(new BytePacketSerializable(() -> getter.getAsBoolean() ? 1 : 0, b -> setter.accept(b != 0)));
    }

    /**
     * Registers a byte field to be synchronized.
     *
     * @param getter provides the current value of the data
     * @param setter sets the value of the data
     */
    public void registerByte(IntSupplier getter, IntConsumer setter) {
        this.register(new BytePacketSerializable(getter, setter));
    }

    /**
     * Registers a short field to be synchronized.
     *
     * @param getter provides the current value of the data
     * @param setter sets the value of the data
     */
    public void registerShort(IntSupplier getter, IntConsumer setter) {
        this.register(new ShortPacketSerializable(getter, setter));
    }

    /**
     * Registers an integer field to be synchronized.
     *
     * @param getter provides the current value of the data
     * @param setter sets the value of the data
     */
    public void registerInt(IntSupplier getter, IntConsumer setter) {
        this.register(new IntPacketSerializable(getter, setter));
    }

    /**
     * Registers a long field to be synchronized.
     *
     * @param getter provides the current value of the data
     * @param setter sets the value of the data
     */
    public void registerLong(LongSupplier getter, LongConsumer setter) {
        this.register(new LongPacketSerializable(getter, setter));
    }

    /**
     * Registers a float field to be synchronized.
     *
     * @param getter provides the current value of the data
     * @param setter sets the value of the data
     */
    public void registerFloat(FloatSupplier getter, FloatConsumer setter) {
        this.register(new FloatPacketSerializable(getter, setter));
    }

    /**
     * Registers a double field to be synchronized.
     *
     * @param getter provides the current value of the data
     * @param setter sets the value of the data
     */
    public void registerDouble(DoubleSupplier getter, DoubleConsumer setter) {
        this.register(new DoublePacketSerializable(getter, setter));
    }

    /**
     * Registers an enum field to be synchronized.
     *
     * @param world the enum values
     * @param getter provides the current value of the data
     * @param setter sets the value of the data
     * @param <E> the type of the enum
     */
    public <E extends Enum<E>> void registerEnum(E[] world, Supplier<E> getter, Consumer<E> setter) {
        this.register(new EnumPacketSerializable<>(world, getter, setter));
    }

    /**
     * Registers an array of booleans to be synchronized.
     *
     * @param source the source array
     * @param dest the destination array
     */
    public void registerBits(int len, boolean[] source, boolean[] dest) {
        this.register(new BitsPacketSerializable(len, source, dest));
    }

    @ApiStatus.Internal
    public abstract void synchronize();

    @ApiStatus.Internal
    public abstract void synchronizeFull();

    @ApiStatus.Internal
    public abstract void synchronizeInitial(RegistryFriendlyByteBuf buf);

    @ApiStatus.Internal
    public abstract void handle(RegistryFriendlyByteBuf buf);

    @ApiStatus.Internal
    public abstract void handleInitial(RegistryFriendlyByteBuf buf);
}
