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

package dev.galacticraft.machinelib.api.storage.slot.display;

import com.mojang.datafixers.util.Pair;
import dev.galacticraft.machinelib.impl.storage.exposed.slot.display.ItemSlotDisplayImpl;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Display information for an item slot.
 */
public interface ItemSlotDisplay {
    /**
     * Creates a new slot display with the specified coordinates (and no icon).
     *
     * @param x the x-coordinate of the item slot display
     * @param y the y-coordinate of the item slot display
     * @return a new slot display
     */
    @Contract("_, _ -> new")
    static @NotNull ItemSlotDisplay create(int x, int y) {
        return create(x, y, null);
    }

    /**
     * Creates a new slot display with the specified coordinates and icon.
     *
     * @param x    the x-coordinate of the item slot display
     * @param y    the y-coordinate of the item slot display
     * @param icon the icon of the item slot display (can be null)
     * @return a new slot display
     */
    @Contract("_, _, _ -> new")
    static @NotNull ItemSlotDisplay create(int x, int y, @Nullable Pair<ResourceLocation, ResourceLocation> icon) {
        return new ItemSlotDisplayImpl(x, y, icon);
    }

    /**
     * Returns the x-position of this slot
     *
     * @return the x-position of this slot
     */
    int x();

    /**
     * Returns the y-position of this slot
     *
     * @return the y-position of this slot
     */
    int y();

    /**
     * Returns the icon for this slot.
     * The first ID represents the texture atlas, while the second ID is the sprite (on the atlas).
     *
     * @return The icon for this slot. Returns {@code null} if no icon is assigned to this slot.
     */
    @Nullable Pair<ResourceLocation, ResourceLocation> icon();
}
