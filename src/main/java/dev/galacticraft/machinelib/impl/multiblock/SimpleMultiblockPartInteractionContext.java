package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.MultiblockDefinition;
import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import dev.galacticraft.machinelib.api.multiblock.MultiblockPartInteractionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

import java.util.UUID;

/**
 * Default immutable implementation of {@link MultiblockPartInteractionContext}.
 */
public final class SimpleMultiblockPartInteractionContext implements MultiblockPartInteractionContext {

    private final FormedMultiblockMachine machine;
    private final ServerPlayer player;
    private final BlockPos clickedPos;
    private final InteractionHand hand;
    private final BlockHitResult hit;

    /**
     * Creates an interaction context for a formed multiblock part.
     *
     * @param machine formed machine
     * @param player interacting player
     * @param clickedPos clicked part position
     * @param hand interaction hand
     * @param hit block hit result
     */
    public SimpleMultiblockPartInteractionContext(
            final FormedMultiblockMachine machine,
            final ServerPlayer player,
            final BlockPos clickedPos,
            final InteractionHand hand,
            final BlockHitResult hit
    ) {
        this.machine = machine;
        this.player = player;
        this.clickedPos = clickedPos.immutable();
        this.hand = hand;
        this.hit = hit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServerLevel level() {
        return this.machine.level();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServerPlayer player() {
        return this.player;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BlockPos clickedPos() {
        return this.clickedPos;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BlockPos origin() {
        return this.machine.origin();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID instanceId() {
        return this.machine.instanceId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultiblockOrientation orientation() {
        return this.machine.orientation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultiblockDefinition definition() {
        return this.machine.definition();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractionHand hand() {
        return this.hand;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BlockHitResult hit() {
        return this.hit;
    }

}