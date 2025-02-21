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

import dev.galacticraft.machinelib.impl.attachment.MachineLibAttachmentTypes;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public class WireBlock extends Block {
    public static final BooleanProperty[] CONNECTIONS = Util.make(new BooleanProperty[6], p->{
        for (Direction value : Direction.values()) {
            p[value.get3DDataValue()] = BooleanProperty.create(value.getName());
        }
    });

    protected long capacity;

    public WireBlock(Properties properties, long capacity) {
        super(properties);
        this.capacity = capacity;

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(CONNECTIONS[0], false)
                .setValue(CONNECTIONS[1], false)
                .setValue(CONNECTIONS[2], false)
                .setValue(CONNECTIONS[3], false)
                .setValue(CONNECTIONS[4], false)
                .setValue(CONNECTIONS[5], false)
        );

        registerComponents();
    }

    protected void registerComponents() {
        EnergyStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, context) -> this.canConnect(world, pos, state, blockEntity, context) ? world.getAttachedOrCreate(MachineLibAttachmentTypes.WIRE_NETWORK_MANAGER).getStorage(world, pos) : null, this);
    }

    // direction outwards
    public boolean canConnect(Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity, Direction direction) {
        return true;
    }

//    // direction towards other
//    public boolean compatibleWith(Level level, BlockPos pos, BlockState state, @Nullable Direction direction, BlockPos otherPos, BlockState otherState) {
//        return otherState.getBlock() instanceof WireBlock wb && wb.capacity == this.capacity;
//    }

    // either state may not be this block
    protected boolean needsRefresh(BlockState previousState, BlockState newState) {
        return !previousState.is(newState.getBlock());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> compositeStateBuilder) {
        super.createBlockStateDefinition(compositeStateBuilder);
        compositeStateBuilder.add(CONNECTIONS);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborChanged(state, level, pos, sourceBlock, sourcePos, notify);

        Direction direction = Direction.fromDelta(sourcePos.getX() - pos.getX(), sourcePos.getY() - pos.getY(), sourcePos.getZ() - pos.getZ());
        assert direction != null;
        if (this.canConnect(level, pos, state, null, direction)) {
            boolean connect;
            BlockState adjState = level.getBlockState(sourcePos);
            if (adjState.getBlock() instanceof WireBlock wb) {
                connect = wb.canConnect(level, sourcePos, adjState, null, direction.getOpposite());
//                        && this.compatibleWith(level, pos, state, direction, sourcePos, adjState)
//                        && wb.compatibleWith(level, sourcePos, adjState, direction.getOpposite(), pos, state);
            } else {
                connect = EnergyStorage.SIDED.find(level, sourcePos, direction.getOpposite()) != null;
            }
            // check if the connection state needs an update
            if (state.getValue(CONNECTIONS[direction.get3DDataValue()]) != connect) {
                // connect if there is an energy storage to attach to
                level.setBlock(pos, state = state.setValue(CONNECTIONS[direction.get3DDataValue()], connect), 6);
                level.neighborChanged(sourcePos, state.getBlock(), pos); //inform other block that this wire has updated
            }
            if (!level.isClientSide) {
                WireNetworkManager manager = level.getAttachedOrCreate(MachineLibAttachmentTypes.WIRE_NETWORK_MANAGER);
                manager.updateWire((ServerLevel) level, pos, state, direction, sourcePos, adjState);
            }
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return calculateState(ctx.getLevel(), ctx.getClickedPos(), this.defaultBlockState());
    }

    private BlockState calculateState(Level level, BlockPos pos, BlockState state) {
        BlockPos.MutableBlockPos adjPos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            if (this.canConnect(level, pos, state, null, direction)) {
                adjPos.setWithOffset(pos, direction);

                BlockState blockState = level.getBlockState(adjPos);
                if ((blockState.getBlock() instanceof WireBlock wb && wb.canConnect(level, adjPos, blockState, null, direction.getOpposite()))
                        || EnergyStorage.SIDED.find(level, adjPos, blockState, null, direction.getOpposite()) != null) {
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
        super.onPlace(state, level, pos, oldState, notify);
        if (!level.isClientSide && this.needsRefresh(oldState, state)) {
            state = this.calculateState(level, pos, state);
            level.setBlock(pos, state, 0);
            level.getAttachedOrCreate(MachineLibAttachmentTypes.WIRE_NETWORK_MANAGER).newWire((ServerLevel) level, pos, state);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        super.onRemove(state, level, pos, newState, moved);
        if (!level.isClientSide && this.needsRefresh(state, newState)) {
            level.getAttachedOrCreate(MachineLibAttachmentTypes.WIRE_NETWORK_MANAGER).removeWire((ServerLevel) level, pos);
        }
    }
}
