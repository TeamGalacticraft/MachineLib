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

import java.util.UUID;

/**
 * Context passed to a multiblock menu factory when a formed multiblock opens a
 * menu.
 *
 * <p>This is similar to {@link MultiblockPartInteractionContext}, but is menu
 * specific and includes the menu sync id and player inventory through the
 * factory call instead of through this context object.</p>
 */
public interface MultiblockMenuContext {

    /**
     * Gets the level containing the formed multiblock.
     *
     * @return server level
     */
    ServerLevel level();

    /**
     * Gets the player opening the menu.
     *
     * @return server player
     */
    ServerPlayer player();

    /**
     * Gets the clicked world position.
     *
     * @return clicked part position
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
     * @return formed orientation
     */
    MultiblockOrientation orientation();

    /**
     * Gets the registered multiblock definition.
     *
     * @return multiblock definition
     */
    MultiblockDefinition definition();

    /**
     * Gets the original interaction context that caused the menu to open.
     *
     * @return interaction context
     */
    MultiblockPartInteractionContext interaction();

}