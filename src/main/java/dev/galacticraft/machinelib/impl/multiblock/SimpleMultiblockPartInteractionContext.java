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
public record SimpleMultiblockPartInteractionContext(FormedMultiblockMachine machine, ServerPlayer player,
                                                     BlockPos clickedPos, InteractionHand hand,
                                                     BlockHitResult hit) implements MultiblockPartInteractionContext {

    /**
     * Creates an interaction context for a formed multiblock part.
     *
     * @param machine    formed machine
     * @param player     interacting player
     * @param clickedPos clicked part position
     * @param hand       interaction hand
     * @param hit        block hit result
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