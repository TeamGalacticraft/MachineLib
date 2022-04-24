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

package dev.galacticraft.api.machine;

import dev.galacticraft.impl.machine.Constant;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/**
 * Default builtin machine statuses.
 */
public final class MachineStatuses {
    private MachineStatuses() {
        throw new IllegalStateException("MachineStatuses cannot be instantiated");
    }

    /**
     * The machine does not have enough energy to run.
     */
    public static final MachineStatus NOT_ENOUGH_ENERGY = MachineStatus.createAndRegister(new Identifier(Constant.MOD_ID, "not_enough_energy"), new TranslatableText("machine_status.machinelib.not_enough_energy").setStyle(Style.EMPTY.withColor(Formatting.RED)), MachineStatus.Type.MISSING_ENERGY);
    /**
     * The machine does not have the proper recipe inputs.
     */
    public static final MachineStatus INVALID_RECIPE = MachineStatus.createAndRegister(new Identifier(Constant.MOD_ID, "invalid_recipe"), new TranslatableText("machine_status.machinelib.invalid_recipe").setStyle(Style.EMPTY.withColor(Formatting.RED)), MachineStatus.Type.MISSING_ITEMS);
    /**
     * The output slot is full.
     */
    public static final MachineStatus OUTPUT_FULL = MachineStatus.createAndRegister(new Identifier(Constant.MOD_ID, "output_full"), new TranslatableText("machine_status.machinelib.output_full").setStyle(Style.EMPTY.withColor(Formatting.GOLD)), MachineStatus.Type.OUTPUT_FULL);
    /**
     * The energy storage is full.
     */
    public static final MachineStatus CAPACITOR_FULL = MachineStatus.createAndRegister(new Identifier(Constant.MOD_ID, "capacitor_full"), new TranslatableText("machine_status.machinelib.capacitor_full").setStyle(Style.EMPTY.withColor(Formatting.GOLD)), MachineStatus.Type.OUTPUT_FULL);
    /**
     * The machine is not running.
     */
    public static final MachineStatus IDLE = MachineStatus.createAndRegister(new Identifier(Constant.MOD_ID, "idle"), new TranslatableText("machine_status.machinelib.idle").setStyle(Style.EMPTY.withColor(Formatting.GOLD)), MachineStatus.Type.MISSING_RESOURCE);

    public static void init() {
    }
}
