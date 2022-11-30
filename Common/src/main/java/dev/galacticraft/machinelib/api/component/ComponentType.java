package dev.galacticraft.machinelib.api.component;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;

public interface ComponentType<A> {
    // direction from searching block -> searched block
    // e.g. hopper requests UP towards the block it is extracting from
    // position of the searching block
    A getInWorldFromBlock(Level level, BlockPos pos, Direction direction);

    // direction from searched block -> searching block
    // e.g. hopper requests DOWN to extract from above inventory
    // position of the block being searched
    A getInWorld(Level level, BlockPos pos, Direction direction);

    // direction from searched block -> searching block
    // e.g. hopper requests DOWN to extract from above inventory
    A getInWorld(BlockEntity blockEntity, Direction direction);

    A getFromItemReadOnly(ItemStack stack);
    @ApiStatus.Experimental
    A getFromItemMutable(ItemStack stack); //todo: is this safe?
    A getFromItem(BasicSlot slot);
}
