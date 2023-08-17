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

package dev.galacticraft.machinelib.api.machine;

import dev.galacticraft.machinelib.impl.Constant;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Contract;

/**
 * Default/builtin machine statuses.
 */
public final class MachineStatuses {
    /**
     * The machine does not have enough energy to run.
     */
    public static final MachineStatus NOT_ENOUGH_ENERGY = MachineStatus.create(Constant.TranslationKey.STATUS_NOT_ENOUGH_ENERGY, ChatFormatting.RED, MachineStatus.Type.MISSING_ENERGY);
    /**
     * The machine does not have the proper recipe inputs.
     */
    public static final MachineStatus INVALID_RECIPE = MachineStatus.create(Constant.TranslationKey.STATUS_INVALID_RECIPE, ChatFormatting.RED, MachineStatus.Type.MISSING_ITEMS);
    /**
     * The output slot is full.
     */
    public static final MachineStatus OUTPUT_FULL = MachineStatus.create(Constant.TranslationKey.STATUS_OUTPUT_FULL, ChatFormatting.GOLD, MachineStatus.Type.OUTPUT_FULL);
    /**
     * The energy storage is full.
     */
    public static final MachineStatus CAPACITOR_FULL = MachineStatus.create(Constant.TranslationKey.STATUS_CAPACITOR_FULL, ChatFormatting.GOLD, MachineStatus.Type.OUTPUT_FULL);
    /**
     * The machine is not running.
     */
    public static final MachineStatus IDLE = MachineStatus.create(Constant.TranslationKey.STATUS_IDLE, ChatFormatting.GOLD, MachineStatus.Type.MISSING_RESOURCE);
    /**
     * The machine is running.
     */
    public static final MachineStatus ACTIVE = MachineStatus.create(Constant.TranslationKey.STATUS_ACTIVE, ChatFormatting.GREEN, MachineStatus.Type.WORKING);

    /**
     * This class should not be instantiated.
     */
    @Contract(value = " -> fail", pure = true)
    private MachineStatuses() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
