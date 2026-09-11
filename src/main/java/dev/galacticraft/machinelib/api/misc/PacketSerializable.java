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
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Represents an object that can be (de)serialized from a packet.
 *
 * @param <B> the type of buffer to use
 */
public interface PacketSerializable<B extends ByteBuf> {
    /**
     * Creates a codec for a packet-serializable object.
     *
     * @param constructor the constructor for the object to create
     * @param <B> the type of buffer to use
     * @param <T> the type of object to create
     * @return a codec for the object
     */
    static <B extends ByteBuf, T extends PacketSerializable<B>> StreamCodec<B, T> createCodec(@NotNull Supplier<@NotNull T> constructor) {
        return new StreamCodec<>() {
            @Override
            public @NotNull T decode(B buf) {
                T output = constructor.get();
                output.readPacket(buf);
                return output;
            }

            @Override
            public void encode(B buf, T input) {
                input.writePacket(buf);
            }
        };
    }

    /**
     * Deserializes this object's state from a buffer.
     *
     * @param buf the buffer to read from.
     * @see PacketSerializable#writePacket(B)
     */
    void readPacket(@NotNull B buf);

    /**
     * Serializes this object's state into a buffer.
     *
     * @param buf the buffer to write into
     * @see PacketSerializable#readPacket(B)
     */
    void writePacket(@NotNull B buf);
}
