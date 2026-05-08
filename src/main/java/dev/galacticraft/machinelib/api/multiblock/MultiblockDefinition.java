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

import dev.galacticraft.machinelib.api.multiblock.port.ConfiguredMultiblockPort;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortFace;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortRule;
import dev.galacticraft.machinelib.impl.multiblock.MultiblockMenuOpener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;

import java.util.List;
import java.util.Set;

/**
 * Immutable definition of a MachineLib multiblock.
 */
public interface MultiblockDefinition {

    ResourceLocation id();

    MultiblockPattern pattern();

    List<FormationRule> rules();

    default MultiblockMenuFactory menuFactory() {
        return null;
    }

    default List<MultiblockComponentFactoryEntry<?>> componentFactories() {
        return List.of();
    }

    default List<MultiblockPortRule> portRules() {
        return List.of();
    }

    default List<ConfiguredMultiblockPort> defaultPorts() {
        return List.of();
    }

    /**
     * Gets all pattern-relative faces that are externally exposed.
     *
     * <p>An exposed face is a face whose adjacent position is outside the
     * multiblock pattern or not occupied by another multiblock part. These faces
     * are used for validating ports, provider lookup, and future configuration
     * UI highlighting.</p>
     *
     * @return immutable exposed face set
     */
    default Set<MultiblockPortFace> exposedFaces() {
        return Set.of();
    }

    /**
     * Handles interaction with one formed part of this multiblock.
     *
     * @param context interaction context
     * @return interaction result
     */
    default InteractionResult usePart(final MultiblockPartInteractionContext context) {
        final MultiblockMenuFactory factory = this.menuFactory();

        if (factory == null) {
            return InteractionResult.PASS;
        }

        return MultiblockMenuOpener.open(context, factory);
    }

}