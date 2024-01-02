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

package dev.galacticraft.machinelib.api.transfer;

import dev.galacticraft.machinelib.impl.Constant;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A resource flow is a way to describe how a resource can be transferred between two storages.
 */
public enum ResourceFlow {
    /**
     * Resources can flow into the machine.
     */
    INPUT(Component.translatable(Constant.TranslationKey.IN).setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))),
    /**
     * Resources can flow out of the machine.
     */
    OUTPUT(Component.translatable(Constant.TranslationKey.OUT).setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED))),
    /**
     * Resources can flow into and out of the machine.
     */
    BOTH(Component.translatable(Constant.TranslationKey.BOTH).setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)));

    /**
     * do not mutate.
     */
    public static final ResourceFlow[] VALUES = ResourceFlow.values();

    /**
     * The text of the flow direction.
     */
    private final @NotNull Component name;

    /**
     * Creates a new resource flow.
     *
     * @param name The text of the flow direction.
     */
    @Contract(pure = true)
    ResourceFlow(@NotNull Component name) {
        this.name = name;
    }

    public static ResourceFlow getFromOrdinal(byte ordinal) {
        return VALUES[ordinal];
    }

    /**
     * Returns the name of the flow direction.
     *
     * @return The text of the flow direction.
     */
    @Contract(pure = true)
    public @NotNull Component getName() {
        return this.name;
    }

    /**
     * Returns whether this flow can flow into the given flow.
     *
     * @param flow The flow to check.
     * @return Whether this flow can flow into the given flow.
     */
    @Contract(pure = true)
    public boolean canFlowIn(ResourceFlow flow) {
        return this == flow || this == BOTH || flow == BOTH;
    }
}
