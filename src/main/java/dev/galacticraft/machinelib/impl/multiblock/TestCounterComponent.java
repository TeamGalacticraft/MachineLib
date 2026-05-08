/*
 * Copyright (c) 2021-2025 Team Galacticraft
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

package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.components.MultiblockComponent;
import dev.galacticraft.machinelib.api.multiblock.MultiblockComponentContext;
import net.minecraft.nbt.CompoundTag;

/**
 * Simple persistent test component that counts loaded ticks.
 */
public final class TestCounterComponent implements MultiblockComponent {

    private static final String TICKS = "Ticks";

    private int ticks;

    /**
     * Loads the saved tick counter.
     *
     * @param context component context
     * @param tag saved component tag
     */
    @Override
    public void load(
            final MultiblockComponentContext context,
            final CompoundTag tag
    ) {
        this.ticks = tag.getInt(TICKS);
    }

    /**
     * Saves the tick counter.
     *
     * @param context component context
     * @param tag component tag to write into
     */
    @Override
    public void save(
            final MultiblockComponentContext context,
            final CompoundTag tag
    ) {
        tag.putInt(TICKS, this.ticks);
    }

    /**
     * Increments the counter once per tick.
     *
     * @param context component context
     */
    @Override
    public void tick(final MultiblockComponentContext context) {
        this.ticks++;
        context.setChanged();
    }

    /**
     * Gets the number of loaded ticks this multiblock has accumulated.
     *
     * @return tick count
     */
    public int ticks() {
        return this.ticks;
    }

}