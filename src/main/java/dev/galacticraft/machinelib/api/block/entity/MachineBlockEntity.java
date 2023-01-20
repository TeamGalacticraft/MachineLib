/*
 * Copyright (c) 2021-2023 Team Galacticraft
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
import dev.galacticraft.machinelib.api.block.face.BlockFace;
import dev.galacticraft.machinelib.api.machine.*;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.ResourceStorage;
import dev.galacticraft.machinelib.api.storage.io.ResourceType;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
import dev.galacticraft.machinelib.api.transfer.cache.AdjacentBlockApiCache;
import dev.galacticraft.machinelib.api.transfer.exposed.ExposedStorage;
import dev.galacticraft.machinelib.api.util.GenericApiUtil;
import dev.galacticraft.machinelib.client.api.screen.MachineScreen;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.MachineLib;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

import java.util.Objects;

/**
 * A block entity that represents a machine.
 * <p>
 * This class handles 3 different types of storage and IO configurations:
 * {@link MachineEnergyStorage energy}, {@link MachineItemStorage item} and {@link MachineFluidStorage fluid} storage.
 *
 * @see MachineBlock
 * @see MachineMenu
 * @see MachineScreen
 */
public abstract class MachineBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, RenderAttachmentBlockEntity {
    /**
     * The configuration for this machine.
     * This is used to store the {@link #getRedstoneActivation() redstone activation}, {@link #getIOConfig() I/O configuration},
     * and {@link #getSecurity() security} settings for this machine.
     *
     * @see MachineConfiguration
     */
    private final MachineConfiguration configuration = MachineConfiguration.create();

    /**
     * The energy storage for this machine.
     *
     * @see #energyStorage()
     * @see #getEnergyCapacity()
     */
    private final @NotNull MachineEnergyStorage energyStorage = MachineEnergyStorage.of(this.getEnergyCapacity(), this.getEnergyInsertionRate(), this.getEnergyExtractionRate(), this.canExposedInsertEnergy(), this.canExposedExtractEnergy());

    /**
     * The item storage for this machine.
     *
     * @see #createItemStorage()
     * @see #itemStorage()
     */
    private final @NotNull MachineItemStorage itemStorage = this.createItemStorage();

    /**
     * The fluid storage for this machine.
     *
     * @see #createFluidStorage()
     * @see #fluidStorage()
     */
    private final @NotNull MachineFluidStorage fluidStorage = this.createFluidStorage();
    /**
     * The name of the machine, to be passed to the screen handler factory for display.
     * <p>
     * By default, this is the name of the block.
     */
    @ApiStatus.Internal
    private final @NotNull Component name;
    /**
     * Caches energy storages available from adjacent blocks.
     */
    @ApiStatus.Internal
    private @Nullable AdjacentBlockApiCache<EnergyStorage> energyCache = null;
    /**
     * Caches fluid storages available from adjacent blocks.
     */
    @ApiStatus.Internal
    private @Nullable AdjacentBlockApiCache<Storage<FluidVariant>> fluidCache = null;
    /**
     * Caches item storages available from adjacent blocks.
     */
    @ApiStatus.Internal
    private @Nullable AdjacentBlockApiCache<Storage<ItemVariant>> itemCache = null;
    /**
     * Whether the machine will not drop items when broken.
     * <p>
     * Used for machines that are placed in structures to prevent players from obtaining too many free resources.
     * Set via NBT.
     *
     * @see Constant.Nbt#DISABLE_DROPS
     */
    @ApiStatus.Internal
    private boolean disableDrops = false;

    /**
     * Constructs a new machine block entity with the name automatically derived from the passed {@link BlockState}.
     *
     * @param type  The type of block entity.
     * @param pos   The position of the machine in the level.
     * @param state The block state of the machine.
     * @see MachineBlockEntity#MachineBlockEntity(BlockEntityType, BlockPos, BlockState, Component)
     */
    protected MachineBlockEntity(@NotNull BlockEntityType<? extends MachineBlockEntity> type, @NotNull BlockPos pos, BlockState state) {
        this(type, pos, state, state.getBlock().getName().setStyle(Constant.Text.DARK_GRAY_STYLE));
    }

    /**
     * Constructs a new machine block entity.
     *
     * @param type  The type of block entity.
     * @param pos   The position of the machine in the level.
     * @param state The block state of the machine.
     * @param name  The name of the machine, to be passed to the screen handler.
     * @see #createItemStorage()
     * @see #createFluidStorage()
     */
    protected MachineBlockEntity(@NotNull BlockEntityType<? extends MachineBlockEntity> type, @NotNull BlockPos pos, BlockState state, @NotNull Component name) {
        super(type, pos, state);
        this.name = name;
    }

    /**
     * Registers the transfer handlers for this machine.
     * <p>
     * This needs to be called for every block entity type that extends this class.
     * Otherwise, in-world resource transfer will not work.
     *
     * @param blocks the blocks to register.
     */
    public static void registerComponents(@NotNull Block... blocks) {
        EnergyStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, context) -> {
            if (blockEntity != null) {
                return ((MachineBlockEntity) blockEntity).getExposedEnergyStorage(state, context);
            }
            return null;
        }, blocks);
        net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, context) -> {
            if (blockEntity != null) {
                return ((MachineBlockEntity) blockEntity).getExposedItemStorage(state, context);
            }
            return null;
        }, blocks);
        net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, context) -> {
            if (blockEntity != null) {
                return ((MachineBlockEntity) blockEntity).getExposedFluidStorage(state, context);
            }
            return null;
        }, blocks);
    }

    /**
     * The maximum amount of energy that this machine can hold.
     * <p>
     * This is called once during the construction of this machine and should return a constant value for each block entity type.
     *
     * @return Energy capacity of this machine.
     */
    @Contract(pure = true)
    public long getEnergyCapacity() {
        return 0;
    }

    /**
     * The maximum amount of energy that the machine can insert into items in its inventory (per transaction).
     *
     * @return The maximum amount of energy that the machine can insert into items in its inventory (per transaction).
     * @see #attemptDrainPowerToStack(SlotGroupType, int)
     * @see #getEnergyItemExtractionRate()
     */
    @Contract(pure = true)
    public long getEnergyItemInsertionRate() {
        return (long) (this.getEnergyCapacity() / 160.0);
    }

    /**
     * The maximum amount of energy that the machine can extract from items in its inventory (per transaction).
     *
     * @return The maximum amount of energy that the machine can extract from items in its inventory (per transaction).
     * @see #attemptChargeFromStack(SlotGroupType, int)
     * @see #getEnergyItemInsertionRate()
     */
    @Contract(pure = true)
    public long getEnergyItemExtractionRate() {
        return (long) (this.getEnergyCapacity() / 160.0);
    }

    /**
     * The maximum amount of energy that the machine can intake per transaction.
     * If adjacent machines should not be able to insert energy, return zero.
     * <p>
     * Should always be greater than {@link #getEnergyItemInsertionRate() the item insertion rate}.
     *
     * @return The maximum amount of energy that the machine can intake per transaction.
     */
    @Contract(pure = true)
    public long getEnergyInsertionRate() {
        return (long) (this.getEnergyCapacity() / 120.0);
    }

    /**
     * The maximum amount of energy that the machine can extract per transaction.
     * If adjacent machines should not be able to extract energy, return zero.
     * <p>
     * Should always be greater than {@link #getEnergyItemExtractionRate()} the item extraction rate}.
     *
     * @return The maximum amount of energy that the machine can eject per transaction.
     */
    @Contract(pure = true)
    public long getEnergyExtractionRate() {
        return (long) (this.getEnergyCapacity() / 120.0);
    }

    /**
     * Returns whether adjacent wires/machines can insert energy into this machine.
     *
     * @return whether adjacent wires/machines can insert energy into this machine.
     */
    @Contract(pure = true)
    public boolean canExposedInsertEnergy() {
        return false;
    }

    /**
     * Returns whether adjacent wires/machines can extract energy into this machine.
     *
     * @return whether adjacent wires/machines can extract energy into this machine.
     */
    @Contract(pure = true)
    public boolean canExposedExtractEnergy() {
        return false;
    }

    /**
     * Returns whether this machine is currently running.
     *
     * @return Whether this machine is currently running.
     * @see #getStatus() for the status of this machine.
     * @see MachineStatus.Type for the different types of statuses.
     */
    @Contract(pure = true)
    protected boolean isActive() {
        return this.getStatus().type().isActive();
    }

    /**
     * Creates an item storage for this machine.
     * <p>
     * This is called once during the construction of this machine and should return a constant value for each block entity type.
     *
     * @return An item storage configured for this machine.
     */
    @Contract(pure = true)
    protected @NotNull MachineItemStorage createItemStorage() {
        return MachineItemStorage.empty();
    }

    /**
     * Creates a fluid storage for this machine.
     * <p>
     * This is called once during the construction of this machine and should return a constant value for each block entity type.
     *
     * @return A fluid storage configured for this machine.
     */
    @Contract(pure = true)
    protected @NotNull MachineFluidStorage createFluidStorage() {
        return MachineFluidStorage.empty();
    }

    /**
     * Sets the redstone activation mode of this machine.
     *
     * @param redstone the redstone activation mode to use.
     * @see #getRedstoneActivation()
     */
    @Contract(mutates = "this")
    public void setRedstone(@NotNull RedstoneActivation redstone) {
        this.configuration.setRedstoneActivation(redstone);
    }

    /**
     * Returns the status of this machine. Machine status is calculated in {@link #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller)},
     * but may be modified manually by calling {@link #setStatus(MachineStatus)}.
     *
     * @return the status of this machine.
     * @see MachineConfiguration
     */
    public @NotNull MachineStatus getStatus() {
        return this.configuration.getStatus();
    }

    /**
     * Sets the status of this machine. It is recommended to use {@link #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller)} to
     * calculate the status of this machine, rather than setting it manually.
     *
     * @param status the status to set.
     * @see #getStatus()
     */
    public void setStatus(@NotNull MachineStatus status) {
        if (this.isActive() != status.type().isActive()) {
            if (this.level != null) {
                this.level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(MachineBlock.ACTIVE, status.type().isActive()));
            }
        }
        this.configuration.setStatus(status);
    }

    /**
     * Returns the energy storage of this machine.
     *
     * @return The energy storage of this machine.
     * @see MachineEnergyStorage
     */
    @Contract(pure = true)
    public final @NotNull MachineEnergyStorage energyStorage() {
        return this.energyStorage;
    }

    /**
     * Returns the item storage of this machine.
     *
     * @return The item storage of this machine.
     * @see MachineItemStorage
     */
    @Contract(pure = true)
    public final @NotNull MachineItemStorage itemStorage() {
        return this.itemStorage;
    }

    /**
     * Returns the fluid storage of this machine.
     *
     * @return the fluid storage of this machine.
     * @see MachineFluidStorage
     */
    @Contract(pure = true)
    public final @NotNull MachineFluidStorage fluidStorage() {
        return this.fluidStorage;
    }

    /**
     * Returns the security settings of this machine.
     * Used to determine who can interact with this machine.
     *
     * @return the security settings of this machine.
     * @see MachineConfiguration
     */
    @Contract(pure = true)
    public final @NotNull SecuritySettings getSecurity() {
        return this.configuration.getSecurity();
    }

    /**
     * Returns how the machine reacts when it interacts with redstone.
     * Dictates how this machine should react to redstone.
     *
     * @return how the machine reacts when it interacts with redstone.
     * @see RedstoneActivation
     * @see #setRedstone(RedstoneActivation)
     */
    @Contract(pure = true)
    public final @NotNull RedstoneActivation getRedstoneActivation() {
        return this.configuration.getRedstoneActivation();
    }

    /**
     * Returns the IO configuration of this machine.
     *
     * @return the IO configuration of this machine.
     */
    @Contract(pure = true)
    public final @NotNull MachineIOConfig getIOConfig() {
        return this.configuration.getIOConfiguration();
    }

    public @NotNull MachineConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * Returns whether this machine will drop items when broken.
     *
     * @return whether this machine will drop items when broken.
     */
    @Contract(pure = true)
    public boolean areDropsDisabled() {
        return this.disableDrops;
    }

    /**
     * Returns the relevant storage when given an explicit resource type.
     * Generic resource types like {@link ResourceType#ANY any} or {@link ResourceType#NONE} will return {@code null}
     *
     * @param type The type of resource to get a storage for.
     * @return the relevant storage.
     */
    @Contract(pure = true)
    public @Nullable ResourceStorage<?, ?, ?, ?> getResourceStorage(@NotNull ResourceType type) {
        return switch (type) {
            case ITEM -> this.itemStorage();
            case FLUID -> this.fluidStorage();
            default -> null;
        };
    }

    /**
     * Returns whether the current machine is enabled.
     *
     * @param world the world this machine is in.
     * @return whether the current machine is enabled.
     * @see RedstoneActivation
     * @see #getRedstoneActivation()
     */
    public boolean isDisabled(@NotNull Level world) {
        return switch (this.getRedstoneActivation()) {
            case LOW -> world.hasNeighborSignal(this.worldPosition);
            case HIGH -> !world.hasNeighborSignal(this.worldPosition);
            case IGNORE -> false;
        };
    }

    /**
     * Updates the machine every tick.
     *
     * @param world    the world.
     * @param pos      the position of this machine.
     * @param state    the block state of this machine.
     * @param profiler the world profiler.
     * @see #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller) for server-side logic that can be disabled (not called) arbitrarily.
     * @see #tickConstant(ServerLevel, BlockPos, BlockState, ProfilerFiller) for the server-side logic that is always called.
     * @see #tickClient(Level, BlockPos, BlockState) for the client-side logic.
     */
    public final void tickBase(@NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        this.setBlockState(state);
        if (!world.isClientSide()) {
            profiler.push("constant");
            ServerLevel serverWorld = (ServerLevel) world;
            this.tickConstant(serverWorld, pos, state, profiler);
            if (this.isDisabled(world)) {
                profiler.popPush("disabled");
                this.tickDisabled(serverWorld, pos, state, profiler);
            } else {
                profiler.popPush("active");
                this.setStatus(this.tick(serverWorld, pos, state, profiler));
            }
        } else {
            profiler.push("client");
            this.tickClient(world, pos, state);
        }
        profiler.pop();
    }

    /**
     * Called every tick, even if the machine is not active/powered.
     * Use this to tick fuel consumption or transfer resources, for example.
     *
     * @param world    the world.
     * @param pos      the position of this machine.
     * @param state    the block state of this machine.
     * @param profiler the world profiler.
     * @see #tickBase(Level, BlockPos, BlockState, ProfilerFiller)
     * @see #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller)
     */
    protected void tickConstant(@NotNull ServerLevel world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
    }

    /**
     * Called every tick, when the machine is explicitly disabled (by redstone, for example).
     * Use this to clean-up resources leaked by {@link #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller)}.
     *
     * @param world    the world.
     * @param pos      the position of this machine.
     * @param state    the block state of this machine.
     * @param profiler the world profiler.
     * @see #tickBase(Level, BlockPos, BlockState, ProfilerFiller)
     * @see #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller)
     */
    protected void tickDisabled(@NotNull ServerLevel world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
    }

    /**
     * Called every tick on the client.
     *
     * @param world the world.
     * @param pos   the position of this machine.
     * @param state the block state of this machine.
     * @see #tickBase(Level, BlockPos, BlockState, ProfilerFiller)
     */
    protected void tickClient(@NotNull /*Client*/Level world, @NotNull BlockPos pos, @NotNull BlockState state) {
    } //todo: client/server split? what is this used for?

    /**
     * Called every tick on the server, when the machine is active.
     * Use this to update crafting progress, for example.
     * Be sure to clean-up state in {@link #tickDisabled(ServerLevel, BlockPos, BlockState, ProfilerFiller)}.
     *
     * @param world    the world.
     * @param pos      the position of this machine.
     * @param state    the block state of this machine.
     * @param profiler the world profiler.
     * @return the status of this machine.
     * @see #tickDisabled(ServerLevel, BlockPos, BlockState, ProfilerFiller)
     * @see #tickConstant(ServerLevel, BlockPos, BlockState, ProfilerFiller)
     */
    protected abstract @NotNull MachineStatus tick(@NotNull ServerLevel world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler);

    /**
     * Returns a controlled/throttled energy storage to expose to adjacent blocks.
     *
     * @param direction the direction the adjacent block is in.
     * @return a controlled/throttled energy storage to expose to adjacent blocks.
     * @see #getExposedEnergyStorage(Direction, Direction)
     */
    @ApiStatus.Internal
    private @Nullable EnergyStorage getExposedEnergyStorage(@NotNull BlockState state, @NotNull Direction direction) {
        return this.getExposedEnergyStorage(state.getValue(BlockStateProperties.HORIZONTAL_FACING), direction);
    }

    /**
     * Returns a controlled/throttled energy storage to expose to adjacent blocks.
     *
     * @param facing    the direction this machine is facing.
     * @param direction the direction the adjacent block is in.
     * @return a controlled/throttled energy storage to expose to adjacent blocks.
     * @see #getExposedEnergyStorage(BlockFace)
     */
    @ApiStatus.Internal
    private @Nullable EnergyStorage getExposedEnergyStorage(@NotNull Direction facing, @NotNull Direction direction) {
        return this.getExposedEnergyStorage(BlockFace.toFace(facing, direction));
    }

    /**
     * Returns a controlled/throttled energy storage to expose to adjacent blocks.
     *
     * @param face the block face to get the exposed storages I/O configuration from.
     * @return a controlled/throttled energy storage to expose to adjacent blocks.
     */
    @ApiStatus.Internal
    private @Nullable EnergyStorage getExposedEnergyStorage(@NotNull BlockFace face) {
        return this.getIOConfig().get(face).getExposedEnergyStorage(this.energyStorage);
    }

    /**
     * Returns a controlled/throttled item storage to expose to adjacent blocks.
     *
     * @param direction the direction the adjacent block is in.
     * @return a controlled/throttled item storage to expose to adjacent blocks.
     * @see #getExposedItemStorage(Direction, Direction)
     */
    @ApiStatus.Internal
    private @Nullable ExposedStorage<Item, ItemVariant> getExposedItemStorage(@NotNull BlockState state, @NotNull Direction direction) {
        return this.getExposedItemStorage(state.getValue(BlockStateProperties.HORIZONTAL_FACING), direction);
    }

    /**
     * Returns a controlled/throttled item storage to expose to adjacent blocks.
     *
     * @param facing    the direction this machine is facing.
     * @param direction the direction the adjacent block is in.
     * @return a controlled/throttled item storage to expose to adjacent blocks.
     * @see #getExposedItemStorage(BlockFace)
     */
    @ApiStatus.Internal
    private @Nullable ExposedStorage<Item, ItemVariant> getExposedItemStorage(@NotNull Direction facing, @NotNull Direction direction) {
        return this.getExposedItemStorage(BlockFace.toFace(facing, direction));
    }

    /**
     * Returns a controlled/throttled item storage to expose to adjacent blocks.
     *
     * @param face the block face to get the exposed storages I/O configuration from.
     * @return a controlled/throttled item storage to expose to adjacent blocks.
     */
    @ApiStatus.Internal
    private @Nullable ExposedStorage<Item, ItemVariant> getExposedItemStorage(@NotNull BlockFace face) {
        return this.getIOConfig().get(face).getExposedItemStorage(this.itemStorage);
    }

    /**
     * Returns a controlled/throttled fluid storage to expose to adjacent blocks.
     *
     * @param direction the direction the adjacent block is in.
     * @return a controlled/throttled fluid storage to expose to adjacent blocks.
     * @see #getExposedFluidStorage(Direction, Direction)
     */
    @ApiStatus.Internal
    private @Nullable ExposedStorage<Fluid, FluidVariant> getExposedFluidStorage(@NotNull BlockState state, @NotNull Direction direction) {
        return this.getExposedFluidStorage(state.getValue(BlockStateProperties.HORIZONTAL_FACING), direction);
    }

    /**
     * Returns a controlled/throttled fluid storage to expose to adjacent blocks.
     *
     * @param facing    the direction this machine is facing.
     * @param direction the direction the adjacent block is in.
     * @return a controlled/throttled fluid storage to expose to adjacent blocks.
     * @see #getExposedFluidStorage(BlockFace)
     */
    @ApiStatus.Internal
    private @Nullable ExposedStorage<Fluid, FluidVariant> getExposedFluidStorage(@NotNull Direction facing, @NotNull Direction direction) {
        return this.getExposedFluidStorage(BlockFace.toFace(facing, direction));
    }

    /**
     * Returns a controlled/throttled fluid storage to expose to adjacent blocks.
     *
     * @param face the block face to get the exposed storages I/O configuration from.
     * @return a controlled/throttled fluid storage to expose to adjacent blocks.
     */
    @ApiStatus.Internal
    private @Nullable ExposedStorage<Fluid, FluidVariant> getExposedFluidStorage(@NotNull BlockFace face) {
        return this.getIOConfig().get(face).getExposedFluidStorage(this.fluidStorage);
    }

    /**
     * Serializes the machine's state to nbt.
     *
     * @param nbt the nbt to serialize to.
     */
    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.put(Constant.Nbt.ENERGY_STORAGE, this.energyStorage.createTag());
        nbt.put(Constant.Nbt.ITEM_STORAGE, this.itemStorage.createTag());
        nbt.put(Constant.Nbt.FLUID_STORAGE, this.fluidStorage.createTag());
        nbt.put(Constant.Nbt.CONFIGURATION, this.configuration.createTag());
        nbt.putBoolean(Constant.Nbt.DISABLE_DROPS, this.disableDrops);
    }

    /**
     * Deserializes the machine's state from nbt.
     *
     * @param nbt the nbt to deserialize from.
     */
    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        if (nbt.contains(Constant.Nbt.ENERGY_STORAGE, Tag.TAG_LONG))
            this.energyStorage.readTag(Objects.requireNonNull(((LongTag) nbt.get(Constant.Nbt.ENERGY_STORAGE))));
        if (nbt.contains(Constant.Nbt.ITEM_STORAGE, Tag.TAG_LIST))
            this.itemStorage.readTag(Objects.requireNonNull(nbt.getList(Constant.Nbt.ITEM_STORAGE, Tag.TAG_COMPOUND)));
        if (nbt.contains(Constant.Nbt.FLUID_STORAGE, Tag.TAG_LIST))
            this.fluidStorage.readTag(Objects.requireNonNull(nbt.getList(Constant.Nbt.FLUID_STORAGE, Tag.TAG_COMPOUND)));
        this.configuration.readTag(nbt);
        this.disableDrops = nbt.getBoolean(Constant.Nbt.DISABLE_DROPS);

        if (level != null && level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, Blocks.AIR.defaultBlockState(), this.getBlockState(), Block.UPDATE_IMMEDIATE);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();

        if (!(level instanceof ServerLevel serverLevel)) return;
        serverLevel.getChunkSource().blockChanged(worldPosition);
    }

    /**
     * Pushes energy from this machine to adjacent capacitor blocks.
     *
     * @param level the level.
     */
    protected void trySpreadEnergy(@NotNull ServerLevel level, @NotNull BlockState state) {
        if (this.energyCache == null) {
            this.energyCache = AdjacentBlockApiCache.create(EnergyStorage.SIDED, level, this.worldPosition);
        }
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        for (Direction direction : Constant.Cache.DIRECTIONS) {
            EnergyStorage storage = this.getExposedEnergyStorage(facing, direction);
            if (storage != null && storage.supportsExtraction()) {
                EnergyStorageUtil.move(storage, this.energyCache.find(direction), Long.MAX_VALUE, null);
            }
        }
    }

    /**
     * Pushes fluids from this machine to adjacent fluid storages.
     *
     * @param level the level.
     */
    protected void trySpreadFluids(@NotNull ServerLevel level, @NotNull BlockState state) {
        if (this.fluidCache == null) {
            this.fluidCache = AdjacentBlockApiCache.create(FluidStorage.SIDED, level, this.worldPosition);
        }
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        for (Direction direction : Constant.Cache.DIRECTIONS) {
            ExposedStorage<Fluid, FluidVariant> storage = this.getExposedFluidStorage(facing, direction);
            if (storage != null && storage.supportsExtraction()) {
                GenericApiUtil.moveAll(storage, this.fluidCache.find(direction), Long.MAX_VALUE, null); //TODO: fluid I/O cap
            }
        }
    }

    /**
     * Pushes items from this machine to adjacent item storages.
     *
     * @param level the level.
     */
    protected void trySpreadItems(@NotNull ServerLevel level, @NotNull BlockState state) {
        if (this.itemCache == null) {
            this.itemCache = AdjacentBlockApiCache.create(ItemStorage.SIDED, level, this.worldPosition);
        }
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        for (Direction direction : Constant.Cache.DIRECTIONS) {
            Storage<ItemVariant> storage = this.getExposedItemStorage(facing, direction);
            if (storage != null && storage.supportsExtraction()) {
                GenericApiUtil.moveAll(storage, this.itemCache.find(direction), Long.MAX_VALUE, null);
            }
        }
    }

    /**
     * Tries to charge this machine from the item in the given slot in this {@link #itemStorage()}.
     *
     * @param slot the slot to charge from.
     */
    protected void attemptChargeFromStack(SlotGroupType type, int slot) {
        if (this.energyStorage().isFull()) return;

        EnergyStorage energyStorage = this.itemStorage().getGroup(type).getSlot(slot).find(EnergyStorage.ITEM);
        if (energyStorage != null && energyStorage.supportsExtraction()) {
            try (Transaction transaction = Transaction.openOuter()) {
                EnergyStorageUtil.move(energyStorage, this.energyStorage, this.getEnergyItemExtractionRate(), transaction);
                transaction.commit();
            }
        }
    }

    /**
     * Tries to drain some of this machine's power into the item in the given slot in this {@link #itemStorage}.
     *
     * @param slot The slot id of the item
     */
    protected void attemptDrainPowerToStack(SlotGroupType type, int slot) {
        if (this.energyStorage().isEmpty()) return;
        EnergyStorage energyStorage = this.itemStorage().getGroup(type).getSlot(slot).find(EnergyStorage.ITEM);
        if (energyStorage != null && energyStorage.supportsInsertion()) {
            try (Transaction transaction = Transaction.openOuter()) {
                EnergyStorageUtil.move(this.energyStorage, energyStorage, this.getEnergyItemInsertionRate(), transaction);
                transaction.commit();
            }
        }
    }


    /**
     * Returns whether the given face can be configured for input or output.
     *
     * @param face the block face to test.
     * @return whether the given face can be configured for input or output.
     */
    public boolean isFaceLocked(@NotNull BlockFace face) { // todo: locked faces
        return false;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, @NotNull FriendlyByteBuf buf) {
        if (!this.getSecurity().hasAccess(player)) {
            MachineLib.LOGGER.error("Player {} has illegally accessed machine at {}", player.getStringUUID(), this.worldPosition);
        }
        buf.writeBlockPos(this.getBlockPos());
    }

    @Override
    public @NotNull Component getDisplayName() {
        return this.name;
    }

    @Override
    public Object getRenderAttachmentData() {
        return getIOConfig();
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
