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

package dev.galacticraft.machinelib.gametest.misc;


import dev.galacticraft.machinelib.gametest.Util;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// todo: alternative to enum? string?
public enum ItemType {
    NONE((ItemLike[]) null),
    STACK_64(Items.IRON_INGOT, Items.STICK, Items.BLUE_DYE),
    STACK_64_NBT(true, Items.IRON_INGOT, Items.STICK, Items.BLUE_DYE),
    STACK_16(Items.SNOWBALL, Items.ENDER_PEARL, Items.EGG),
    STACK_16_NBT(true, Items.SNOWBALL, Items.ENDER_PEARL, Items.EGG),
    STACK_1(Items.IRON_AXE, Items.SPYGLASS, Items.POTION),
    STACK_1_NBT(true, Items.IRON_AXE, Items.SPYGLASS, Items.POTION);

    private final @NotNull ItemLike @Nullable [] items;
    private final boolean hasTag;

    ItemType(@NotNull ItemLike @Nullable ... items) {
        this(false, items);
    }

    ItemType(boolean hasTag, @NotNull ItemLike @Nullable ... items) {
        this.items = items;
        this.hasTag = hasTag;
    }

    public @NotNull ItemType getNbtInverse() {
        if (this == NONE) throw new AssertionError();
        return ItemType.values()[this.ordinal() + (this.hasTag ? -1 : 1)];
    }

    /**
     * WARNING: May change between calls, do not use NONE as type
     *
     * @return
     */
    public @NotNull ItemVariant generateVariant() {
        return this.generateVariant(0);
    }

    /**
     * WARNING: May change between calls, do not use NONE as type
     *
     * @return
     */
    public @NotNull ItemVariant generateVariant(int id) {
        if (this.items == null) throw new NullPointerException();
        if (id >= this.items.length) throw new AssertionError();
        return ItemVariant.of(this.items[id], this.hasTag ? Util.generateNbt() : null);
    }
}
