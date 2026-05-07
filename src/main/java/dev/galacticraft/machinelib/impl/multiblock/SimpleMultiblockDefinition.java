package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.FormationRule;
import dev.galacticraft.machinelib.api.multiblock.MultiblockDefinition;
import dev.galacticraft.machinelib.api.multiblock.MultiblockPartInteractionContext;
import dev.galacticraft.machinelib.api.multiblock.MultiblockPartInteractionHandler;
import dev.galacticraft.machinelib.api.multiblock.MultiblockPattern;
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

    /**
     * Creates a simple multiblock definition.
     *
     * @param id unique multiblock id
     * @param pattern structure pattern
     * @param rules formation rules
     * @param interactionHandler optional part interaction handler
     */
    public SimpleMultiblockDefinition(
            final ResourceLocation id,
            final MultiblockPattern pattern,
            final List<FormationRule> rules,
            final MultiblockPartInteractionHandler interactionHandler
    ) {
        this.id = id;
        this.pattern = pattern;
        this.rules = List.copyOf(rules);
        this.interactionHandler = interactionHandler;
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
    public InteractionResult usePart(final MultiblockPartInteractionContext context) {
        if (this.interactionHandler == null) {
            return InteractionResult.PASS;
        }

        return this.interactionHandler.usePart(context);
    }

}