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

package dev.galacticraft.machinelib.testmod.slot;

import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
import dev.galacticraft.machinelib.impl.storage.slot.InputType;
import dev.galacticraft.machinelib.testmod.Constant;
import net.minecraft.network.chat.TextColor;

public class TestModSlotGroupTypes {
    public static final SlotGroupType CHARGE = SlotGroupType.createAndRegister(Constant.id("charge"), TextColor.fromRgb(0xffff55), InputType.TRANSFER);
    public static final SlotGroupType DIRT = SlotGroupType.createAndRegister(Constant.id("dirt"), TextColor.fromRgb(0x774422), InputType.INPUT);
    public static final SlotGroupType DIAMONDS = SlotGroupType.createAndRegister(Constant.id("diamonds"), TextColor.fromRgb(0x55ffff), InputType.OUTPUT);

    public static final SlotGroupType SOLID_FUEL = SlotGroupType.createAndRegister(Constant.id("solid_fuel"), TextColor.fromRgb(0x000000), InputType.INPUT);
    public static final SlotGroupType TANK_IN = SlotGroupType.createAndRegister(Constant.id("tin"), TextColor.fromRgb(0xaa0000), InputType.INPUT);
    public static final SlotGroupType WATER = SlotGroupType.createAndRegister(Constant.id("water"), TextColor.fromRgb(0x0000ff), InputType.INPUT);

    public static void initialize() {
    }
}
