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

package dev.galacticraft.machinelib.impl.storage.slot;

import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@ApiStatus.Internal
public final class SlotGroupImpl implements SlotGroup {
    private final @NotNull TextColor color;
    private final @NotNull Component name;
    private final boolean automatable;

    public SlotGroupImpl(@NotNull TextColor color, @NotNull MutableComponent name, boolean automatable) {
        this.color = color;
        this.automatable = automatable;
        this.name = name.setStyle(Style.EMPTY.withColor(color));
    }

    @Override
    public @NotNull TextColor getColor() {
        return this.color;
    }

    @Override
    public @NotNull Component getName() {
        return this.name;
    }

    @Override
    public boolean isAutomatable() {
        return this.automatable;
    }

    @Override
    public String toString() {
        return "SlotTypeImpl{" +
                "color=" + color +
                ", name=" + name +
                ", automatable=" + automatable +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlotGroupImpl slotType = (SlotGroupImpl) o;
        return automatable == slotType.automatable && color.equals(slotType.color) && name.equals(slotType.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, name, automatable);
    }
}
