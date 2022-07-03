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

package dev.galacticraft.api.machine.storage.io;

import dev.galacticraft.impl.MLConstant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the types of resources that can be stored in a {@link ConfiguredStorage}.
 * Has compile-time generics to allow for more specific resource filtering.
 * @param <T> The inner type of resource.
 * @param <V> The resource variant type.
 */
@SuppressWarnings("unused")
public final class ResourceType<T, V> {
    /**
     * No resources can be stored/transferred.
     */
    public static final ResourceType<?, ?> NONE = new ResourceType<>(0, Text.translatable(MLConstant.TranslationKey.NONE).setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
    /**
     * All resources can be stored/transferred.
     */
    public static final ResourceType<?, ?> ANY = new ResourceType<>(1, Text.translatable(MLConstant.TranslationKey.ANY).setStyle(Style.EMPTY.withColor(Formatting.AQUA)));
    /**
     * Energy can be stored/transferred.
     */
    public static final ResourceType<Long, Long> ENERGY = new ResourceType<>(2, Text.translatable(MLConstant.TranslationKey.ENERGY).setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE)));
    /**
     * Items can be stored/transferred.
     */
    public static final ResourceType<Item, ItemVariant> ITEM = new ResourceType<>(3, Text.translatable(MLConstant.TranslationKey.ITEM).setStyle(Style.EMPTY.withColor(Formatting.GOLD)));
    /**
     * Fluids can be stored/transferred.
     */
    public static final ResourceType<Fluid, FluidVariant> FLUID = new ResourceType<>(4, Text.translatable(MLConstant.TranslationKey.FLUID).setStyle(Style.EMPTY.withColor(Formatting.GREEN)));

    /**
     * The name of the resource type.
     */
    private final @NotNull Text name;
    /**
     * The ID of the resource type.
     */
    private final byte ordinal;

    private ResourceType(int ordinal, @NotNull Text name) {
        this.ordinal = (byte)ordinal;
        this.name = name;
    }

    /**
     * Returns an array of all resource types.
     * @return An array of all resource types.
     */
    @Contract(value = " -> new", pure = true)
    public static ResourceType<?, ?> @NotNull [] types() {
        return new ResourceType[] {NONE, ANY, ENERGY, ITEM, FLUID};
    }

    /**
     * Returns an array of all resource types except {@link #NONE} and {@link #ANY}.
     * @return An array of all resource types except {@link #NONE} and {@link #ANY}.
     */
    @Contract(value = " -> new", pure = true)
    public static ResourceType<?, ?> @NotNull [] normalTypes() {
        return new ResourceType[] {ENERGY, ITEM, FLUID};
    }

    /**
     * Returns the name of the resource type.
     * @return The name of the resource type.
     */
    public @NotNull Text getName() {
        return this.name;
    }

    /**
     * Returns whether the resource type is {@link #NONE} or {@link #ANY}.
     * @return Whether the resource type is {@link #NONE} or {@link #ANY}.
     */
    public boolean isSpecial() {
        return this == ANY || this == NONE;
    }

    /**
     * Returns the ID of the resource type.
     * @return The ID of the resource type.
     */
    public byte getOrdinal() {
        return ordinal;
    }

    /**
     * Returns the resource type with the given ID.
     * @param id The ID of the resource type.
     * @return The resource type with the given ID.
     */
    public static ResourceType<?, ?> getFromOrdinal(byte id) {
        return switch (id) {
            case 0 -> ANY;
            case 1 -> NONE;
            case 2 -> ENERGY;
            case 3 -> ITEM;
            case 4 -> FLUID;
            default -> throw new IllegalStateException("Unexpected id: " + id);
        };
    }

    /**
     * Returns whether the given resource type is compatible with this resource type.
     * @param other The other resource type.
     * @return Whether the given resource type is compatible with this resource type.
     */
    public boolean willAcceptResource(ResourceType<?, ?> other) {
        return this != NONE && (this == other || this == ANY);
    }
}
