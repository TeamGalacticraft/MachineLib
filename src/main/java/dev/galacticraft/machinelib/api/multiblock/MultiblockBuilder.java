package dev.galacticraft.machinelib.api.multiblock;

import net.minecraft.resources.ResourceLocation;

/**
 * Mutable builder API for a multiblock definition.
 */
public interface MultiblockBuilder {

    /**
     * Sets the multiblock pattern.
     *
     * @param pattern structure pattern
     * @return this builder
     */
    MultiblockBuilder pattern(MultiblockPattern pattern);

    /**
     * Adds a formation rule.
     *
     * @param rule formation rule
     * @return this builder
     */
    MultiblockBuilder rule(FormationRule rule);

    /**
     * Sets the handler called when a player interacts with a formed part.
     *
     * @param handler interaction handler
     * @return this builder
     */
    MultiblockBuilder onUsePart(MultiblockPartInteractionHandler handler);

    /**
     * Sets the menu factory used when a player interacts with a formed part and
     * no custom interaction handler claims the interaction.
     *
     * @param factory menu factory
     * @return this builder
     */
    MultiblockBuilder menu(MultiblockMenuFactory factory);

    /**
     * Adds a runtime component factory to this multiblock definition.
     *
     * @param id stable persistent component id
     * @param type component lookup type
     * @param factory component factory
     * @return this builder
     * @param <T> component type
     */
    <T extends MultiblockComponent> MultiblockBuilder component(
            ResourceLocation id,
            Class<T> type,
            MultiblockComponentFactory<? extends T> factory
    );

    /**
     * Builds the immutable multiblock definition.
     *
     * @return built definition
     */
    MultiblockDefinition build();

}