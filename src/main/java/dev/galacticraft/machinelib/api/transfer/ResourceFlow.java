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

package dev.galacticraft.machinelib.api.transfer;

import dev.galacticraft.machinelib.impl.Constant;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A resource flow is a way to describe how a resource can be transferred between two storages.
 */
public enum ResourceFlow implements StringRepresentable {
    /**
     * Resources can flow into the machine.
     */
    INPUT(0b01, Component.translatable(Constant.TranslationKey.IN).setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))),
    /**
     * Resources can flow out of the machine.
     */
    OUTPUT(0b10, Component.translatable(Constant.TranslationKey.OUT).setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED))),
    /**
     * Resources can flow into and out of the machine.
     */
    BOTH(0b11, Component.translatable(Constant.TranslationKey.BOTH).setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)));

    /**
     * do not mutate.
     */
    public static final ResourceFlow[] VALUES = ResourceFlow.values();

    public static final StringRepresentable.EnumCodec<ResourceFlow> CODEC = StringRepresentable.fromEnum(ResourceFlow::values);
    public static final StreamCodec<ByteBuf, ResourceFlow> STREAM_CODEC = ByteBufCodecs.BYTE.map(i -> i == -1 ? null : values()[i], face -> face == null ? -1 : (byte) face.ordinal());

    /**
     * The text of the flow direction.
     */
    private final @NotNull Component name;
    private final int id;

    /**
     * Creates a new resource flow.
     *
     * @param id
     * @param name The text of the flow direction.
     */
    @Contract(pure = true)
    ResourceFlow(int id, @NotNull Component name) {
        this.id = id;
        this.name = name;
    }

    public static ResourceFlow getFromOrdinal(byte ordinal) {
        return VALUES[ordinal];
    }

    public static ResourceFlow getFromId(byte id) {
        return switch (id) {
//            case 0b00 -> null;
            case 0b01 -> INPUT;
            case 0b10 -> OUTPUT;
            case 0b11 -> BOTH;
            default -> throw new IllegalArgumentException("Invalid id: " + id);
        };
    }

    public int getId() {
        return id;
    }

    /**
     * {@return The name of the flow direction}
     */
    @Contract(pure = true)
    public @NotNull Component getName() {
        return this.name;
    }

    /**
     * {@return whether this flow can flow in the direction of the given flow}.
     */
    @Contract(pure = true)
    public boolean canFlowIn(ResourceFlow flow) {
        return this == flow || this == BOTH || flow == BOTH;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String getSerializedName() {
        return switch (this) {
            case INPUT -> "input";
            case OUTPUT -> "output";
            case BOTH -> "both";
        };
    }
}
