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

import java.util.Set;
import java.util.UUID;

/**
 * Context passed to runtime multiblock components.
 */
public interface MultiblockComponentContext {

    /**
     * Gets the server level containing the formed multiblock.
     *
     * @return server level
     */
    ServerLevel level();

    /**
     * Gets the formed multiblock origin.
     *
     * @return origin
     */
    BlockPos origin();

    /**
     * Gets the formed multiblock instance id.
     *
     * @return instance id
     */
    UUID instanceId();

    /**
     * Gets the formed multiblock orientation.
     *
     * @return orientation
     */
    MultiblockOrientation orientation();

    /**
     * Gets the multiblock definition.
     *
     * @return definition
     */
    MultiblockDefinition definition();

    /**
     * Gets all world positions occupied by the formed machine.
     *
     * @return immutable part position set
     */
    Set<BlockPos> partPositions();

    /**
     * Marks this formed machine's persistent component data as changed.
     *
     * <p>Components should call this whenever persistent state changes. The
     * multiblock manager will then write the component state into saved data.</p>
     */
    void setChanged();

    /**
     * Gets another component attached to the same formed machine.
     *
     * @param type component type
     * @return component, or {@code null}
     * @param <T> component type
     */
    <T extends MultiblockComponent> T component(Class<T> type);

}