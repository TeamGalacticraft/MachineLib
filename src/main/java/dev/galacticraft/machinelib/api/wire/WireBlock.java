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

package dev.galacticraft.machinelib.api.wire;

import dev.galacticraft.machinelib.impl.attachment.AttachmentTypes;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.Objects;

public abstract class WireBlock extends Block {
    public static final BooleanProperty[] CONNECTIONS = Util.make(new BooleanProperty[6], p->{
        for (Direction value : Direction.values()) {
            p[value.get3DDataValue()] = BooleanProperty.create(value.getName());
        }
    });

    public WireBlock(Properties properties) {
        super(properties);
        registerComponents();
    }

    private void registerComponents() {
        EnergyStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, context) -> {
            WireNetworkManager manager = world.getAttachedOrCreate(AttachmentTypes.WIRE_NETWORK_MANAGER);
            WireSegment segment = manager.getSegment(pos);
            return segment != null ? segment.storage(manager, world, context) : null;
        }, this);
    }

    public abstract long getCapacity();
    // direction outwards
    public abstract boolean canConnect(Level level, BlockPos pos, @Nullable BlockState state, Direction direction);
//    public abstract boolean compatibleWith(Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity, @Nullable Direction direction);

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> compositeStateBuilder) {
        super.createBlockStateDefinition(compositeStateBuilder);
        compositeStateBuilder.add(CONNECTIONS);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        System.out.println("neigh");
        super.neighborChanged(state, level, pos, sourceBlock, sourcePos, notify);

        Direction direction = Direction.fromDelta(sourcePos.getX() - pos.getX(), sourcePos.getY() - pos.getY(), sourcePos.getZ() - pos.getZ());
        assert direction != null;
        if (this.canConnect(level, pos, state, direction)) {
            boolean canConnect;
            BlockState adjState = level.getBlockState(sourcePos);
            if (adjState.getBlock() instanceof WireBlock wb) {
                canConnect = wb.canConnect(level, sourcePos, adjState, direction.getOpposite());
            } else {
                canConnect = EnergyStorage.SIDED.find(level, sourcePos, direction.getOpposite()) != null;
            }
            // check if the connection state needs an update
            if (state.getValue(CONNECTIONS[direction.get3DDataValue()]) != canConnect) {
                // connect if there is an energy storage to attach to
                level.setBlock(pos, state = state.setValue(CONNECTIONS[direction.get3DDataValue()], canConnect), 6);
                level.neighborChanged(sourcePos, state.getBlock(), pos); //inform other block that this wire has updated
            }
            if (!level.isClientSide) {
                WireNetworkManager manager = level.getAttachedOrCreate(AttachmentTypes.WIRE_NETWORK_MANAGER);
                manager.updateWire(level, pos, state, direction, sourcePos, adjState);
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            System.out.println(level.getAttachedOrCreate(AttachmentTypes.WIRE_NETWORK_MANAGER));
            player.sendSystemMessage(Component.literal(Objects.toString(level.getAttachedOrCreate(AttachmentTypes.WIRE_NETWORK_MANAGER).getSegment(pos))));
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        System.out.println("state" + ctx.getLevel());
        BlockState state = super.getStateForPlacement(ctx);
        assert state != null;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            if (this.canConnect(ctx.getLevel(), ctx.getClickedPos(), state, direction)) {
                pos.setWithOffset(ctx.getClickedPos(), direction);

                BlockState blockState = ctx.getLevel().getBlockState(pos);
                if ((blockState.getBlock() instanceof WireBlock wb && wb.canConnect(ctx.getLevel(), pos, blockState, direction.getOpposite()))
                        || EnergyStorage.SIDED.find(ctx.getLevel(), pos, blockState, null, direction.getOpposite()) != null) {
                    state = state.setValue(CONNECTIONS[direction.get3DDataValue()], true);
                } else {
                    state = state.setValue(CONNECTIONS[direction.get3DDataValue()], false);
                }
            }
        }
        return state;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean notify) {
        System.out.println("place" + level);
        super.onPlace(state, level, pos, oldState, notify);
        if (!level.isClientSide && oldState.getBlock() != state.getBlock()) level.getAttachedOrCreate(AttachmentTypes.WIRE_NETWORK_MANAGER).newWire(level, pos, state);
    }
}
