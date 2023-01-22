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

import dev.galacticraft.machinelib.impl.MachineLib;
import dev.galacticraft.machinelib.impl.storage.slot.InputType;
import dev.galacticraft.machinelib.impl.storage.slot.SlotGroupTypeImpl;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Used for filtering, flow and I/O configuration of resources.
 * Groups can be arbitrary and cover multiple resource types.
 */
public interface SlotGroupType {
    /**
     * Constructs a new slot group with the given properties.
     *
     * @param color     the colour for highlighting
     * @param name      the name of the groups
     * @param inputType
     * @return a new slot group.
     */
    @Contract("_, _, _ -> new")
    static @NotNull SlotGroupType create(@NotNull TextColor color, @NotNull MutableComponent name, @NotNull InputType inputType) {
        return new SlotGroupTypeImpl(color, name.setStyle(Style.EMPTY.withColor(color)), inputType);
    }

    @Contract("_, _, _ -> new")
    static @NotNull SlotGroupType createAndRegister(@NotNull ResourceLocation id, @NotNull TextColor color, @NotNull InputType inputType) {
        return Registry.register(MachineLib.SLOT_GROUP_TYPE_REGISTRY, id, new SlotGroupTypeImpl(color, Component.translatable(id.getNamespace() + ".slot_group." + id.getPath()).setStyle(Style.EMPTY.withColor(color)), inputType));
    }

    /**
     * Returns the color of the slot type.
     *
     * @return The color of the slot type.
     */
    @Contract(pure = true)
    @NotNull TextColor color();

    /**
     * Returns the name of the slot type.
     *
     * @return The name of the slot type.
     */
    @Contract(pure = true)
    @NotNull Component name();

    @Contract(pure = true)
    InputType inputType();
}
