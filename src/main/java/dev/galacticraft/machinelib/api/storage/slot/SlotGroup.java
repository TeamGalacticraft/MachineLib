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

package dev.galacticraft.machinelib.api.storage.slot;

import dev.galacticraft.machinelib.impl.storage.slot.SlotGroupImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Used for filtering, flow and I/O configuration of resources.
 */
public interface SlotGroup {
    @Contract("_, _, _ -> new")
    static @NotNull SlotGroup create(@NotNull TextColor color, @NotNull MutableComponent name, boolean automatable) {
        if (color.getValue() == 0xFFFFFFFF) throw new IllegalArgumentException("Color cannot be totally white (-1)! (It is used as a default/invalid number)");
        return new SlotGroupImpl(color, name, automatable);
    }

    /**
     * Returns the color of the slot type.
     * @return The color of the slot type.
     */
    @Contract(pure = true)
    @NotNull TextColor getColor();

    /**
     * Returns the name of the slot type.
     * @return The name of the slot type.
     */
    @Contract(pure = true)
    @NotNull Component getName();

    /**
     * Returns whether the slot can be accessed by external blocks (eg. pipes)
     * @return whether the slot can be accessed by external blocks
     */
    @Contract(pure = true)
    boolean isAutomatable();
}
