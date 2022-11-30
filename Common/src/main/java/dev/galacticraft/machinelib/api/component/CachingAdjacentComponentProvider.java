package dev.galacticraft.machinelib.api.component;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface CachingAdjacentComponentProvider<A> {
    default A get(Direction direction) {
        return this.get(direction, null);
    }

    A get(Direction direction, @Nullable BlockState state);

    void invalidate(Direction direction);

    BlockEntity getBlockEntity(Direction direction);
}
