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

package dev.galacticraft.machinelib.api.misc;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an object that can be (de)serialized from a packet.
 *
 * @param <B> the type of buffer to use
 * @param <T> the type of object to compare with
 * @see PacketSerializable
 * @see Equivalent
 */
public interface DeltaPacketSerializable<B extends ByteBuf, T> extends Equivalent<T>, PacketSerializable<B> {
    /**
     * Deserializes part of this object's state from a buffer.
     *
     * @param buf the buffer to read from.
     * @see DeltaPacketSerializable#writeDeltaPacket(B, T)
     */
    default void readDeltaPacket(@NotNull B buf) {
        this.readPacket(buf);
    }

    /**
     * Serializes the difference in object's state from the previous state into a buffer.
     *
     * @param buf the buffer to write into
     * @param previous the previous state of the object
     * @see DeltaPacketSerializable#readDeltaPacket(ByteBuf)
     */
    default void writeDeltaPacket(@NotNull B buf, T previous) {
        this.writePacket(buf);
    }

    @Nullable
    T createEquivalent();
}
