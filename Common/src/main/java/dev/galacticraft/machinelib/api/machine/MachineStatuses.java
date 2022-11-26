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

package dev.galacticraft.machinelib.api.machine;

import dev.galacticraft.machinelib.impl.Constant;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Contract;

/**
 * Default builtin machine statuses.
 */
public final class MachineStatuses {
    @Contract(value = " -> fail", pure = true)
    private MachineStatuses() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * The machine does not have enough energy to run.
     */
    public static final MachineStatus NOT_ENOUGH_ENERGY = MachineStatus.createAndRegister(Constant.id("not_enough_energy"), Component.translatable(Constant.TranslationKey.STATUS_NOT_ENOUGH_ENERGY).setStyle(Style.EMPTY.withColor(ChatFormatting.RED)), MachineStatus.Type.MISSING_ENERGY);
    /**
     * The machine does not have the proper recipe inputs.
     */
    public static final MachineStatus INVALID_RECIPE = MachineStatus.createAndRegister(Constant.id("invalid_recipe"), Component.translatable(Constant.TranslationKey.STATUS_INVALID_RECIPE).setStyle(Style.EMPTY.withColor(ChatFormatting.RED)), MachineStatus.Type.MISSING_ITEMS);
    /**
     * The output slot is full.
     */
    public static final MachineStatus OUTPUT_FULL = MachineStatus.createAndRegister(Constant.id("output_full"), Component.translatable(Constant.TranslationKey.STATUS_OUTPUT_FULL).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), MachineStatus.Type.OUTPUT_FULL);
    /**
     * The energy storage is full.
     */
    public static final MachineStatus CAPACITOR_FULL = MachineStatus.createAndRegister(Constant.id("capacitor_full"), Component.translatable(Constant.TranslationKey.STATUS_CAPACITOR_FULL).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), MachineStatus.Type.OUTPUT_FULL);
    /**
     * The machine is not running.
     */
    public static final MachineStatus IDLE = MachineStatus.createAndRegister(Constant.id("idle"), Component.translatable(Constant.TranslationKey.STATUS_IDLE).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), MachineStatus.Type.MISSING_RESOURCE);
    /**
     * The machine is running.
     */
    public static final MachineStatus ACTIVE = MachineStatus.createAndRegister(Constant.id("active"), Component.translatable(Constant.TranslationKey.STATUS_ACTIVE).setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)), MachineStatus.Type.WORKING);

    /**
     * Utility method to initialize the class
     */
    @Contract(pure = true)
    public static void init() {
    }
}
