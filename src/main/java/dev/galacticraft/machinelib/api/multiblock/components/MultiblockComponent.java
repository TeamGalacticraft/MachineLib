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

import dev.galacticraft.machinelib.api.multiblock.MultiblockComponentContext;
import net.minecraft.nbt.CompoundTag;

/**
 * Runtime logic attached to a formed multiblock machine.
 *
 * <p>Components allow a formed structure to hold runtime behaviour such as
 * inventories, tanks, energy, recipes, progress, animation state, and machine
 * status.</p>
 *
 * <p>Persistent components should write only stable data. Structure identity,
 * origin, orientation, and definition id are already stored by the multiblock
 * saved-data system.</p>
 */
public interface MultiblockComponent {

    /**
     * Reads this component's persistent state.
     *
     * <p>This is called before {@link #onFormed(MultiblockComponentContext)} when
     * a machine is restored from saved data.</p>
     *
     * @param context component context
     * @param tag saved component tag
     */
    default void load(
            final MultiblockComponentContext context,
            final CompoundTag tag
    ) {

    }

    /**
     * Writes this component's persistent state.
     *
     * @param context component context
     * @param tag component tag to write into
     */
    default void save(
            final MultiblockComponentContext context,
            final CompoundTag tag
    ) {

    }

    /**
     * Called once after the component is created, loaded, and attached to a
     * formed machine.
     *
     * @param context component context
     */
    default void onFormed(final MultiblockComponentContext context) {

    }

    /**
     * Called once per server tick while the formed machine is loaded.
     *
     * @param context component context
     */
    default void tick(final MultiblockComponentContext context) {

    }

    /**
     * Called when the formed machine is permanently invalidated.
     *
     * @param context component context
     */
    default void onInvalidated(final MultiblockComponentContext context) {

    }

    /**
     * Called when the runtime machine is unloaded while persistent saved data is
     * kept.
     *
     * @param context component context
     */
    default void onRuntimeUnloaded(final MultiblockComponentContext context) {

    }

}