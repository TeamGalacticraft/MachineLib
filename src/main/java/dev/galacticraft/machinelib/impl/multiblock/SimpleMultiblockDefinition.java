package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.*;
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
            final MultiblockMenuFactory menuFactory
    ) {
        this.id = id;
        this.pattern = pattern;
        this.rules = List.copyOf(rules);
        this.interactionHandler = interactionHandler;
        this.menuFactory = menuFactory;
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
     * {@link InteractionResult#PASS}, the configured multiblock menu is opened
     * if one exists.</p>
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

        if (this.menuFactory == null) {
            return InteractionResult.PASS;
        }

        return MultiblockMenuOpener.open(
                context,
                this.menuFactory
        );
    }

}