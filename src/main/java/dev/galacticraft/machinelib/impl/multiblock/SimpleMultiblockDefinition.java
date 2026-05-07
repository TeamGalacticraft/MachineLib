package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.*;
import dev.galacticraft.machinelib.api.multiblock.components.MultiblockSecurityComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;

import java.util.List;

/**
 * Default immutable implementation of {@link MultiblockDefinition}.
 */
public final class SimpleMultiblockDefinition implements MultiblockDefinition {

    private final ResourceLocation id;
    private final MultiblockPattern pattern;
    private final List<FormationRule> rules;
    private final MultiblockPartInteractionHandler interactionHandler;
    private final MultiblockMenuFactory menuFactory;
    private final List<MultiblockComponentFactoryEntry<?>> componentFactories;

    /**
     * Creates a simple multiblock definition.
     *
     * @param id unique multiblock id
     * @param pattern structure pattern
     * @param rules formation rules
     * @param interactionHandler optional part interaction handler
     * @param menuFactory optional menu factory
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

    @Override
    public List<MultiblockComponentFactoryEntry<?>> componentFactories() {
        return this.componentFactories;
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