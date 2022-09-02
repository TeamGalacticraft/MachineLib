package dev.galacticraft.api.machine.storage.cache;

import dev.galacticraft.impl.machine.storage.cache.AdjacentBlockApiCacheImpl;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AdjacentBlockApiCache<A> {

    static <A> AdjacentBlockApiCache<A> create(BlockApiLookup<A, Direction> lookup, ServerLevel world, BlockPos pos) {
        return new AdjacentBlockApiCacheImpl<>(lookup, world, pos);
    }

    @Nullable
    default A find(Direction direction) {
        return this.find(direction, null);
    }

    /**
     * Attempt to retrieve an API from an adjacent block
     *
     * @param direction The direction to look for
     * @param state The block state at the target position, or null if unknown.
     * @return The retrieved API, or {@code null} if no API was found.
     */
    @Nullable
    A find(@NotNull Direction direction, @Nullable BlockState state);

    /**
     * Return the block entity at the target position of this lookup.
     *
     * <p>This is the most efficient way to query the block entity at the target position repeatedly:
     * unless the block entity has been loaded or unloaded since the last query, the result will be cached.
     */
    @Nullable
    BlockEntity getBlockEntity(@NotNull Direction direction);

    /**
     * Return the lookup t
     */
    @Contract(pure = true)
    BlockApiLookup<A, Direction> getLookup();

    /**
     * Return the world this cache is bound to.
     */
    @Contract(pure = true)
    ServerLevel getWorld();
}
