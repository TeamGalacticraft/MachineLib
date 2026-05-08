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

import dev.galacticraft.machinelib.api.multiblock.*;
import dev.galacticraft.machinelib.api.multiblock.components.MultiblockSecurityComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;

import java.util.List;

/**
 * Default immutable implementation of {@link MultiblockDefinition}.
 */
public record SimpleMultiblockDefinition(ResourceLocation id, MultiblockPattern pattern, List<FormationRule> rules,
                                         MultiblockPartInteractionHandler interactionHandler,
                                         MultiblockMenuFactory menuFactory,
                                         List<MultiblockComponentFactoryEntry<?>> componentFactories) implements MultiblockDefinition {

    /**
     * Creates a simple multiblock definition.
     *
     * @param id                 unique multiblock id
     * @param pattern            structure pattern
     * @param rules              formation rules
     * @param interactionHandler optional part interaction handler
     * @param menuFactory        optional menu factory
     */
    public SimpleMultiblockDefinition(
            final ResourceLocation id,
            final MultiblockPattern pattern,
            final List<FormationRule> rules,
            final MultiblockPartInteractionHandler interactionHandler,
            final MultiblockMenuFactory menuFactory,
            final List<MultiblockComponentFactoryEntry<?>> componentFactories
    ) {
        this.id = id;
        this.pattern = pattern;
        this.rules = List.copyOf(rules);
        this.interactionHandler = interactionHandler;
        this.menuFactory = menuFactory;
        this.componentFactories = List.copyOf(componentFactories);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceLocation id() {
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultiblockPattern pattern() {
        return this.pattern;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FormationRule> rules() {
        return this.rules;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultiblockMenuFactory menuFactory() {
        return this.menuFactory;
    }

    /**
     * Handles interaction with a formed part.
     *
     * <p>The custom interaction handler runs first. If it returns
     * {@link InteractionResult#PASS}, this method applies standard multiblock
     * security checks and then opens the configured menu if one exists.</p>
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

        return MultiblockMenuOpener.open(
                context,
                this.menuFactory
        );
    }

}