/*
 * Copyright (c) 2021-2022 Team Galacticraft
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

package dev.galacticraft.api.block.entity;

import dev.galacticraft.api.block.ConfiguredMachineFace;
import dev.galacticraft.api.block.MachineBlock;
import dev.galacticraft.api.block.util.BlockFace;
import dev.galacticraft.api.machine.*;
import dev.galacticraft.api.machine.storage.MachineEnergyStorage;
import dev.galacticraft.api.machine.storage.MachineFluidStorage;
import dev.galacticraft.api.machine.storage.MachineItemStorage;
import dev.galacticraft.api.machine.storage.io.ConfiguredStorage;
import dev.galacticraft.api.machine.storage.io.ExposedStorage;
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.machine.storage.io.ResourceType;
import dev.galacticraft.api.transfer.GenericStorageUtil;
import dev.galacticraft.api.transfer.StateCachingStorageProvider;
import dev.galacticraft.impl.MLConstant;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
 * A block entity that represents a machine.>
 * This class handles the different types of storage and IO configurations.
 *
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 * @see MachineBlock
 * @see dev.galacticraft.api.screen.MachineScreenHandler
 * @see dev.galacticraft.api.client.screen.MachineHandledScreen
 */
public abstract class MachineBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, StorageProvider, RenderAttachmentBlockEntity {
    /**
     * Array of directions, to avoid reallocating it every tick.
     */
    private static final Direction[] DIRECTIONS = Direction.values();

    /**
     * The configuration for this machine.
     * This is used to store the redstone activation, I/O configuration, and security settings for this machine.
     *
     * @see #getRedstoneActivation() for the redstone activation configuration.
     * @see #getIOConfig() for the IO configuration.
     * @see #getSecurity() for the security configuration.
     */
    private final MachineConfiguration configuration = MachineConfiguration.create();

    /**
     * The energy storage for this machine.
     * This is used to store the energy storage for this machine.
     *
     * @see #energyStorage() for the energy storage.
     */
    private final @NotNull MachineEnergyStorage energyStorage = MachineEnergyStorage.of(this.getEnergyCapacity(), this.getEnergyInsertionRate(), this.getEnergyExtractionRate());

    /**
     * The item storage for this machine.
     *
     * @see #createItemStorage() to modify the item storage's settings.
     * @see #itemStorage() for the item storage.
     */
    private final @NotNull MachineItemStorage itemStorage = this.createItemStorage();

    /**
     * The fluid storage for this machine.
     *
     * @see #createFluidStorage() () to modify the item storage's settings.
     * @see #fluidStorage()
     */
    private final @NotNull MachineFluidStorage fluidStorage = this.createFluidStorage();

    /**
     * Whether the machine will not drop items when broken.
     * Used for machines that are placed in structures to prevent players from obtaining too many free resources.
     */
    @ApiStatus.Internal
    private boolean disableDrops = false;

    /**
     * The name of the block entity, derived from the name of the containing block.
     * Passed to the screen handler factory as the name of the machine.
     */
    @ApiStatus.Internal
    private final @NotNull Component name;

    /**
     * Creates a new machine block entity.
     *
     * @param type The type of block entity.
     * @param pos The position of this machine.
     * @param state The block state of this machine.
     */
    protected MachineBlockEntity(@NotNull BlockEntityType<? extends MachineBlockEntity> type, @NotNull BlockPos pos, BlockState state) {
        this(type, pos, state, state.getBlock().getName().setStyle(MLConstant.Text.DARK_GRAY_STYLE));
    }

    protected MachineBlockEntity(@NotNull BlockEntityType<? extends MachineBlockEntity> type, @NotNull BlockPos pos, BlockState state, @NotNull Component name) {
        super(type, pos, state);
        this.name = name;
    }

    /**
     * Registers the transfer handlers for this machine.
     * This needs to be called for every block entity type that extends this class.
     * Otherwise, in-world resource transfer will not work.
     *
     * @param type the block entity type to register.
     */
    public static void registerComponents(@NotNull BlockEntityType<? extends MachineBlockEntity> type) {
        EnergyStorage.SIDED.registerForBlockEntity(MachineBlockEntity::getExposedEnergyStorage, type);
        ItemStorage.SIDED.registerForBlockEntity(MachineBlockEntity::getExposedItemStorage, type);
        FluidStorage.SIDED.registerForBlockEntity(MachineBlockEntity::getExposedFluidInv, type);
    }

    /**
     * The maximum amount of energy that this machine can hold.
     * This is called once during the construction of this machine.
     * This method should not return different values based on state, as it may cause the capacitor to be (de)serialized incorrectly.
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
     * @see #attemptDrainPowerToStack(int) for the actual charging logic.
     * @return The maximum amount of energy that the machine can insert into items in its inventory (per transaction).
     */
    @Contract(pure = true)
    public long getEnergyItemInsertionRate() {
        return (long)(this.getEnergyCapacity() / 160.0);
    }

    /**
     * The maximum amount of energy that the machine can extract from items in its inventory (per transaction).
     *
     * @see #attemptChargeFromStack(int) for the actual charging logic.
     * @return The maximum amount of energy that the machine can extract from items in its inventory (per transaction).
     */
    @Contract(pure = true)
    public long getEnergyItemExtractionRate() {
        return (long)(this.getEnergyCapacity() / 160.0);
    }

    /**
     * The maximum amount of energy that the machine can intake per transaction.
     * If adjacent machines should not be able to insert energy, return zero.
     *
     * @return The maximum amount of energy that the machine can intake per transaction.
     */
    @Contract(pure = true)
    public long getEnergyInsertionRate() {
        return (long)(this.getEnergyCapacity() / 120.0);
    }

    /**
     * The maximum amount of energy that the machine can extract per transaction.
     * If adjacent machines should not be able to extract energy, return zero.
     *
     * @return The maximum amount of energy that the machine can eject per transaction.
     */
    @Contract(pure = true)
    public long getEnergyExtractionRate() {
        return (long)(this.getEnergyCapacity() / 120.0);
    }

    /**
     * Returns whether this machine is currently running.
     *
     * @see #getStatus() for the status of this machine.
     * @see MachineStatus.Type for the different types of statuses.
     * @return Whether this machine is currently running.
     */
    @Contract(pure = true)
    protected boolean isStatusActive() {
        return this.getStatus().type().isActive();
    }

    /**
     * Creates an item storage for this machine.
     * This is called once during the construction of this machine.
     * This method should not return different values based on state, as it may cause the inventory to be (de)serialized incorrectly.
     *
     * @return An item storage configured for this machine.
     */
    protected @NotNull MachineItemStorage createItemStorage() {
        return MachineItemStorage.empty();
    }

    /**
     * Creates a fluid storage for this machine.
     * This is called once during the construction of this machine.
     * This method should not return different values based on state, as it may cause the fluid storage to be (de)serialized incorrectly.
     *
     * @return The fluid storage configured for this machine.
     */
    protected @NotNull MachineFluidStorage createFluidStorage() {
        return MachineFluidStorage.empty();
    }

    /**
     * Sets the redstone activation mode of this machine.
     *
     * @param redstone the redstone activation mode to use.
     * @see #getRedstoneActivation() for the current redstone activation mode.
     */
    public void setRedstone(@NotNull RedstoneActivation redstone) {
        this.configuration.setRedstoneActivation(redstone);
    }

    /**
     * Returns the status of this machine. Machine status is calculated in {@link #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller)},
     * but may be modified manually by calling {@link #setStatus(MachineStatus)}.
     *
     * @see #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller) to calculate the status of this machine.
     * @see #setStatus(MachineStatus) to manually change the status of this machine.
     * @return the status of this machine.
     */
    public @NotNull MachineStatus getStatus() {
        return this.configuration.getStatus();
    }

    /**
     * Sets the status of this machine. It is recommended to use {@link #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller)} to
     * calculate the status of this machine, rather than setting it manually.
     *
     * @param status the status to set.
     */
    public void setStatus(@NotNull MachineStatus status) {
        if (this.isStatusActive() != status.type().isActive()) {
            if (this.level != null) {
                this.level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(MachineBlock.ACTIVE, status.type().isActive()));
            }
        }
        this.configuration.setStatus(status);
    }

    /**
     * Returns the energy storage of this machine.
     *
     * @see #getEnergyCapacity() for the maximum amount of energy that this machine can hold.
     * @return The energy storage of this machine.
     */
    public final @NotNull MachineEnergyStorage energyStorage() {
        return this.energyStorage;
    }

    /**
     * Returns the item storage of this machine.
     *
     * @see #createItemStorage() for the properties of the item storage.
     * @return The item storage of this machine.
     */
    public final @NotNull MachineItemStorage itemStorage() {
        return this.itemStorage;
    }

    /**
     * Returns the fluid storage of this machine.
     *
     * @see #createFluidStorage() for the properties of the fluid storage.
     * @return the fluid storage of this machine.
     */
    public final @NotNull MachineFluidStorage fluidStorage() {
        return this.fluidStorage;
    }

    /**
     * Returns the security settings of this machine.
     * Used to determine who can interact with this machine.
     *
     * @return the security settings of this machine.
     */
    public final @NotNull SecuritySettings getSecurity() {
        return this.configuration.getSecurity();
    }

    /**
     * Returns the redstone activation of this machine.
     * Dictates how this machine should react to redstone.
     *
     * @see #setRedstone(RedstoneActivation) to change the redstone activation of this machine.
     * @return the redstone configuration of this machine.
     */
    public final @NotNull RedstoneActivation getRedstoneActivation() {
        return this.configuration.getRedstoneActivation();
    }

    /**
     * Returns the IO configuration of this machine.
     * @return the IO configuration of this machine.
     */
    public final @NotNull MachineIOConfig getIOConfig() {
        return this.configuration.getIOConfiguration();
    }

    /**
     * Returns whether this machine will drop items when broken.
     * @return whether this machine will drop items when broken.
     */
    public boolean areDropsDisabled() {
        return this.disableDrops;
    }

    @Override
    public @Nullable ConfiguredStorage<?, ?> getStorage(@NotNull ResourceType<?, ?> type) {
        if (type == ResourceType.ENERGY) return this.energyStorage();
        if (type == ResourceType.ITEM) return this.itemStorage();
        if (type == ResourceType.FLUID) return this.fluidStorage();
        return null;
    }

    /**
     * Returns whether the current machine is enabled.
     *
     * @param world the world this machine is in.
     * @return whether the current machine is enabled.
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
     * Override {@link #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller)} for the machine's logic (only called server-side).
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
     * @see #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller) for server-side logic that can be disabled (not called) arbitrarily.
     */
    protected void tickConstant(@NotNull ServerLevel world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {}

    /**
     * Called every tick, when the machine is explicitly disabled (by redstone, for example).
     * Use this to clean-up resources distributed by {@link #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller)}.
     *
     * @param world    the world.
     * @param pos      the position of this machine.
     * @param state    the block state of this machine.
     * @param profiler the world profiler.
     */
    protected void tickDisabled(@NotNull ServerLevel world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {}

    /**
     * Called every tick on the client.
     *
     * @param world the world.
     * @param pos the position of this machine.
     * @param state the block state of this machine.
     */
    protected void tickClient(@NotNull /*Client*/Level world, @NotNull BlockPos pos, @NotNull BlockState state) {}

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

    @ApiStatus.Internal
    private @Nullable EnergyStorage getExposedEnergyStorage(@NotNull Direction direction) {
        assert this.level != null;
        return this.getExposedEnergyStorage(BlockFace.toFace(this.level.getBlockState(this.worldPosition).getValue(BlockStateProperties.HORIZONTAL_FACING), direction.getOpposite()));
    }

    @ApiStatus.Internal
    private @Nullable EnergyStorage getExposedEnergyStorage(@NotNull BlockFace face) {
        return this.getIOConfig().get(face).getExposedStorage(this.energyStorage);
    }

    @ApiStatus.Internal
    private @Nullable ExposedStorage<Item, ItemVariant> getExposedItemStorage(@NotNull Direction direction) {
        assert this.level != null;
        return this.getExposedItemStorage(BlockFace.toFace(this.level.getBlockState(this.worldPosition).getValue(BlockStateProperties.HORIZONTAL_FACING), direction.getOpposite()));
    }

    @ApiStatus.Internal
    private @Nullable ExposedStorage<Item, ItemVariant> getExposedItemStorage(@NotNull BlockFace face) {
        return this.getIOConfig().get(face).getExposedStorage(this.itemStorage);
    }

    @ApiStatus.Internal
    private @Nullable ExposedStorage<Fluid, FluidVariant> getExposedFluidInv(@NotNull Direction direction) {
        assert this.level != null;
        return this.getExposedFluidInv(BlockFace.toFace(this.level.getBlockState(this.worldPosition).getValue(BlockStateProperties.HORIZONTAL_FACING), direction.getOpposite()));
    }

    @ApiStatus.Internal
    private @Nullable ExposedStorage<Fluid, FluidVariant> getExposedFluidInv(@NotNull BlockFace face) {
        return this.getIOConfig().get(face).getExposedStorage(this.fluidStorage);
    }

    /**
     * Serializes the machine's state to nbt.
     * @param nbt the nbt to serialize to.
     */
    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.put(MLConstant.Nbt.ENERGY_STORAGE, this.energyStorage.writeNbt());
        nbt.put(MLConstant.Nbt.ITEM_STORAGE, this.itemStorage.writeNbt());
        nbt.put(MLConstant.Nbt.FLUID_STORAGE, this.fluidStorage.writeNbt());
        this.configuration.writeNbt(nbt);
        nbt.putBoolean(MLConstant.Nbt.DISABLE_DROPS, this.disableDrops);
    }

    /**
     * Deserializes the machine's state from nbt.
     * @param nbt the nbt to deserialize from.
     */
    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        if (nbt.contains(MLConstant.Nbt.ENERGY_STORAGE)) this.energyStorage.readNbt(Objects.requireNonNull(nbt.get(MLConstant.Nbt.ENERGY_STORAGE)));
        if (nbt.contains(MLConstant.Nbt.ITEM_STORAGE)) this.itemStorage.readNbt(Objects.requireNonNull(nbt.get(MLConstant.Nbt.ITEM_STORAGE)));
        if (nbt.contains(MLConstant.Nbt.FLUID_STORAGE)) this.fluidStorage.readNbt(Objects.requireNonNull(nbt.get(MLConstant.Nbt.FLUID_STORAGE)));
        this.configuration.readNbt(nbt);
        this.disableDrops = nbt.getBoolean(MLConstant.Nbt.DISABLE_DROPS);

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
     * @param world the world.
     */
    protected void trySpreadEnergy(@NotNull Level world) {
        for (Direction direction : DIRECTIONS) {
            ConfiguredMachineFace face = this.getIOConfig().get(BlockFace.toFace(world.getBlockState(this.worldPosition).getValue(BlockStateProperties.HORIZONTAL_FACING), direction.getOpposite()));
            if (face.getType() == ResourceType.ENERGY && face.getFlow().canFlowIn(ResourceFlow.OUTPUT)) {
                try (Transaction transaction = Transaction.openOuter()) {
                    EnergyStorageUtil.move(this.energyStorage, EnergyStorage.SIDED.find(world, this.worldPosition.relative(direction), direction.getOpposite()), this.getEnergyExtractionRate(), transaction);
                    transaction.commit();
                }
            }
        }
    }

    /**
     * Pushes fluids from this machine to adjacent fluid storages.
     * @param world the world.
     */
    protected void trySpreadFluids(@NotNull Level world) {
        for (Direction direction : DIRECTIONS) {
            Storage<FluidVariant> storage = this.getExposedFluidInv(direction);
            if (storage.supportsExtraction()) {
                Storage<FluidVariant> to = FluidStorage.SIDED.find(world, this.worldPosition.relative(direction), direction.getOpposite());
                try (Transaction transaction = Transaction.openOuter()) {
                    GenericStorageUtil.moveAll(storage, to, Long.MAX_VALUE, transaction);
                    transaction.commit();
                }
            }
        }
    }

    /**
     * Pushes items from this machine to adjacent item storages.
     * @param world the world.
     */
    protected void trySpreadItems(@NotNull Level world) {
        for (Direction direction : DIRECTIONS) {
            Storage<ItemVariant> storage = this.getExposedItemStorage(direction);
            if (storage.supportsExtraction()) {
                Storage<ItemVariant> to = ItemStorage.SIDED.find(world, this.worldPosition.relative(direction), direction.getOpposite());
                try (Transaction transaction = Transaction.openOuter()) {
                    GenericStorageUtil.moveAll(storage, to, Long.MAX_VALUE, transaction);
                    transaction.commit();
                }
            }
        }
    }

    /**
     * Tries to charge this machine from the item in the given slot in this {@link #itemStorage()}.
     * It is recommended to use {@link #attemptChargeFromStack(StateCachingStorageProvider)} instead to avoid testing for an energy storage when the inventory has not changed.
     * @param slot the slot to charge from.
     */
    protected void attemptChargeFromStack(int slot) {
        if (this.energyStorage().isFull()) return;

        EnergyStorage energyStorage = ContainerItemContext.ofSingleSlot(this.itemStorage().getSlot(slot)).find(EnergyStorage.ITEM);
        if (energyStorage != null) {
            if (energyStorage.supportsExtraction()) {
                try (Transaction transaction = Transaction.openOuter()) {
                    EnergyStorageUtil.move(energyStorage, this.energyStorage, this.getEnergyItemExtractionRate(), transaction);
                    transaction.commit();
                }
            }
        }
    }

    /**
     * Tries to drain some of this machine's power into the item in the given slot in this {@link #itemStorage}.
     * It is recommended to use {@link #attemptDrainPowerToStack(StateCachingStorageProvider)} instead to avoid testing for an energy storage when the inventory has not changed.
     *
     * @param slot The slot id of the item
     */
    protected void attemptDrainPowerToStack(int slot) {
        if (this.energyStorage().isEmpty()) return;
        EnergyStorage energyStorage = ContainerItemContext.ofSingleSlot(this.itemStorage().getSlot(slot)).find(EnergyStorage.ITEM);
        if (energyStorage != null) {
            if (energyStorage.supportsInsertion()) {
                try (Transaction transaction = Transaction.openOuter()) {
                    EnergyStorageUtil.move(this.energyStorage, energyStorage, this.getEnergyItemInsertionRate(), transaction);
                    transaction.commit();
                }
            }
        }
    }

    /**
     * Tries to charge this machine from the item in the given slot in this {@link #itemStorage()}.
     *
     * @param provider The storage provider
     */
    protected void attemptChargeFromStack(@NotNull StateCachingStorageProvider<EnergyStorage> provider) {
        if (this.energyStorage().isFull()) return;

        EnergyStorage energyStorage = provider.getStorage();
        if (energyStorage != null) {
            if (energyStorage.supportsExtraction()) {
                try (Transaction transaction = Transaction.openOuter()) {
                    EnergyStorageUtil.move(energyStorage, this.energyStorage, this.getEnergyItemExtractionRate(), transaction);
                    transaction.commit();
                }
            }
        }
    }

    /**
     * Tries to drain some of this machine's power into the item in the given slot in this {@link #itemStorage()}.
     *
     * @param provider The storage provider
     */
    protected void attemptDrainPowerToStack(@NotNull StateCachingStorageProvider<EnergyStorage> provider) {
        if (this.energyStorage().isEmpty()) return;
        EnergyStorage energyStorage = provider.getStorage();
        if (energyStorage != null) {
            if (energyStorage.supportsInsertion()) {
                try (Transaction transaction = Transaction.openOuter()) {
                    EnergyStorageUtil.move(this.energyStorage, energyStorage, this.getEnergyItemInsertionRate(), transaction);
                    transaction.commit();
                }
            }
        }
    }

    /**
     * Returns whether the given face can be configured for input or output.
     * @param face the face.
     * @return whether the given face can be configured for input or output.
     */
    public boolean isFaceLocked(@NotNull BlockFace face) {
        return false;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer serverPlayerEntity, @NotNull FriendlyByteBuf packetByteBuf) {
        packetByteBuf.writeBlockPos(this.getBlockPos());
    }

    @Override
    public Component getDisplayName() {
        return this.name;
    }

    public MachineConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public Object getRenderAttachmentData() {
        return getIOConfig();
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
