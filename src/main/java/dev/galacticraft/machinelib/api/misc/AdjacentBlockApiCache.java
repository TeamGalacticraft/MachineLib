/*
 * Copyright (c) 2021-2024 Team Galacticraft
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

package dev.galacticraft.machinelib.api.misc;

import dev.galacticraft.machinelib.impl.misc.AdjacentBlockApiCacheImpl;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Caches apis available in adjacent blocks.
 *
 * @param <Api> the api type
 */
public interface AdjacentBlockApiCache<Api> {
    @Contract("_, _, _ -> new")
    static <A> @NotNull AdjacentBlockApiCache<A> create(@NotNull BlockApiLookup<A, Direction> lookup, @NotNull ServerLevel world, @NotNull BlockPos pos) {
        return new AdjacentBlockApiCacheImpl<>(lookup, world, pos);
    }

    /**
     * Attempt to retrieve an API from an adjacent block.
     *
     * @param direction the direction to search in.
     * @return The retrieved API, or {@code null} if no API was found.
     */
    @Contract(mutates = "this")
    @Nullable
    default Api find(Direction direction) {
        return this.find(direction, null);
    }

    /**
     * Attempt to retrieve an API from an adjacent block.
     *
     * @param direction the direction to search in.
     * @param state     The block state at the target position, or null if unknown.
     * @return The retrieved API, or {@code null} if no API was found.
     */
    @Contract(mutates = "this")
    @Nullable
    Api find(@NotNull Direction direction, @Nullable BlockState state);

    /**
     * Returns the block entity in the given direction.
     * If the cache is invalid it will automatically be updated.
     * <p>
     * This is the most efficient way to query the block entity at the target position repeatedly:
     * unless the block entity has been loaded or unloaded since the last query, the result will be cached.
     *
     * @return the block entity in the given direction.
     */
    @Contract(mutates = "this")
    @Nullable
    BlockEntity getBlockEntity(@NotNull Direction direction);
}
