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
import dev.galacticraft.impl.Constant;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
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
public abstract class MachineBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, StorageProvider {
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
    private final Text name;

    /**
     * Creates a new machine block entity.
     *
     * @param type The type of block entity.
     * @param pos The position of this machine.
     * @param state The block state of this machine.
     */
    protected MachineBlockEntity(@NotNull BlockEntityType<? extends MachineBlockEntity> type, @NotNull BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.name = state.getBlock().getName().setStyle(Constant.Text.DARK_GRAY_STYLE);
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
        return this.getEnergyCapacity() / 80;
    }

    /**
     * The maximum amount of energy that the machine can extract from items in its inventory (per transaction).
     *
     * @see #attemptChargeFromStack(int) for the actual charging logic.
     * @return The maximum amount of energy that the machine can extract from items in its inventory (per transaction).
     */
    @Contract(pure = true)
    public long getEnergyItemExtractionRate() {
        return this.getEnergyCapacity() / 80;
    }

    /**
     * The maximum amount of energy that the machine can intake per transaction.
     * If adjacent machines should not be able to insert energy, return zero.
     *
     * @return The maximum amount of energy that the machine can intake per transaction.
     */
    @Contract(pure = true)
    public long getEnergyInsertionRate() {
        return this.getEnergyCapacity() / 60;
    }

    /**
     * The maximum amount of energy that the machine can extract per transaction.
     * If adjacent machines should not be able to extract energy, return zero.
     *
     * @return The maximum amount of energy that the machine can eject per transaction.
     */
    @Contract(pure = true)
    public long getEnergyExtractionRate() {
        return this.getEnergyCapacity() / 60;
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
     * Returns the status of this machine. Machine status is calculated in {@link #tick(ServerWorld, BlockPos, BlockState)},
     * but may be modified manually by calling {@link #setStatus(ServerWorld, MachineStatus)}.
     *
     * @see #tick(ServerWorld, BlockPos, BlockState) to calculate the status of this machine.
     * @see #setStatus(ServerWorld, MachineStatus) to manually change the status of this machine.
     * @return the status of this machine.
     */
    public @NotNull MachineStatus getStatus() {
        return this.configuration.getStatus();
    }

    /**
     * Sets the status of this machine. It is recommended to use {@link #tick(ServerWorld, BlockPos, BlockState)} to
     * calculate the status of this machine, rather than setting it manually.
     *
     * @param world the world this machine is in.
     * @param status the status to set.
     */
    public void setStatus(@Nullable ServerWorld world, @NotNull MachineStatus status) {
        if (this.isStatusActive() != status.type().isActive()) {
            if (world != null) {
                world.setBlockState(this.pos, this.getCachedState().with(MachineBlock.ACTIVE, status.type().isActive()));
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
    public boolean isDisabled(@NotNull World world) {
        return switch (this.getRedstoneActivation()) {
            case LOW -> world.isReceivingRedstonePower(this.pos);
            case HIGH -> !world.isReceivingRedstonePower(this.pos);
            case IGNORE -> false;
        };
    }

    /**
     * Updates the machine every tick.
     * Override {@link #tick(ServerWorld, BlockPos, BlockState)} for the machine's logic (only called server-side).
     *
     * @param world the world.
     * @param pos the position of this machine.
     * @param state the block state of this machine.
     * @see #tick(ServerWorld, BlockPos, BlockState) for server-side logic that can be disabled (not called) arbitrarily.
     * @see #tickConstant(ServerWorld, BlockPos, BlockState) for the server-side logic that is always called.
     * @see #tickClient(World, BlockPos, BlockState) for the client-side logic.
     */
    public final void tickBase(@NotNull World world, @NotNull BlockPos pos, @NotNull BlockState state) {
        this.setCachedState(state);
        if (!world.isClient()) {
            world.getProfiler().push("constant");
            ServerWorld serverWorld = (ServerWorld) world;
            this.tickConstant(serverWorld, pos, state);
            if (this.isDisabled(world)) {
                world.getProfiler().swap("disabled");
                this.tickDisabled(serverWorld, pos, state);
            } else {
                world.getProfiler().swap("active");
                this.setStatus(serverWorld, this.tick(serverWorld, pos, state));
            }
        } else {
            world.getProfiler().push("client");
            this.tickClient(world, pos, state);
        }
        world.getProfiler().pop();
    }

    /**
     * Called every tick, even if the machine is not active/powered.
     * Use this to tick fuel consumption or transfer resources, for example.
     *
     * @param world the world.
     * @param pos the position of this machine.
     * @param state the block state of this machine.
     * @see #tick(ServerWorld, BlockPos, BlockState) for server-side logic that can be disabled (not called) arbitrarily.
     */
    protected void tickConstant(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull BlockState state) {}

    /**
     * Called every tick, when the machine is explicitly disabled (by redstone, for example).
     * Use this to clean-up resources distributed by {@link #tick(ServerWorld, BlockPos, BlockState)}.
     *
     * @param world the world.
     * @param pos the position of this machine.
     * @param state the block state of this machine.
     */
    protected void tickDisabled(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull BlockState state) {}

    /**
     * Called every tick on the client.
     *
     * @param world the world.
     * @param pos the position of this machine.
     * @param state the block state of this machine.
     */
    protected void tickClient(@NotNull /*Client*/World world, @NotNull BlockPos pos, @NotNull BlockState state) {}

    /**
     * Called every tick on the server, when the machine is active.
     * Use this to update crafting progress, for example.
     * Be sure to clean-up state in {@link #tickDisabled(ServerWorld, BlockPos, BlockState)}.
     *
     * @param world the world.
     * @param pos the position of this machine.
     * @param state the block state of this machine.
     * @see #tickDisabled(ServerWorld, BlockPos, BlockState)
     * @see #tickConstant(ServerWorld, BlockPos, BlockState)
     * @return the status of this machine.
     */
    protected abstract @NotNull MachineStatus tick(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull BlockState state);

    @ApiStatus.Internal
    private @NotNull EnergyStorage getExposedEnergyStorage(@NotNull Direction direction) {
        assert this.world != null;
        return this.getExposedEnergyStorage(BlockFace.toFace(this.world.getBlockState(this.pos).get(Properties.HORIZONTAL_FACING), direction.getOpposite()));
    }

    @ApiStatus.Internal
    private @NotNull EnergyStorage getExposedEnergyStorage(@NotNull BlockFace face) {
        return this.getIOConfig().get(face).getExposedStorage(this.energyStorage);
    }

    @ApiStatus.Internal
    private @NotNull ExposedStorage<Item, ItemVariant> getExposedItemStorage(@NotNull Direction direction) {
        assert this.world != null;
        return this.getExposedItemStorage(BlockFace.toFace(this.world.getBlockState(this.pos).get(Properties.HORIZONTAL_FACING), direction.getOpposite()));
    }

    @ApiStatus.Internal
    private @NotNull ExposedStorage<Item, ItemVariant> getExposedItemStorage(@NotNull BlockFace face) {
        return this.getIOConfig().get(face).getExposedStorage(this.itemStorage);
    }

    @ApiStatus.Internal
    private @NotNull ExposedStorage<Fluid, FluidVariant> getExposedFluidInv(@NotNull Direction direction) {
        assert this.world != null;
        return this.getExposedFluidInv(BlockFace.toFace(this.world.getBlockState(this.pos).get(Properties.HORIZONTAL_FACING), direction.getOpposite()));
    }

    @ApiStatus.Internal
    private @NotNull ExposedStorage<Fluid, FluidVariant> getExposedFluidInv(@NotNull BlockFace face) {
        return this.getIOConfig().get(face).getExposedStorage(this.fluidStorage);
    }

    /**
     * Serializes the machine's state to nbt.
     * @param nbt the nbt to serialize to.
     */
    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put(Constant.Nbt.ENERGY_STORAGE, this.energyStorage.writeNbt());
        nbt.put(Constant.Nbt.ITEM_STORAGE, this.itemStorage.writeNbt());
        nbt.put(Constant.Nbt.FLUID_STORAGE, this.fluidStorage.writeNbt());
        this.configuration.writeNbt(nbt);
        nbt.putBoolean(Constant.Nbt.DISABLE_DROPS, this.disableDrops);
    }

    /**
     * Deserializes the machine's state from nbt.
     * @param nbt the nbt to deserialize from.
     */
    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains(Constant.Nbt.ENERGY_STORAGE)) this.energyStorage.readNbt(Objects.requireNonNull(nbt.get(Constant.Nbt.ENERGY_STORAGE)));
        if (nbt.contains(Constant.Nbt.ITEM_STORAGE)) this.itemStorage.readNbt(Objects.requireNonNull(nbt.get(Constant.Nbt.ITEM_STORAGE)));
        if (nbt.contains(Constant.Nbt.FLUID_STORAGE)) this.fluidStorage.readNbt(Objects.requireNonNull(nbt.get(Constant.Nbt.FLUID_STORAGE)));
        this.configuration.readNbt(nbt);
        this.disableDrops = nbt.getBoolean(Constant.Nbt.DISABLE_DROPS);
        assert this.world != null;
        if (this.world.isClient){
            MinecraftClient.getInstance().worldRenderer.scheduleBlockRender(this.pos.getX(), this.pos.getY(), this.pos.getZ());
        }
    }

    /**
     * Pushes energy from this machine to adjacent capacitor blocks.
     * @param world the world.
     */
    public void trySpreadEnergy(@NotNull World world) {
        for (Direction direction : DIRECTIONS) {
            ConfiguredMachineFace face = this.getIOConfig().get(BlockFace.toFace(world.getBlockState(this.pos).get(Properties.HORIZONTAL_FACING), direction.getOpposite()));
            if (face.getType() == ResourceType.ENERGY && face.getFlow().canFlowIn(ResourceFlow.OUTPUT)) {
                try (Transaction transaction = Transaction.openOuter()) {
                    EnergyStorageUtil.move(this.energyStorage, EnergyStorage.SIDED.find(world, this.pos.offset(direction), direction.getOpposite()), this.getEnergyExtractionRate(), transaction);
                    transaction.commit();
                }
            }
        }
    }

    /**
     * Pushes fluids from this machine to adjacent fluid storages.
     * @param world the world.
     */
    public void trySpreadFluids(@NotNull World world) {
        for (Direction direction : DIRECTIONS) {
            Storage<FluidVariant> storage = this.getExposedFluidInv(direction);
            if (storage.supportsExtraction()) {
                Storage<FluidVariant> to = FluidStorage.SIDED.find(world, this.pos.offset(direction), direction.getOpposite());
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
    public void trySpreadItems(@NotNull World world) {
        for (Direction direction : DIRECTIONS) {
            Storage<ItemVariant> storage = this.getExposedItemStorage(direction);
            if (storage.supportsExtraction()) {
                Storage<ItemVariant> to = ItemStorage.SIDED.find(world, this.pos.offset(direction), direction.getOpposite());
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
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, @NotNull PacketByteBuf packetByteBuf) {
        packetByteBuf.writeBlockPos(this.getPos());
    }

    @Override
    public Text getDisplayName() {
        return this.name;
    }
}
