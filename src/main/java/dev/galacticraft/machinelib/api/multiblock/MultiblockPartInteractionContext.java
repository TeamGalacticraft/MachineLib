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

package dev.galacticraft.machinelib.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

import java.util.UUID;

/**
 * Context passed to a multiblock definition when a player interacts with one of
 * its formed parts.
 *
 * <p>This allows multiblocks made from arbitrary blocks, including vanilla
 * blocks with no block entity, to route interaction through MachineLib once the
 * structure is formed.</p>
 */
public interface MultiblockPartInteractionContext {

    /**
     * Gets the level containing the formed multiblock.
     *
     * @return server level
     */
    ServerLevel level();

    /**
     * Gets the player interacting with the multiblock part.
     *
     * @return server player
     */
    ServerPlayer player();

    /**
     * Gets the clicked world position.
     *
     * @return clicked block position
     */
    BlockPos clickedPos();

    /**
     * Gets the formed multiblock origin.
     *
     * @return multiblock origin
     */
    BlockPos origin();

    /**
     * Gets the formed multiblock instance id.
     *
     * @return instance id
     */
    UUID instanceId();

    /**
     * Gets the multiblock orientation.
     *
     * @return orientation
     */
    MultiblockOrientation orientation();

    /**
     * Gets the registered multiblock definition.
     *
     * @return definition
     */
    MultiblockDefinition definition();

    /**
     * Gets the hand used for the interaction.
     *
     * @return interaction hand
     */
    InteractionHand hand();

    /**
     * Gets the block hit result for the interaction.
     *
     * @return block hit result
     */
    BlockHitResult hit();

}