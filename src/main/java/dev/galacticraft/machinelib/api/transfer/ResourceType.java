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

import com.mojang.serialization.Codec;
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
 * Defines the types of resource that are be stored in a storage
 */
@SuppressWarnings("unused")
public enum ResourceType implements StringRepresentable {
    /**
     * No resources can be stored/transferred.
     */
    NONE(0b000, Component.translatable(Constant.TranslationKey.NONE).setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY))),
    /**
     * Energy can be stored/transferred.
     */
    ENERGY(0b001, Component.translatable(Constant.TranslationKey.ENERGY).setStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE))),
    /**
     * Items can be stored/transferred.
     */
    ITEM(0b010, Component.translatable(Constant.TranslationKey.ITEM).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD))),
    /**
     * Fluids can be stored/transferred.
     */
    FLUID(0b100, Component.translatable(Constant.TranslationKey.FLUID).setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))),
    /**
     * All resources can be stored/transferred.
     */
    ANY(0b111, Component.translatable(Constant.TranslationKey.ANY).setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)));

    public static final Codec<ResourceType> CODEC = StringRepresentable.fromValues(ResourceType::values);
    public static final StreamCodec<ByteBuf, ResourceType> STREAM_CODEC = ByteBufCodecs.BYTE.map(i -> i == -1 ? null : values()[i], face -> face == null ? -1 : (byte) face.ordinal());

    /**
     * The text of the resource type.
     */
    private final @NotNull Component name;
    private final byte id;

    /**
     * Constructs a new resource type.
     *
     * @param name the name of the resource.
     */
    @Contract(pure = true)
    ResourceType(int id, @NotNull Component name) {
        this.id = (byte) id;
        this.name = name;
    }

    /**
     * {@return the resource type with the given ordinal}
     *
     * @param ordinal The ordinal of the resource type.
     */
    @Contract(pure = true)
    public static ResourceType getFromOrdinal(byte ordinal) {
        return switch (ordinal) {
            case 0 -> NONE;
            case 1 -> ENERGY;
            case 2 -> ITEM;
            case 3 -> FLUID;
            case 4 -> ANY;
            default -> throw new IllegalStateException("Unexpected ordinal: " + ordinal);
        };
    }

    @Contract(pure = true)
    public static ResourceType getFromId(byte id) {
        return switch (id) {
            case 0b000 -> NONE;
            case 0b001 -> ENERGY;
            case 0b010 -> ITEM;
            case 0b100 -> FLUID;
            case 0b111 -> ANY;
            default -> throw new IllegalArgumentException("Invalid id: " + id);
        };
    }

    /**
     * {@return the name of the resource type}
     */
    @Contract(pure = true)
    public @NotNull Component getName() {
        return this.name;
    }

    public byte getId() {
        return id;
    }

    /**
     * {@return whether the resource type is associated with slots}
     */
    @Contract(pure = true)
    public boolean matchesSlots() {
        return this != ANY && this != NONE && this != ENERGY;
    }

    /**
     * {@return whether the resource types can have groups applied to them}
     */
    @Contract(pure = true)
    public boolean matchesGroups() {
        return this != NONE && this != ENERGY;
    }

    /**
     * {@return whether the given resource type is compatible with this resource type}
     *
     * @param other The other resource type.
     */
    @Contract(pure = true)
    public boolean willAcceptResource(ResourceType other) {
        return this != NONE && (this == other || this == ANY);
    }

    @Override
    @Contract(pure = true)
    public @NotNull String getSerializedName() {
        return switch (this) {
            case NONE -> "none";
            case ENERGY -> "energy";
            case ITEM -> "item";
            case FLUID -> "fluid";
            case ANY -> "any";
        };
    }
}
