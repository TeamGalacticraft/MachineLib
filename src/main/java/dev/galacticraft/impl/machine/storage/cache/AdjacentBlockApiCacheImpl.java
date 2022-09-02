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
