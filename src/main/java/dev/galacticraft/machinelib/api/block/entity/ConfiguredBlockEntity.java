/*
 * Copyright (c) 2021-2026 Team Galacticraft
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

package dev.galacticraft.machinelib.api.block.entity;

import dev.galacticraft.machinelib.api.block.MachineBlock;
import dev.galacticraft.machinelib.api.machine.MachineRenderData;
import dev.galacticraft.machinelib.api.machine.MachineState;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.configuration.*;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.network.s2c.BaseMachineUpdatePayload;
import dev.galacticraft.machinelib.impl.network.s2c.MachineStatusUpdatePayload;
import dev.galacticraft.machinelib.impl.network.s2c.SideConfigurationUpdatePayload;
import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class ConfiguredBlockEntity extends BaseBlockEntity implements RenderDataBlockEntity {
    private final @NotNull IOConfig configuration;
    private final @NotNull SecuritySettings security;
    /**
     * The {@link MachineState state} of this machine.
     * Stores the status and redstone power data
     *
     * @see #getState()
     */
    private final @NotNull MachineState state;
    private @NotNull RedstoneMode redstone;
    /**
     * Whether the machine is currently active/working.
     * This covers both working/state active and redstone activity control.
     *
     * @see #isActive()
     */
    private boolean active = false;

    /**
     * Constructs a new machine block entity.
     *
     * @param type The type of block entity.
     * @param pos The position of the machine in the level.
     * @param state The block state of the machine.
     */
    protected ConfiguredBlockEntity(BlockEntityType<? extends ConfiguredBlockEntity> type,
                                    BlockPos pos,
                                    BlockState state
    ) {
        super(type, pos, state);

        this.configuration = new IOConfig(generateIOFaces());
        this.security = new InternalSecuritySettings();
        this.state = new InternalMachineState();
        this.redstone = RedstoneMode.IGNORE;
    }

    /**
     * {@return the IO configuration of this machine}
     */
    @Contract(pure = true)
    public final @NotNull IOConfig getIOConfig() {
        return this.configuration;
    }

    /**
     * {@return the security settings of this machine} Used to determine who can interact with this machine.
     */
    @Contract(pure = true)
    public final @NotNull SecuritySettings getSecurity() {
        return this.security;
    }

    /**
     * {@return how the machine reacts when it interacts with redstone}
     */
    @Contract(pure = true)
    public final @NotNull RedstoneMode getRedstoneMode() {
        return this.redstone;
    }

    /**
     * Sets the redstone mode of this machine.
     *
     * @param redstone the redstone level level to use.
     * @see #getRedstoneMode()
     */
    @Contract(mutates = "this")
    public void setRedstoneMode(@NotNull RedstoneMode redstone) {
        this.redstone = redstone;
        this.setChanged();
    }

    /**
     * {@return the state of this machine}
     */
    public @NotNull MachineState getState() {
        return this.state;
    }

    /**
     * Updates the machine every tick.
     *
     * @param level the world.
     * @param pos the position of this machine.
     * @param state the block state of this machine.
     * @param profiler the world profiler.
     * @see #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller) for server-side logic that can be disabled (not called) arbitrarily.
     * @see #tickConstant(ServerLevel, BlockPos, BlockState, ProfilerFiller) for the server-side logic that is always called.
     */
    @Override
    public final void tickBase(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        this.setBlockState(state);
        profiler.push("constant");
        this.tickConstant(level, pos, state, profiler);
        profiler.pop();
        if (this.isDisabled()) {
            if (this.active) {
                this.updateActiveState(level, pos, state, this.active = false);
            }
            profiler.push("disabled");
            this.tickDisabled(level, pos, state, profiler);
            profiler.pop();
        } else {
            profiler.push("active");
            this.state.setStatus(this.tick(level, pos, state, profiler));
            profiler.pop();
            if (this.active != this.state.isActive()) {
                this.updateActiveState(level, pos, state, this.active = this.state.isActive());
            }
        }
    }

    /**
     * Called every tick, even if the machine is not active/powered.
     * Use this to tick fuel consumption or transfer resources, for example.
     *
     * @param level the world.
     * @param pos the position of this machine.
     * @param state the block state of this machine.
     * @param profiler the world profiler.
     * @see #tickBase(ServerLevel, BlockPos, BlockState, ProfilerFiller)
     * @see #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller)
     */
    protected void tickConstant(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
    }

    /**
     * Called every tick, when the machine is explicitly disabled (by redstone, for example).
     * Use this to clean-up resources leaked by {@link #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller)}.
     *
     * @param level the world.
     * @param pos the position of this machine.
     * @param state the block state of this machine.
     * @param profiler the world profiler.
     * @see #tickBase(ServerLevel, BlockPos, BlockState, ProfilerFiller)
     * @see #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller)
     */
    protected void tickDisabled(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
    }

    /**
     * Called every tick on the server, when the machine is active.
     * Use this to update crafting progress, for example.
     * Be sure to clean up state in {@link #tickDisabled(ServerLevel, BlockPos, BlockState, ProfilerFiller)}.
     *
     * @param level the world.
     * @param pos the position of this machine.
     * @param state the block state of this machine.
     * @param profiler the world profiler.
     * @return the status of this machine.
     * @see #tickDisabled(ServerLevel, BlockPos, BlockState, ProfilerFiller)
     * @see #tickConstant(ServerLevel, BlockPos, BlockState, ProfilerFiller)
     */
    protected abstract @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler);

    /**
     * Updates the active state of a machine block in the specified level at a given position.
     *
     * @param level The level in which the machine block exists.
     * @param pos The position of the machine block.
     * @param state The current state of the machine block.
     * @param b The new value for the active state.
     */
    protected void updateActiveState(Level level, BlockPos pos, BlockState state, boolean b) {
        if (state.getBlock() instanceof MachineBlock) {
            level.setBlock(pos, state.setValue(MachineBlock.ACTIVE, b), 2);
        }
    }

    /**
     * Returns whether the machine is currently active or not.
     * Not to be used while ticking.
     * A machine is active when its state is "working" AND redstone activity control allows activity.
     *
     * @return {@code true} if the machine is active, {@code false} otherwise.
     * @see #active
     */
    public boolean isActive() {
        return this.active;
    }

    /**
     * Sets whether the machine is currently active or not.
     *
     * @param active {@code true} if the machine is active, {@code false} otherwise.
     * @see #active
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * {@return whether the current machine is enabled}
     *
     * @see RedstoneMode
     */
    public boolean isDisabled() {
        return !this.redstone.isActive(this.state.isPowered());
    }

    /**
     * Serializes the machine's state to nbt.
     *
     * @param tag the nbt to serialize to.
     * @param lookup the registry lookup provider.
     */
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.saveAdditional(tag, lookup);
        tag.put(Constant.Nbt.CONFIGURATION, this.configuration.createTag());
        tag.put(Constant.Nbt.SECURITY, this.security.createTag());
        tag.put(Constant.Nbt.REDSTONE_MODE, this.redstone.createTag());
        tag.put(Constant.Nbt.STATE, this.state.createTag());
        tag.putBoolean(Constant.Nbt.ACTIVE, this.active);
    }

    /**
     * Deserializes the machine's state from nbt.
     *
     * @param tag the nbt to deserialize from.
     * @param lookup the registry lookup provider.
     */
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.loadAdditional(tag, lookup);
        if (tag.contains(Constant.Nbt.CONFIGURATION, Tag.TAG_BYTE_ARRAY))
            this.configuration.readTag(new ByteArrayTag(tag.getByteArray(Constant.Nbt.CONFIGURATION)));
        if (tag.contains(Constant.Nbt.SECURITY, Tag.TAG_COMPOUND))
            this.security.readTag(tag.getCompound(Constant.Nbt.SECURITY));
        if (tag.contains(Constant.Nbt.REDSTONE_MODE, Tag.TAG_BYTE))
            this.redstone = RedstoneMode.readTag(tag.get(Constant.Nbt.REDSTONE_MODE));
        if (tag.contains(Constant.Nbt.STATE, Tag.TAG_BYTE))
            this.state.readTag((ByteTag) tag.get(Constant.Nbt.STATE));
        if (tag.contains(Constant.Nbt.ACTIVE))
            this.active = tag.getBoolean(Constant.Nbt.ACTIVE);

        if (this.level != null && this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, Blocks.AIR.defaultBlockState(), this.getBlockState(), Block.UPDATE_IMMEDIATE);
        }
    }

    @Override
    public @NotNull MachineRenderData getRenderData() {
        return this.configuration;
    }

    @Override
    public @NotNull CustomPacketPayload createUpdatePayload() {
        return new BaseMachineUpdatePayload(this.worldPosition, this.configuration, this.active);
    }

    @Override
    public void populateUpdateTag(CompoundTag tag) {
        tag.put(Constant.Nbt.CONFIGURATION, this.configuration.createTag());
        tag.putBoolean(Constant.Nbt.ACTIVE, this.active);
    }

    private IOFace @NotNull [] generateIOFaces() {
        IOFace[] faces = new IOFace[6];
        for (int i = 0; i < faces.length; i++) {
            faces[i] = new InternalIOFace(i);
        }
        return faces;
    }

    /**
     * Subclass that tracks modifications to save the block entity.
     */
    private class InternalMachineState extends MachineState {
        @Override
        public void setStatus(@Nullable MachineStatus status) {
            if (this.status != status) {
                MachineStatus oldStatus = this.status;
                this.status = status;
                ConfiguredBlockEntity.this.setChanged();
                if (ConfiguredBlockEntity.this.level != null && !ConfiguredBlockEntity.this.level.isClientSide) {
                    ConfiguredBlockEntity.this.broadcastToPlayers(new MachineStatusUpdatePayload(ConfiguredBlockEntity.this.worldPosition, status, oldStatus));
                }
            }
        }

        @Override
        public void setPowered(boolean powered) {
            if (this.powered != powered) {
                this.powered = powered;
                ConfiguredBlockEntity.this.setChanged();
            }
        }
    }

    /**
     * Subclass that tracks modifications to save the block entity.
     */
    private class InternalSecuritySettings extends SecuritySettings {
        @Override
        public void tryUpdate(@NotNull UUID uuid) {
            if (this.owner == null) {
                this.owner = uuid;
                ConfiguredBlockEntity.this.setChanged();
            }
        }

        @Override
        public void setAccessLevel(@NotNull AccessLevel accessLevel) {
            if (this.accessLevel != accessLevel) {
                this.accessLevel = accessLevel;
                ConfiguredBlockEntity.this.setChanged();
            }
        }
    }

    /**
     * Subclass that tracks modifications to save and re-render the block entity.
     */
    private class InternalIOFace extends IOFace {
        private final BlockFace face;

        public InternalIOFace(int i) {
            super(ResourceType.NONE, ResourceFlow.BOTH);
            this.face = BlockFace.values()[i];
        }

        @Override
        public void setOption(@NotNull ResourceType type, @NotNull ResourceFlow flow) {
            if (this.type != type || this.flow != flow) {
                this.type = type;
                this.flow = flow;

                ConfiguredBlockEntity.this.level.neighborChanged(
                        ConfiguredBlockEntity.this.worldPosition.relative(face.toDirection(ConfiguredBlockEntity.this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING))),
                        ConfiguredBlockEntity.this.getBlockState().getBlock(),
                        ConfiguredBlockEntity.this.worldPosition
                );
                ConfiguredBlockEntity.this.setChanged();
                ConfiguredBlockEntity.this.requestRerender();
                if (ConfiguredBlockEntity.this.level != null && !ConfiguredBlockEntity.this.level.isClientSide) {
                    ConfiguredBlockEntity.this.broadcastToPlayers(new SideConfigurationUpdatePayload(ConfiguredBlockEntity.this.worldPosition, this.face, type, flow));
                }
            }
        }
    }
}
