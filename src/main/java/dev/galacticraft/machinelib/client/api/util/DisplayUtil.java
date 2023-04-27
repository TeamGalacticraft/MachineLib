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

package dev.galacticraft.machinelib.client.api.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.text.DecimalFormat;
import java.util.List;

public final class DisplayUtil {
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat();
    private static final char DECIMAL_SEPARATOR;

    static {
        DECIMAL_SEPARATOR = NUMBER_FORMAT.getDecimalFormatSymbols().getDecimalSeparator();
        NUMBER_FORMAT.setGroupingUsed(true);
    }

    private DisplayUtil() {}

    public static String truncateDecimal(double d, int places) { //fixme R -> L languages?
        if (places == 0) return NUMBER_FORMAT.format(Math.round(d));
        String s = NUMBER_FORMAT.format(d);
        int dot = s.indexOf(DECIMAL_SEPARATOR);
        if (dot == -1) {
            return s;
        }
        return s.substring(0, Math.min(s.length(), dot + 1 + places));
    }

    @Contract(pure = true, value = "_ -> new")
    public static @NotNull MutableComponent formatEnergy(long amount) {
        if (amount >= 1_000_000L) {
            return Component.literal(truncateDecimal(amount / 1_000_000.0, 2) + " MgJ");
        } else {
            return Component.literal(NUMBER_FORMAT.format(amount) + " gJ");
        }
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

    public static int colorScale(double stored, double capacity) {
        double scale = 1.0 - (stored < 0 ? 0.0 : (capacity == 0 ? 1.0 : (1.0 - stored / capacity)));
        return Mth.hsvToRgb((float) ((120.0 / 360.0) * scale), 1.0f, 0.90f);
    }
}
