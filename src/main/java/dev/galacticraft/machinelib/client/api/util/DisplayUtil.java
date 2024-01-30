/*
 * Copyright (c) 2021-2024 Team Galacticraft
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
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.MachineLib;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public final class DisplayUtil {
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();

    static {
        if (NUMBER_FORMAT instanceof DecimalFormat fmt) {
            fmt.setRoundingMode(RoundingMode.FLOOR);
        }
        NUMBER_FORMAT.setGroupingUsed(true);
    }

    private DisplayUtil() {}

    public static String truncateDecimal(double d, int places) {
        NUMBER_FORMAT.setMaximumFractionDigits(places);
        return NUMBER_FORMAT.format(d);
    }

    @Contract(pure = true, value = "_ -> new")
    public static @NotNull MutableComponent formatNumber(long amount) {
        return Component.literal(NUMBER_FORMAT.format(amount));
    }

    @Contract(pure = true, value = "_ -> new")
    public static @NotNull MutableComponent formatEnergy(long amount) {
        return formatNumber(amount).append(Component.translatable(Constant.TranslationKey.UNIT_GJ));
    }

    @Contract(pure = true, value = "_, _ -> new")
    public static @NotNull MutableComponent formatFluid(long amount, boolean forceDetail) {
        return forceDetail || amount < MachineLib.CONFIG.bucketBreakpoint() ?
                Component.literal(truncateDecimal((double) amount / ((double)(FluidConstants.BUCKET / 1000)), 0)).append(Component.translatable(Constant.TranslationKey.UNIT_MILLIBUCKET))
                : Component.literal(truncateDecimal((double) amount / (double) FluidConstants.BUCKET, 2)).append(Component.translatable(Constant.TranslationKey.UNIT_BUCKET));
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

    public static void createFluidTooltip(@NotNull List<Component> tooltip, @Nullable Fluid fluid, @Nullable CompoundTag tag, long amount, long capacity) {
        if (amount == 0) {
            tooltip.add(Component.translatable(Constant.TranslationKey.TANK_EMPTY).setStyle(Constant.Text.GRAY_STYLE));
            return;
        }

        tooltip.add(Component.translatable(Constant.TranslationKey.TANK_CONTENTS).setStyle(Constant.Text.GRAY_STYLE).append(FluidVariantAttributes.getName(FluidVariant.of(fluid, tag))));
        tooltip.add(Component.translatable(Constant.TranslationKey.TANK_AMOUNT).setStyle(Constant.Text.GRAY_STYLE).append(DisplayUtil.formatFluid(amount, Screen.hasShiftDown()).setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))));

        if (capacity != -1) {
            tooltip.add(Component.translatable(Constant.TranslationKey.TANK_CAPACITY).setStyle(Constant.Text.GRAY_STYLE).append(DisplayUtil.formatFluid(capacity, Screen.hasShiftDown()).setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))));
        }
    }

    public static MutableComponent createEnergyTooltip(long amount, long capacity) {
        return Component.translatable(Constant.TranslationKey.CURRENT_ENERGY, DisplayUtil.formatNumber(amount).setStyle(Style.EMPTY.withColor(DisplayUtil.colorScale(amount, capacity))), DisplayUtil.formatEnergy(capacity).setStyle(Constant.Text.LIGHT_PURPLE_STYLE)).setStyle(Constant.Text.GRAY_STYLE);
    }
}
