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
import dev.galacticraft.machinelib.api.compat.transfer.ExposedStorage;
import dev.galacticraft.machinelib.api.machine.MachineState;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.machine.configuration.MachineConfiguration;
import dev.galacticraft.machinelib.api.machine.configuration.MachineIOConfig;
import dev.galacticraft.machinelib.api.machine.configuration.RedstoneActivation;
import dev.galacticraft.machinelib.api.machine.configuration.SecuritySettings;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.misc.AdjacentBlockApiCache;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.api.util.GenericApiUtil;
import dev.galacticraft.machinelib.client.api.render.MachineRenderData;
import dev.galacticraft.machinelib.client.api.screen.MachineScreen;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.MachineLib;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
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
import org.jetbrains.annotations.*;
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
    private final MachineType<? extends MachineBlockEntity, ? extends MachineMenu<? extends MachineBlockEntity>> type;
    /**
     * The configuration for this machine.
     * This is used to store the {@link #getRedstoneActivation() redstone activation}, {@link #getIOConfig() I/O configuration},
     * and {@link #getSecurity() security} settings for this machine.
     *
     * @see MachineConfiguration
     */
    private final MachineConfiguration configuration;

    private final MachineState state;

    /**
     * The energy storage for this machine.
     *
     * @see #energyStorage()
     */
    private final @NotNull MachineEnergyStorage energyStorage;

    /**
     * The item storage for this machine.
     *
     * @see #itemStorage()
     */
    private final @NotNull MachineItemStorage itemStorage;

    /**
     * The fluid storage for this machine.
     *
     * @see #fluidStorage()
     */
    private final @NotNull MachineFluidStorage fluidStorage;
    /**
     * The text of the machine, to be passed to the screen handler factory for display.
     * <p>
     * By default, this is the text of the block.
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

    private boolean active = false;

    /**
     * Constructs a new machine block entity with the text automatically derived from the passed {@link BlockState}.
     *
     * @param type  The type of block entity.
     * @param pos   The position of the machine in the level.
     * @param state The block state of the machine.
     * @see MachineBlockEntity#MachineBlockEntity(MachineType, BlockPos, BlockState, Component)
     */
    protected MachineBlockEntity(@NotNull MachineType<? extends MachineBlockEntity, ? extends MachineMenu<? extends MachineBlockEntity>> type, @NotNull BlockPos pos, BlockState state) {
        this(type, pos, state, state.getBlock().getName().setStyle(Constant.Text.DARK_GRAY_STYLE));
    }

    /**
     * Constructs a new machine block entity.
     *
     * @param type  The type of block entity.
     * @param pos   The position of the machine in the level.
     * @param state The block state of the machine.
     * @param name  The text of the machine, to be passed to the screen handler.
     */
    protected MachineBlockEntity(@NotNull MachineType<? extends MachineBlockEntity, ? extends MachineMenu<? extends MachineBlockEntity>> type, @NotNull BlockPos pos, BlockState state, @NotNull Component name) {
        super(type.getBlockEntityType(), pos, state);
        this.type = type;
        this.name = name;

        this.configuration = MachineConfiguration.create();
        this.state = MachineState.create(this.type);

        this.energyStorage = type.createEnergyStorage();
        this.energyStorage.setListener(this::setChanged);
        this.itemStorage = type.createItemStorage();
        this.itemStorage.setListener(this::setChanged);
        this.fluidStorage = type.createFluidStorage();
        this.fluidStorage.setListener(this::setChanged);
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
        ItemStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, context) -> {
            if (blockEntity != null) {
                return ((MachineBlockEntity) blockEntity).getExposedItemStorage(state, context);
            }
            return null;
        }, blocks);
        FluidStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, context) -> {
            if (blockEntity != null) {
                return ((MachineBlockEntity) blockEntity).getExposedFluidStorage(state, context);
            }
            return null;
        }, blocks);
    }

    public MachineType<? extends MachineBlockEntity, ? extends MachineMenu<? extends MachineBlockEntity>> getMachineType() {
        return this.type;
    }

    /**
     * The maximum amount of energy that the machine can insert into items in its inventory (per transaction).
     *
     * @return The maximum amount of energy that the machine can insert into items in its inventory (per transaction).
     * @see #drainPowerToStack(int)
     * @see #getEnergyItemExtractionRate()
     */
    @Contract(pure = true)
    public long getEnergyItemInsertionRate() {
        return (long) (this.energyStorage.getCapacity() / 160.0);
    }

    /**
     * The maximum amount of energy that the machine can extract from items in its inventory (per transaction).
     *
     * @return The maximum amount of energy that the machine can extract from items in its inventory (per transaction).
     * @see #chargeFromStack(int)
     * @see #getEnergyItemInsertionRate()
     */
    @Contract(pure = true)
    public long getEnergyItemExtractionRate() {
        return (long) (this.energyStorage.getCapacity() / 160.0);
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

    public @NotNull MachineState getState() {
        return this.state;
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
     * Returns whether the current machine is enabled.
     *
     * @return whether the current machine is enabled.
     * @see RedstoneActivation
     * @see #getRedstoneActivation()
     */
    public boolean isDisabled() {
        return !this.getRedstoneActivation().isActive(this.state.isPowered());
    }

    /**
     * Updates the machine every tick.
     *
     * @param level    the world.
     * @param pos      the position of this machine.
     * @param state    the block state of this machine.
     * @param profiler the world profiler.
     * @see #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller) for server-side logic that can be disabled (not called) arbitrarily.
     * @see #tickConstant(ServerLevel, BlockPos, BlockState, ProfilerFiller) for the server-side logic that is always called.
     * @see #tickClient(Level, BlockPos, BlockState) for the client-side logic.
     */
    public final void tickBase(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        this.setBlockState(state);
        if (!level.isClientSide()) {
            profiler.push("constant");
            ServerLevel serverLevel = (ServerLevel) level;
            this.tickConstant(serverLevel, pos, state, profiler);
            if (this.isDisabled()) {
                if (this.active) {
                    MachineBlock.updateActiveState(level, pos, state, this.active = false);
                }
                profiler.popPush("disabled");
                this.tickDisabled(serverLevel, pos, state, profiler);
            } else {
                profiler.popPush("active");
                this.state.setStatus(this.tick(serverLevel, pos, state, profiler));
                if (!this.active) {
                    if (this.state.isActive()) {
                        MachineBlock.updateActiveState(level, pos, state, this.active = true);
                    }
                } else {
                    if (!this.state.isActive()) {
                        MachineBlock.updateActiveState(level, pos, state, this.active = false);
                    }
                }
            }
        } else {
            profiler.push("client");
            this.tickClient(level, pos, state);
        }
        profiler.pop();
    }

    /**
     * Called every tick, even if the machine is not active/powered.
     * Use this to tick fuel consumption or transfer resources, for example.
     *
     * @param level    the world.
     * @param pos      the position of this machine.
     * @param state    the block state of this machine.
     * @param profiler the world profiler.
     * @see #tickBase(Level, BlockPos, BlockState, ProfilerFiller)
     * @see #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller)
     */
    protected void tickConstant(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
    }

    /**
     * Called every tick, when the machine is explicitly disabled (by redstone, for example).
     * Use this to clean-up resources leaked by {@link #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller)}.
     *
     * @param level    the world.
     * @param pos      the position of this machine.
     * @param state    the block state of this machine.
     * @param profiler the world profiler.
     * @see #tickBase(Level, BlockPos, BlockState, ProfilerFiller)
     * @see #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller)
     */
    protected void tickDisabled(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
    }

    /**
     * Called every tick on the client.
     *
     * @param level the world.
     * @param pos   the position of this machine.
     * @param state the block state of this machine.
     * @see #tickBase(Level, BlockPos, BlockState, ProfilerFiller)
     */
    protected void tickClient(@NotNull /*Client*/Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
    } //todo: client/server split? what is this used for?

    /**
     * Called every tick on the server, when the machine is active.
     * Use this to update crafting progress, for example.
     * Be sure to clean-up state in {@link #tickDisabled(ServerLevel, BlockPos, BlockState, ProfilerFiller)}.
     *
     * @param level    the world.
     * @param pos      the position of this machine.
     * @param state    the block state of this machine.
     * @param profiler the world profiler.
     * @return the status of this machine.
     * @see #tickDisabled(ServerLevel, BlockPos, BlockState, ProfilerFiller)
     * @see #tickConstant(ServerLevel, BlockPos, BlockState, ProfilerFiller)
     */
    protected abstract @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler);

    /**
     * Not to be used while ticking
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Returns a controlled/throttled energy storage to expose to adjacent blocks.
     *
     * @param direction the direction the adjacent block is in.
     * @return a controlled/throttled energy storage to expose to adjacent blocks.
     * @see #getExposedEnergyStorage(Direction, Direction)
     */
    @ApiStatus.Internal
    private @Nullable EnergyStorage getExposedEnergyStorage(@NotNull BlockState state, @Nullable Direction direction) {
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
    private @Nullable EnergyStorage getExposedEnergyStorage(@NotNull Direction facing, @Nullable Direction direction) {
        return this.getExposedEnergyStorage(BlockFace.toFace(facing, direction));
    }

    /**
     * Returns a controlled/throttled energy storage to expose to adjacent blocks.
     *
     * @param face the block face to get the exposed storages I/O configuration from.
     * @return a controlled/throttled energy storage to expose to adjacent blocks.
     */
    @ApiStatus.Internal
    private @Nullable EnergyStorage getExposedEnergyStorage(@Nullable BlockFace face) {
        if (face == null) return this.energyStorage;
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
    private @Nullable ExposedStorage<Item, ItemVariant> getExposedItemStorage(@NotNull BlockState state, @Nullable Direction direction) {
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
    private @Nullable ExposedStorage<Item, ItemVariant> getExposedItemStorage(@NotNull Direction facing, @Nullable Direction direction) {
        return this.getExposedItemStorage(BlockFace.toFace(facing, direction));
    }

    /**
     * Returns a controlled/throttled item storage to expose to adjacent blocks.
     *
     * @param face the block face to get the exposed storages I/O configuration from.
     * @return a controlled/throttled item storage to expose to adjacent blocks.
     */
    @ApiStatus.Internal
    private @Nullable ExposedStorage<Item, ItemVariant> getExposedItemStorage(@Nullable BlockFace face) {
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
    private @Nullable ExposedStorage<Fluid, FluidVariant> getExposedFluidStorage(@NotNull BlockState state, @Nullable Direction direction) {
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
    private @Nullable ExposedStorage<Fluid, FluidVariant> getExposedFluidStorage(@NotNull Direction facing, @Nullable Direction direction) {
        return this.getExposedFluidStorage(BlockFace.toFace(facing, direction));
    }

    /**
     * Returns a controlled/throttled fluid storage to expose to adjacent blocks.
     *
     * @param face the block face to get the exposed storages I/O configuration from.
     * @return a controlled/throttled fluid storage to expose to adjacent blocks.
     */
    @ApiStatus.Internal
    private @Nullable ExposedStorage<Fluid, FluidVariant> getExposedFluidStorage(@Nullable BlockFace face) {
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
        nbt.put(Constant.Nbt.STATE, this.state.createTag());
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
        if (nbt.contains(Constant.Nbt.CONFIGURATION, Tag.TAG_COMPOUND))
            this.configuration.readTag(nbt.getCompound(Constant.Nbt.CONFIGURATION));
        if (nbt.contains(Constant.Nbt.STATE, Tag.TAG_COMPOUND))
            this.state.readTag(nbt.getCompound(Constant.Nbt.CONFIGURATION));
        if (nbt.contains(Constant.Nbt.ENERGY_STORAGE, Tag.TAG_LONG))
            this.energyStorage.readTag(Objects.requireNonNull(((LongTag) nbt.get(Constant.Nbt.ENERGY_STORAGE))));
        if (nbt.contains(Constant.Nbt.ITEM_STORAGE, Tag.TAG_LIST))
            this.itemStorage.readTag(Objects.requireNonNull(nbt.getList(Constant.Nbt.ITEM_STORAGE, Tag.TAG_LIST)));
        if (nbt.contains(Constant.Nbt.FLUID_STORAGE, Tag.TAG_LIST))
            this.fluidStorage.readTag(Objects.requireNonNull(nbt.getList(Constant.Nbt.FLUID_STORAGE, Tag.TAG_LIST)));
        this.disableDrops = nbt.getBoolean(Constant.Nbt.DISABLE_DROPS);

        if (level != null && level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, Blocks.AIR.defaultBlockState(), this.getBlockState(), Block.UPDATE_IMMEDIATE);
        }
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
     */
    protected void chargeFromStack(int slot) {
        if (this.energyStorage().isFull()) return;

        EnergyStorage energyStorage = this.itemStorage.getSlot(slot).find(EnergyStorage.ITEM);
        if (energyStorage != null && energyStorage.supportsExtraction()) {
            EnergyStorageUtil.move(energyStorage, this.energyStorage, this.getEnergyItemExtractionRate(), null);
        }
    }

    /**
     * Tries to drain some of this machine's power into the item in the given slot in this {@link #itemStorage}.
     */
    protected void drainPowerToStack(int slot) {
        if (this.energyStorage().isEmpty()) return;
        EnergyStorage energyStorage = this.itemStorage.getSlot(slot).find(EnergyStorage.ITEM);
        if (energyStorage != null && energyStorage.supportsInsertion()) {
            EnergyStorageUtil.move(this.energyStorage, energyStorage, this.getEnergyItemInsertionRate(), null);
        }
    }

    protected void takeFluidFromStack(int inputSlot, int tankSlot, Fluid fluid) {
        FluidResourceSlot tank = this.fluidStorage().getSlot(tankSlot);
        if (tank.isFull()) return;
        ItemResourceSlot slot = this.itemStorage.getSlot(inputSlot);
        Storage<FluidVariant> storage = slot.find(FluidStorage.ITEM);
        if (storage != null && storage.supportsExtraction()) {
            GenericApiUtil.move(FluidVariant.of(fluid), storage, tank, Integer.MAX_VALUE, null);
        }
    }

    protected void insertFluidToStack(int inputSlot, int tankSlot, Fluid fluid) {
        FluidResourceSlot tank = this.fluidStorage().getSlot(tankSlot);
        if (tank.isEmpty()) return;

        ItemResourceSlot slot = this.itemStorage.getSlot(inputSlot);
        Storage<FluidVariant> storage = slot.find(FluidStorage.ITEM);
        if (storage != null && storage.supportsInsertion()) {
            GenericApiUtil.move(FluidVariant.of(fluid), tank, storage, Integer.MAX_VALUE, null);
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
        this.configuration.writePacket(buf);
        this.state.writePacket(buf);
        this.energyStorage.writePacket(buf);
        this.itemStorage.writePacket(buf);
        this.fluidStorage.writePacket(buf);
    }

    @Override
    public @NotNull BlockEntityType<? extends MachineBlockEntity> getType() {
        return (BlockEntityType<? extends MachineBlockEntity>) super.getType();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return this.name;
    }

    @Override
    public @NotNull MachineRenderData getRenderAttachmentData() {
        return this.getIOConfig();
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.put(Constant.Nbt.CONFIGURATION, this.configuration.createTag());
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBlockPos(this.getBlockPos());
        this.writeRenderData(buf);
        System.out.println("write update packet");
        return ServerPlayNetworking.createS2CPacket(Constant.id("be_render_data"), buf);
    }

    @MustBeInvokedByOverriders
    public void readRenderData(FriendlyByteBuf buf) {
        System.out.println("READ");
        this.configuration.getIOConfiguration().readPacket(buf);

    }

    @MustBeInvokedByOverriders
    public void writeRenderData(FriendlyByteBuf buf) {
        System.out.println("WRITE");
        this.configuration.getIOConfiguration().writePacket(buf);
    }

    public void setRedstoneState(boolean b) {
        this.state.setPowered(b);
    }

}
