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

import dev.galacticraft.machinelib.api.multiblock.FormationRule;
import dev.galacticraft.machinelib.api.multiblock.MultiblockComponentFactoryEntry;
import dev.galacticraft.machinelib.api.multiblock.MultiblockDefinition;
import dev.galacticraft.machinelib.api.multiblock.MultiblockMenuFactory;
import dev.galacticraft.machinelib.api.multiblock.MultiblockPartInteractionContext;
import dev.galacticraft.machinelib.api.multiblock.MultiblockPartInteractionHandler;
import dev.galacticraft.machinelib.api.multiblock.MultiblockPattern;
import dev.galacticraft.machinelib.api.multiblock.components.MultiblockSecurityComponent;
import dev.galacticraft.machinelib.api.multiblock.port.ConfiguredMultiblockPort;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortFace;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortRule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;

import java.util.List;
import java.util.Set;

/**
 * Default immutable implementation of {@link MultiblockDefinition}.
 *
 * @param id unique multiblock id
 * @param pattern structure pattern
 * @param rules formation rules
 * @param interactionHandler optional part interaction handler
 * @param menuFactory optional menu factory
 * @param componentFactories runtime component factories
 * @param portRules allowed port rules
 * @param defaultPorts default configured ports
 * @param exposedFaces externally exposed pattern-relative faces
 */
public record SimpleMultiblockDefinition(
        ResourceLocation id,
        MultiblockPattern pattern,
        List<FormationRule> rules,
        MultiblockPartInteractionHandler interactionHandler,
        MultiblockMenuFactory menuFactory,
        List<MultiblockComponentFactoryEntry<?>> componentFactories,
        List<MultiblockPortRule> portRules,
        List<ConfiguredMultiblockPort> defaultPorts,
        Set<MultiblockPortFace> exposedFaces
) implements MultiblockDefinition {

    /**
     * Creates a simple immutable multiblock definition.
     */
    public SimpleMultiblockDefinition {
        rules = List.copyOf(rules);
        componentFactories = List.copyOf(componentFactories);
        portRules = List.copyOf(portRules);
        defaultPorts = List.copyOf(defaultPorts);
        exposedFaces = Set.copyOf(exposedFaces);
    }

    /**
     * Handles interaction with a formed multiblock part.
     *
     * <p>The custom interaction handler runs first. If it passes, security is
     * checked, then the configured menu is opened when one exists.</p>
     *
     * @param context interaction context
     * @return interaction result
     */
    @Override
    public InteractionResult usePart(final MultiblockPartInteractionContext context) {
        if (this.interactionHandler != null) {
            final InteractionResult result = this.interactionHandler.usePart(context);

            if (result != InteractionResult.PASS) {
                return result;
            }
        }

        final FormedMultiblockMachine machine =
                MultiblockManager.get(context.level()).getById(context.instanceId());

        if (machine != null) {
            final MultiblockSecurityComponent security =
                    machine.component(MultiblockSecurityComponent.class);

            if (security != null) {
                security.tryClaim(context.player());

                if (!security.hasAccess(context.player())) {
                    return InteractionResult.SUCCESS;
                }
            }
        }

        if (this.menuFactory == null) {
            return InteractionResult.PASS;
        }

        return MultiblockMenuOpener.open(context, this.menuFactory);
    }

}