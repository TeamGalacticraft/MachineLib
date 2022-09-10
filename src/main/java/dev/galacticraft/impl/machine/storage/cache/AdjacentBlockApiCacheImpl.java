/*
 * Copyright (c) 2021-2022 Team Galacticraft
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

package dev.galacticraft.impl.machine.storage.cache;

import dev.galacticraft.api.machine.storage.cache.AdjacentBlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AdjacentBlockApiCacheImpl<A> implements AdjacentBlockApiCache<A> {
    private static final Direction[] DIRECTIONS = Direction.values();

    private final BlockApiCache<A, Direction>[] caches = new BlockApiCache[6];
    private final BlockApiLookup<A, Direction> lookup;
    private final ServerLevel world;

    public AdjacentBlockApiCacheImpl(BlockApiLookup<A, Direction> lookup, ServerLevel world, BlockPos pos) {
        this.lookup = lookup;
        this.world = world;

        for (int i = 0; i < 6; i++) {
            this.caches[i] = BlockApiCache.create(lookup, world, pos.relative(DIRECTIONS[i]));
        }
    }

    @Override
    public @Nullable A find(@NotNull Direction direction, @Nullable BlockState state) {
        return this.caches[direction.ordinal()].find(state, direction.getOpposite());
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(@NotNull Direction direction) {
        return this.caches[direction.ordinal()].getBlockEntity();
    }

    @Override
    @Contract(pure = true)
    public BlockApiLookup<A, Direction> getLookup() {
        return this.lookup;
    }

    @Override
    @Contract(pure = true)
    public ServerLevel getWorld() {
        return this.world;
    }
}
