package dev.galacticraft.machinelib.impl.fabric.component;

import dev.galacticraft.machinelib.api.component.ComponentType;
import dev.galacticraft.machinelib.api.component.ItemContext;
import dev.galacticraft.machinelib.api.component.ItemContexts;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FabricComponentType<S, A> implements ComponentType<A> {
    private final BlockApiLookup<S, Direction> blockLookup;
    private final ItemApiLookup<S, ContainerItemContext> itemLookup;
    private final PlatformConversion<A, S> conversion;

    public FabricComponentType(BlockApiLookup<S, Direction> blockLookup, ItemApiLookup<S, ContainerItemContext> itemLookup, PlatformConversion<A, S> conversion) {
        this.blockLookup = blockLookup;
        this.itemLookup = itemLookup;
        this.conversion = conversion;
    }

    @Override
    public A getInWorldFromBlock(Level level, BlockPos pos, Direction direction) {
        return this.getInWorld(level, pos.relative(direction), direction.getOpposite());
    }

    @Override
    public A getInWorld(Level level, BlockPos pos, Direction direction) {
        return this.conversion.convert(this.blockLookup.find(level, pos, direction));
    }

    @Override
    public A getInWorld(BlockEntity blockEntity, Direction direction) {
        return this.conversion.convert(this.blockLookup.find(blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity, direction));
    }

    @Override
    public A getFromItemReadOnly(ItemStack stack) {
        return this.conversion.convert(ContainerItemContext.withInitial(stack).find(this.itemLookup));
    }

    @Override
    public A getFromItem(ItemContext slot) {
        ContainerItemContext ctx = ItemContexts.toPlatform(slot);
        return this.conversion.convert(ctx.find(this.itemLookup));
    }

    public interface PlatformConversion<A, S> {
        A convert(S storage);
    }
}
