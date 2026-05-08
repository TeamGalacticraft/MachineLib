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

package dev.galacticraft.machinelib.api.multiblock;

import dev.galacticraft.machinelib.impl.multiblock.MultiblockMenuOpener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;

import java.util.List;

/**
 * Immutable definition of a MachineLib multiblock.
 *
 * <p>A definition describes the structure pattern, formation rules, and optional
 * runtime behaviour for a formed multiblock.</p>
 */
public interface MultiblockDefinition {

    /**
     * Gets the unique registry id for this multiblock.
     *
     * @return multiblock id
     */
    ResourceLocation id();

    /**
     * Gets the multiblock structure pattern.
     *
     * @return pattern
     */
    MultiblockPattern pattern();

    /**
     * Gets the formation rules for this multiblock.
     *
     * @return immutable rule list
     */
    List<FormationRule> rules();

    /**
     * Gets the optional menu factory for this multiblock.
     *
     * @return menu factory, or {@code null}
     */
    default MultiblockMenuFactory menuFactory() {
        return null;
    }

    /**
     * Gets runtime component factories attached to this definition.
     *
     * @return immutable component factory list
     */
    default List<MultiblockComponentFactoryEntry<?>> componentFactories() {
        return List.of();
    }

    /**
     * Handles interaction with one formed part of this multiblock.
     *
     * <p>The default implementation opens the configured multiblock menu if one
     * exists. Otherwise, the interaction passes through to the original clicked
     * block.</p>
     *
     * @param context interaction context
     * @return interaction result
     */
    default InteractionResult usePart(final MultiblockPartInteractionContext context) {
        final MultiblockMenuFactory factory = this.menuFactory();

        if (factory == null) {
            return InteractionResult.PASS;
        }

        return MultiblockMenuOpener.open(
                context,
                factory
        );
    }

}