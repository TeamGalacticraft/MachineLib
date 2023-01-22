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

package dev.galacticraft.machinelib.api.storage.slot;

import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.storage.slot.InputType;
import net.minecraft.network.chat.TextColor;

public final class SlotGroupTypes {
    private SlotGroupTypes() {}

    public static final SlotGroupType CHARGE = SlotGroupType.createAndRegister(Constant.id("charge"), TextColor.fromRgb(0xffff55), InputType.TRANSFER);
    public static final SlotGroupType FLUID_TRANSFER = SlotGroupType.createAndRegister(Constant.id("fluid_transfer"), TextColor.fromRgb(0x55ffff), InputType.TRANSFER);
    public static final SlotGroupType ITEM_TRANSFER = SlotGroupType.createAndRegister(Constant.id("item_transfer"), TextColor.fromRgb(0xff55ff), InputType.TRANSFER);

    public static void initialize() {
    }
}
