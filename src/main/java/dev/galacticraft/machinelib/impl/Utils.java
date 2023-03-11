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

package dev.galacticraft.machinelib.impl;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public final class Utils {
    private Utils() {
    }

    @Contract(value = "null, null -> true", pure = true)
    public static boolean tagsEqual(@Nullable CompoundTag a, @Nullable CompoundTag b) {
        if (a == null) {
            return b == null || b.isEmpty();
        } else if (b == null) {
            return a.isEmpty();
        } else {
            return a.equals(b);
        }
    }

    @Contract(pure = true)
    public static boolean itemsEqual(@Nullable Item a, @NotNull Item b) {
        return a == b || (a == null && b == Items.AIR);
    }

    public static @NotNull @Unmodifiable List<Component> wrapText(@NotNull Component text, int length) {
        return wrapText(text.getContents() instanceof TranslatableContents contents ? I18n.get(contents.getKey(), contents.getArgs()) : text.getString(), length, text.getStyle());
    }

    public static @NotNull @Unmodifiable List<Component> wrapText(@NotNull String text, int length, @Nullable Style style) {
        if (text.length() <= length) {
            return ImmutableList.of(Component.literal(text));
        }

        Minecraft minecraft = Minecraft.getInstance();
        ImmutableList.Builder<Component> list = ImmutableList.builder();
        StringBuilder builder = new StringBuilder();
        int lineLength = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            lineLength += minecraft.font.width(String.valueOf(c));
            if (Character.isWhitespace(c) && lineLength >= length) {
                lineLength = 0;
                list.add(Component.literal(builder.toString()).setStyle(style));
                builder.delete(0, builder.length());
            } else {
                builder.append(c);
            }
        }
        list.add(Component.literal(builder.toString()).setStyle(style));
        return list.build();
    }

    public static void breakpointMe(String s) {
        MachineLib.LOGGER.error(s);
    }
}
