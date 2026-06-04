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

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Represents an object that can be deserialized from a specific tag or a packet.
 *
 * @param <T> the type of NBT tag used for deserialization
 * @see Serializable
 */
public interface Serializable<T extends Tag> {
    /**
     * Creates a codec for a serializable object.
     *
     * @param factory the factory for the object to create
     * @param <T> the type of tag to use
     * @param <S> the type of object to create
     * @return a codec for this type of object
     */
    static <T extends Tag, S extends Serializable<T>> Codec<S> createCodec(@NotNull Supplier<S> factory) {
        return new Codec<>() {
            @Override
            public <O> DataResult<Pair<S, O>> decode(DynamicOps<O> ops, O input) {
                Tag tag = ops.convertTo(NbtOps.INSTANCE, input);
                S s = factory.get();
                try {
                    s.readTag((T) tag);
                } catch (ClassCastException ignore) {
                    return DataResult.error(() -> "Invalid tag type: " + tag.getClass().getName());
                }
                return DataResult.success(Pair.of(s, input));
            }

            @Override
            public <O> DataResult<O> encode(S input, DynamicOps<O> ops, O prefix) {
                return DataResult.success(NbtOps.INSTANCE.convertTo(ops, input.createTag()));
            }
        };
    }

    /**
     * Serializes this object as a tag.
     *
     * @return the created tag
     * @see #readTag(Tag)
     */
    @NotNull
    T createTag();

    /**
     * Deserializes this object's state from a tag.
     *
     * @param tag the tag to be read
     * @see #createTag()
     */
    void readTag(@NotNull T tag);
}
