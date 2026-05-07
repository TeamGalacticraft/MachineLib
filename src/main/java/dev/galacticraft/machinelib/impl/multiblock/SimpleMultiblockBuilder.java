package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.FormationRule;
import dev.galacticraft.machinelib.api.multiblock.MultiblockPartInteractionHandler;
import dev.galacticraft.machinelib.api.multiblock.MultiblockPattern;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable builder used to construct a simple immutable multiblock definition.
 */
public final class SimpleMultiblockBuilder {

    private final ResourceLocation id;
    private final List<FormationRule> rules = new ArrayList<>();

    private MultiblockPattern pattern;
    private MultiblockPartInteractionHandler interactionHandler;

    /**
     * Creates a builder for a multiblock id.
     *
     * @param id unique multiblock id
     */
    public SimpleMultiblockBuilder(final ResourceLocation id) {
        this.id = id;
    }

    /**
     * Sets the structure pattern for the multiblock.
     *
     * @param pattern structure pattern
     * @return this builder
     */
    public SimpleMultiblockBuilder pattern(final MultiblockPattern pattern) {
        this.pattern = pattern;
        return this;
    }

    /**
     * Adds a formation rule.
     *
     * @param rule formation rule
     * @return this builder
     */
    public SimpleMultiblockBuilder rule(final FormationRule rule) {
        this.rules.add(rule);
        return this;
    }

    /**
     * Sets the handler called when a player interacts with a formed part.
     *
     * @param interactionHandler interaction handler
     * @return this builder
     */
    public SimpleMultiblockBuilder onUsePart(final MultiblockPartInteractionHandler interactionHandler) {
        this.interactionHandler = interactionHandler;
        return this;
    }

    /**
     * Builds the immutable definition.
     *
     * @return built definition
     */
    public SimpleMultiblockDefinition build() {
        if (this.pattern == null) {
            throw new IllegalStateException("Multiblock " + this.id + " has no pattern");
        }

        return new SimpleMultiblockDefinition(
                this.id,
                this.pattern,
                this.rules,
                this.interactionHandler
        );
    }

}