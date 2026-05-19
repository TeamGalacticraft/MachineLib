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
import dev.galacticraft.machinelib.api.multiblock.port.conflict.MultiblockPortConflictRuleAssignment;
import dev.galacticraft.machinelib.api.multiblock.visual.MultiblockVisualFactory;
import dev.galacticraft.machinelib.api.multiblock.visual.PreviewableMultiblockVisualFactory;
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
     * Gets the optional client-side visual factory for this multiblock definition.
     *
     * <p>The visual factory is only used on the client. The server stores it as part
     * of the definition metadata but never creates or renders visual instances.</p>
     *
     * @return visual factory, or {@code null} if this multiblock has no custom visual
     */
    default MultiblockVisualFactory visualFactory() {
        return null;
    }

    /**
     * Gets the optional glTF visual model id used for GUI previews.
     *
     * <p>This id comes from the registered visual factory rather than being guessed
     * from the multiblock definition id. For example, a multiblock registered as
     * {@code machinelib:test_iron_cube} may preview the visual model
     * {@code machinelib:engineering_bay}.</p>
     *
     * @return preview glTF model id, or {@code null} if this definition has no
     * previewable visual
     */
    default ResourceLocation previewVisualModelId() {
        final MultiblockVisualFactory factory = this.visualFactory();

        if (factory instanceof PreviewableMultiblockVisualFactory previewable) {
            return previewable.previewVisualModelId();
        }

        return null;
    }

    /**
     * Checks whether a configured port is valid for this multiblock definition.
     *
     * <p>A valid port must be on an exposed face and must match at least one
     * registered port rule. This method is used for runtime player edits so packet
     * handlers do not duplicate definition validation logic.</p>
     *
     * @param port configured port to validate
     * @return {@code true} if this definition allows the port
     */
    default boolean allowsPort(final ConfiguredMultiblockPort port) {
        if (!this.exposedFaces().contains(port.face())) {
            return false;
        }

        for (final MultiblockPortRule rule : this.portRules()) {
            if (rule.allows(port)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets optional port conflict rule assignments.
     *
     * <p>MachineLib applies no port conflicts by default. Definitions may opt into
     * conflict rules globally, by port type, by pattern-relative block position, or
     * by exact pattern-relative face.</p>
     *
     * @return immutable conflict rule assignments
     */
    default List<MultiblockPortConflictRuleAssignment> portConflictRules() {
        return List.of();
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