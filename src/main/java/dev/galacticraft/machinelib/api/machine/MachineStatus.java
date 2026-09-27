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

package dev.galacticraft.machinelib.api.machine;

import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.machine.MachineStatusImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the status of a machine.
 *
 * @see MachineStatuses
 */
public interface MachineStatus {
    StreamCodec<RegistryFriendlyByteBuf, @Nullable MachineStatus> STREAM_CODEC = StreamCodec.ofMember(MachineStatus::writePacket, MachineStatus::readPacket);

    /**
     * Creates a new machine status.
     *
     * @param key The translation key of the machine status name.
     * @param color The color to use for the status text.
     * @param type The type of the machine status.
     * @return The newly created machine status.
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    static @NotNull MachineStatus create(@NotNull String key, @Nullable ChatFormatting color, @NotNull MachineStatus.Type type) {
        return new MachineStatusImpl(key, color, type);
    }

    /**
     * Creates a new machine status.
     *
     * @param key The translation key of the machine status name.
     * @param colorIndex The id of the color to use for the status text.
     * @param type The type of the machine status.
     * @return The newly created machine status.
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    static @NotNull MachineStatus create(@NotNull String key, int colorIndex, @NotNull MachineStatus.Type type) {
        return MachineStatus.create(key, ChatFormatting.getById(colorIndex), type);
    }

    static @NotNull CompoundTag createTag(@Nullable MachineStatus status) {
        CompoundTag tag = new CompoundTag();
        if (status == null) {
            tag.putByte(Constant.Nbt.TYPE, (byte) -1);
        } else {
            tag.putByte(Constant.Nbt.TYPE, (byte) status.getType().ordinal());
            tag.putString(Constant.Nbt.KEY, status.getKey());
            tag.putByte(Constant.Nbt.COLOR, (byte) status.getColorIndex());
        }
        return tag;
    }

    static @Nullable MachineStatus readTag(@NotNull CompoundTag tag) {
        byte type = tag.getByte(Constant.Nbt.TYPE);
        if (type == -1) {
            return null;
        }

        byte colorIndex = 0;
        if (tag.contains(Constant.Nbt.COLOR, Tag.TAG_BYTE)) {
            colorIndex = tag.getByte(Constant.Nbt.COLOR);
        }

        String key;
        if (tag.contains(Constant.Nbt.KEY, Tag.TAG_STRING)) {
            key = tag.getString(Constant.Nbt.KEY);
        } else {
            return null;
        }

        return MachineStatus.create(key, colorIndex, Type.values()[type]);
    }

    /**
     * Serializes this machine status to a packet
     *
     * @param buf the buffer to write to
     */
    static void writePacket(@Nullable MachineStatus status, @NotNull RegistryFriendlyByteBuf buf) {
        if (status == null) {
            buf.writeByte(-1);
            return;
        }
        buf.writeByte(status.getType().ordinal());
        buf.writeByte(status.getColorIndex());
        buf.writeUtf(status.getKey());
    }

    /**
     * Deserializes this machine status form a packet, based on the machine in use
     *
     * @param buf the buffer to write to
     * @return the deserialized machine status
     */
    static @Nullable MachineStatus readPacket(@NotNull RegistryFriendlyByteBuf buf) {
        byte b = buf.readByte();
        if (b == -1) return null;
        byte colorIndex = buf.readByte();
        String key = buf.readUtf();
        return MachineStatus.create(key, colorIndex, Type.values()[b]);
    }

    /**
     * {@return the translation key of the machine status}
     */
    @NotNull
    String getKey();

    /**
     * {@return the color of the machine status}
     */
    @Nullable
    ChatFormatting getColor();

    /**
     * {@return the id of the color of the machine status}
     */
    int getColorIndex();

    /**
     * {@return the name of the machine status}
     */
    @NotNull
    Component getText();

    /**
     * {@return the type of the machine status}
     */
    @NotNull
    MachineStatus.Type getType();

    /**
     * Represents the types of machine statuses.
     */
    enum Type {
        /**
         * The machine is active and generating resources.
         */
        WORKING(true),
        /**
         * The machine is active at reduced efficiency.
         */
        PARTIALLY_WORKING(true),
        /**
         * The machine is missing a resource it needs to function.
         * Prefer more specific static types over this one.
         *
         * @see #MISSING_ENERGY
         * @see #MISSING_FLUIDS
         * @see #MISSING_ITEMS
         */
        MISSING_RESOURCE(false),
        /**
         * The machine is missing a fluid it needs to function.
         */
        MISSING_FLUIDS(false),
        /**
         * The machine does not have the amount of energy needed to function.
         */
        MISSING_ENERGY(false),
        /**
         * The machine does not have the items needed to function.
         */
        MISSING_ITEMS(false),
        /**
         * The machine's output is blocked/full.
         */
        OUTPUT_FULL(false),
        /**
         * All other problems.
         */
        OTHER(false);

        /**
         * Whether the machine is considered working with this status type.
         */
        private final boolean active;

        @Contract(pure = true)
        Type(boolean active) {
            this.active = active;
        }

        /**
         * {@return whether the machine should be considered to be working}
         */
        @Contract(pure = true)
        public boolean isActive() {
            return this.active;
        }
    }
}
