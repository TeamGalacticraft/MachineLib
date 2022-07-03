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

package dev.galacticraft.api.machine.storage.io;

import com.google.common.collect.ImmutableList;
import dev.galacticraft.impl.MLConstant;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A resource flow is a way to describe how a resource can be transferred between two storages.
 */
public enum ResourceFlow {
    /**
     * Resources can flow into the machine.
     */
    INPUT(Text.translatable(MLConstant.TranslationKey.IN).setStyle(Style.EMPTY.withColor(Formatting.GREEN))),
    /**
     * Resources can flow out of the machine.
     */
    OUTPUT(Text.translatable(MLConstant.TranslationKey.OUT).setStyle(Style.EMPTY.withColor(Formatting.DARK_RED))),
    /**
     * Resources can flow into and out of the machine.
     */
    BOTH(Text.translatable(MLConstant.TranslationKey.BOTH).setStyle(Style.EMPTY.withColor(Formatting.BLUE)));

    public static final List<ResourceFlow> ALL_FLOWS = ImmutableList.copyOf(ResourceFlow.values());

    /**
     * The name of the flow direction.
     */
    private final @NotNull Text name;

    /**
     * Creates a new resource flow.
     * @param name The name of the flow direction.
     */
    ResourceFlow(@NotNull Text name) {
        this.name = name;
    }

    /**
     * Returns the name of the flow direction.
     * @return The name of the flow direction.
     */
    public @NotNull Text getName() {
        return this.name;
    }

    /**
     * Returns whether this flow can flow into the given flow.
     * @param flow The flow to check.
     * @return Whether this flow can flow into the given flow.
     */
    public boolean canFlowIn(ResourceFlow flow) {
        return this == flow || this == BOTH;
    }
}
