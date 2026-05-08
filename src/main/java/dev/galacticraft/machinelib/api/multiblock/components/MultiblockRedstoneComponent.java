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

package dev.galacticraft.machinelib.api.multiblock.components;

import dev.galacticraft.machinelib.api.machine.configuration.RedstoneMode;
import dev.galacticraft.machinelib.api.multiblock.MultiblockComponentContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * Persistent redstone mode component for a formed multiblock machine.
 *
 * <p>This stores how the multiblock reacts to redstone. The powered state itself
 * belongs to {@link MultiblockStateComponent}, matching MachineLib's normal
 * split between redstone mode and machine state.</p>
 */
public final class MultiblockRedstoneComponent implements MultiblockComponent {

    private static final String MODE = "Mode";

    private RedstoneMode mode = RedstoneMode.IGNORE;
    private MultiblockComponentContext context;

    /**
     * Gets the current redstone mode.
     *
     * @return redstone mode
     */
    public RedstoneMode mode() {
        return this.mode;
    }

    /**
     * Sets the redstone mode.
     *
     * @param mode new redstone mode
     */
    public void setMode(final RedstoneMode mode) {
        if (this.mode == mode) {
            return;
        }

        this.mode = mode;
        this.setChanged();
    }

    /**
     * Checks whether the multiblock should be disabled based on the supplied
     * powered state.
     *
     * @param powered whether the multiblock is currently powered
     * @return {@code true} if redstone disables the machine
     */
    public boolean isDisabled(final boolean powered) {
        return !this.mode.isActive(powered);
    }

    /**
     * Loads persisted redstone mode.
     *
     * @param context component context
     * @param tag saved component tag
     */
    @Override
    public void load(
            final MultiblockComponentContext context,
            final CompoundTag tag
    ) {
        this.context = context;

        if (tag.contains(MODE, Tag.TAG_STRING)) {
            try {
                this.mode = RedstoneMode.valueOf(tag.getString(MODE));
            } catch (final IllegalArgumentException ignored) {
                this.mode = RedstoneMode.IGNORE;
            }
        }
    }

    /**
     * Saves persisted redstone mode.
     *
     * @param context component context
     * @param tag component tag to write into
     */
    @Override
    public void save(
            final MultiblockComponentContext context,
            final CompoundTag tag
    ) {
        tag.putString(
                MODE,
                this.mode.name()
        );
    }

    /**
     * Stores the active component context.
     *
     * @param context component context
     */
    @Override
    public void onFormed(final MultiblockComponentContext context) {
        this.context = context;
    }

    private void setChanged() {
        if (this.context != null) {
            this.context.setChanged();
        }
    }

}