package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.MultiblockDefinition;
import dev.galacticraft.machinelib.api.multiblock.MultiblockMenuContext;
import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import dev.galacticraft.machinelib.api.multiblock.MultiblockPartInteractionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Default immutable implementation of {@link MultiblockMenuContext}.
 */
public final class SimpleMultiblockMenuContext implements MultiblockMenuContext {

    private final MultiblockPartInteractionContext interaction;

    /**
     * Creates a menu context from an interaction context.
     *
     * @param interaction interaction that opened the menu
     */
    public SimpleMultiblockMenuContext(final MultiblockPartInteractionContext interaction) {
        this.interaction = interaction;
    }

    @Override
    public ServerLevel level() {
        return this.interaction.level();
    }

    @Override
    public ServerPlayer player() {
        return this.interaction.player();
    }

    @Override
    public BlockPos clickedPos() {
        return this.interaction.clickedPos();
    }

    @Override
    public BlockPos origin() {
        return this.interaction.origin();
    }

    @Override
    public UUID instanceId() {
        return this.interaction.instanceId();
    }

    @Override
    public MultiblockOrientation orientation() {
        return this.interaction.orientation();
    }

    @Override
    public MultiblockDefinition definition() {
        return this.interaction.definition();
    }

    @Override
    public MultiblockPartInteractionContext interaction() {
        return this.interaction;
    }

}