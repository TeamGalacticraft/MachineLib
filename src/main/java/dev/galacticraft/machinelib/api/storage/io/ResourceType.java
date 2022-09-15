/*
 * Copyright (c) 2021-2022 Team Galacticraft
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

package dev.galacticraft.machinelib.api.storage.io;

import dev.galacticraft.machinelib.impl.Constant;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the types of resources that can be stored in a {@link ConfiguredStorage}.
 */
@SuppressWarnings("unused")
public enum ResourceType {
    /**
     * No resources can be stored/transferred.
     */
    NONE(Component.translatable(Constant.TranslationKey.NONE).setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY))),
    /**
     * Energy can be stored/transferred.
     */
    ENERGY(Component.translatable(Constant.TranslationKey.ENERGY).setStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE))),
    /**
     * Items can be stored/transferred.
     */
    ITEM(Component.translatable(Constant.TranslationKey.ITEM).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD))),
    /**
     * Fluids can be stored/transferred.
     */
    FLUID(Component.translatable(Constant.TranslationKey.FLUID).setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))),
    /**
     * All resources can be stored/transferred.
     */
    ANY(Component.translatable(Constant.TranslationKey.ANY).setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)));

    private static final ResourceType[] normalTypes =  new ResourceType[] {ENERGY, ITEM, FLUID};
    private static final ResourceType[] types =  new ResourceType[] {NONE, ANY, ENERGY, ITEM, FLUID};

    /**
     * The name of the resource type.
     */
    private final @NotNull Component name;

    ResourceType(@NotNull Component name) {
        this.name = name;
    }

    /**
     * Returns an array of all resource types.
     * @return An array of all resource types.
     */
    @Contract(value = " -> new", pure = true)
    public static ResourceType @NotNull [] types() {
        return types;
    }

    /**
     * Returns an array of all resource types except {@link #NONE} and {@link #ANY}.
     * @return An array of all resource types except {@link #NONE} and {@link #ANY}.
     */
    @Contract(value = " -> new", pure = true)
    public static ResourceType @NotNull [] normalTypes() {
        return normalTypes;
    }

    /**
     * Returns the name of the resource type.
     * @return The name of the resource type.
     */
    public @NotNull Component getName() {
        return this.name;
    }

    /**
     * Returns whether the resource type is {@link #NONE} or {@link #ANY}.
     * @return Whether the resource type is {@link #NONE} or {@link #ANY}.
     */
    public boolean matchesSlots() {
        return this != ANY && this != NONE && this != ENERGY;
    }

    public boolean matchesGroups() {
        return this != NONE && this != ENERGY;
    }

    /**
     * Returns the resource type with the given ID.
     * @param id The ID of the resource type.
     * @return The resource type with the given ID.
     */
    public static ResourceType getFromOrdinal(byte id) {
        return switch (id) {
            case 0 -> NONE;
            case 1 -> ENERGY;
            case 2 -> ITEM;
            case 3 -> FLUID;
            case 4 -> ANY;
            default -> throw new IllegalStateException("Unexpected id: " + id);
        };
    }

    /**
     * Returns whether the given resource type is compatible with this resource type.
     * @param other The other resource type.
     * @return Whether the given resource type is compatible with this resource type.
     */
    public boolean willAcceptResource(ResourceType other) {
        return this != NONE && (this == other || this == ANY);
    }
}
