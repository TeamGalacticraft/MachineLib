/*
 * Copyright (c) 2021-2023 Team Galacticraft
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
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the types of resource that are be stored in a storage
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

    /**
     * The text of the resource type.
     */
    private final @NotNull Component name;

    /**
     * Constructs a new resource type.
     *
     * @param name the name of the resource.
     */
    @Contract(pure = true)
    ResourceType(@NotNull Component name) {
        this.name = name;
    }

    /**
     * Returns the resource type with the given ID.
     *
     * @param id The ID of the resource type.
     * @return The resource type with the given ID.
     */
    @Contract(pure = true)
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
     * Returns the name of the resource type.
     *
     * @return The text of the resource type.
     */
    @Contract(pure = true)
    public @NotNull Component getName() {
        return this.name;
    }

    /**
     * Returns whether the resource type is associated with slots.
     *
     * @return whether the resource type is associated with slots.
     */
    @Contract(pure = true)
    public boolean matchesSlots() {
        return this != ANY && this != NONE && this != ENERGY;
    }

    /**
     * Returns whether the resource types can have groups applied to them.
     *
     * @return whether the resource types can have groups applied to them.
     */
    @Contract(pure = true)
    public boolean matchesGroups() {
        return this != NONE && this != ENERGY;
    }

    /**
     * Returns whether the given resource type is compatible with this resource type.
     *
     * @param other The other resource type.
     * @return Whether the given resource type is compatible with this resource type.
     */
    @Contract(pure = true)
    public boolean willAcceptResource(ResourceType other) {
        return this != NONE && (this == other || this == ANY);
    }
}
