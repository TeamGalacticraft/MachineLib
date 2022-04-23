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
import dev.galacticraft.api.transfer.StateCachingStorageProvider;
import dev.galacticraft.impl.machine.Constant;
import dev.galacticraft.impl.machine.storage.io.NullConfiguredStorage;
import dev.galacticraft.impl.util.GenericStorageUtil;
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
@SuppressWarnings("UnstableApiUsage")
public abstract class MachineBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, StorageProvider {
    private static final Direction[] DIRECTIONS = Direction.values();
    /**
     * The configuration for this machine.
     */
    private final MachineConfiguration configuration = MachineConfiguration.create();

    /**
     * Whether the machine will not drop items when broken.
     */
    @ApiStatus.Internal
    private boolean noDrop = false;
    /**
     * Whether the machine has been initialized and synced to the client.
     */
    @ApiStatus.Internal
    private boolean loaded = false;

    /**
     * The energy storage for this machine.
     * @see #energyStorage()
     */
    private final @NotNull MachineEnergyStorage energyStorage = MachineEnergyStorage.of(this.getEnergyCapacity(), this.getEnergyInsertionRate(), this.getEnergyExtractionRate());

    /**
     * The item storage for this machine.
     * @see #itemStorage()
     */
    private final @NotNull MachineItemStorage itemStorage = this.createItemStorage();

    /**
     * The fluid storage for this machine.
     * @see #fluidStorage()
     */
    private final @NotNull MachineFluidStorage fluidStorage = this.createFluidStorage();

    /**
     * Creates a new machine block entity.
     * @param type The type of block entity.
     * @param pos The position of this machine.
     * @param state The block state of this machine.
     */
    protected MachineBlockEntity(@NotNull BlockEntityType<? extends MachineBlockEntity> type, @NotNull BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Registers the transfer handlers for this machine.
     * @param type the block entity type to register.
     */
    public static void registerComponents(@NotNull BlockEntityType<? extends MachineBlockEntity> type) {
        EnergyStorage.SIDED.registerForBlockEntity(MachineBlockEntity::getExposedEnergyStorage, type);
        ItemStorage.SIDED.registerForBlockEntity(MachineBlockEntity::getExposedItemStorage, type);
        FluidStorage.SIDED.registerForBlockEntity(MachineBlockEntity::getExposedFluidInv, type);
    }

    /**
     * The maximum amount of energy that this machine can hold.
     * @return Energy capacity of this machine.
     */
    @Contract(pure = true)
    public long getEnergyCapacity() {
        return 0;
    }

    /**
     * The maximum amount of energy that the machine can insert into items in its inventory (per transaction).*
     * @return The maximum amount of energy that the machine can insert into items in its inventory (per transaction).
     */
    @Contract(pure = true)
    public long getEnergyItemInsertionRate() {
        return this.getEnergyCapacity() / 80;
    }

    /**
     * The maximum amount of energy that the machine can extract from items in its inventory (per transaction).
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

    @Contract(pure = true)
    protected boolean isActive() {
        return this.getStatus().getType().isActive();
    }

    /**
     * Creates an item storage for this machine.
     * @return The item storage configured for this machine.
     */
    protected @NotNull MachineItemStorage createItemStorage() {
        return MachineItemStorage.empty();
    }

    /**
     * Creates a fluid storage for this machine.
     * @return The fluid storage configured for this machine.
     */
    protected @NotNull MachineFluidStorage createFluidStorage() {
        return MachineFluidStorage.empty();
    }

    /**
     * Sets the redstone activation mode of this machine.
     * @param redstone the redstone activation mode to use.
     */
    public void setRedstone(@NotNull RedstoneActivation redstone) {
        this.configuration.setRedstoneActivation(redstone);
    }

    /**
     * Returns the status of this machine.
     * @return the status of this machine.
     */
    public @NotNull MachineStatus getStatus() {
        return this.configuration.getStatus();
    }

    /**
     * Sets the status of this machine.
     * @param status the status to set.
     */
    public void setStatus(@NotNull MachineStatus status) {
        if (this.isActive() != status.getType().isActive()) {
            if (!this.world.isClient()) {
                BlockState with = this.getCachedState().with(MachineBlock.ACTIVE, status.getType().isActive());
                this.setCachedState(with);
                this.world.setBlockState(this.pos, with);
            }
        }
        this.configuration.setStatus(status);
    }

    /**
     * Returns the energy storage of this machine.
     * @return The energy storage of this machine.
     * @see #getEnergyCapacity()
     */
    public final @NotNull MachineEnergyStorage energyStorage() {
        return this.energyStorage;
    }

    /**
     * Returns the item storage of this machine.
     * @return the item storage of this machine.
     * @see #createItemStorage()
     */
    public final @NotNull MachineItemStorage itemStorage() {
        return this.itemStorage;
    }

    /**
     * Returns the fluid storage of this machine.
     * @return the fluid storage of this machine.
     * @see #createFluidStorage()
     */
    public final @NotNull MachineFluidStorage fluidStorage() {
        return this.fluidStorage;
    }

    /**
     * Returns the security settings of this machine.
     * @return the security settings of this machine.
     */
    public final @NotNull SecuritySettings security() {
        return this.configuration.getSecurity();
    }

    /**
     * Returns the redstone configuration of this machine.
     * @return the redstone configuration of this machine.
     */
    public final @NotNull RedstoneActivation redstoneInteraction() {
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
    public boolean dontDropItems() {
        return this.noDrop;
    }

    @Override
    public ConfiguredStorage<?, ?> getStorage(@NotNull ResourceType<?, ?> type) {
        if (type == ResourceType.ENERGY) return this.energyStorage();
        if (type == ResourceType.ITEM) return this.itemStorage();
        if (type == ResourceType.FLUID) return this.fluidStorage();
        return NullConfiguredStorage.INSTANCE;
    }

    /**
     * Returns whether the current machine is enabled.
     * @return whether the current machine is enabled.
     */
    public boolean disabled() {
        return switch (this.redstoneInteraction()) {
            case LOW -> this.getWorld().isReceivingRedstonePower(this.pos);
            case HIGH -> !this.getWorld().isReceivingRedstonePower(this.pos);
            case IGNORE -> false;
        };
    }

    /**
     * Updates the machine every tick.
     * @param world the world.
     * @param pos the position of this machine.
     * @param state the block state of this machine.
     */
    public final void tickBase(@NotNull World world, @NotNull BlockPos pos, @NotNull BlockState state) {
        assert this.world == world;
        assert this.pos == pos;

        this.setCachedState(state);
        if (!this.world.isClient()) {
            this.world.getProfiler().push("constant");
            ServerWorld serverWorld = (ServerWorld) world;
            this.tickConstant(serverWorld, pos, state);
            if (this.disabled()) {
                this.world.getProfiler().swap("disabled");
                this.tickDisabled(serverWorld, pos, state);
            } else {
                this.world.getProfiler().swap("active");
                this.setStatus(this.tick(serverWorld, pos, state));
            }
        } else {
            this.world.getProfiler().push("client");
            this.tickClient(world, pos, state);
        }
        this.world.getProfiler().pop();
    }

    /**
     * Called every tick, even if the machine is not active/powered.
     * Use this to tick fuel consumption or transfer resources, for example.
     */
    protected void tickConstant(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull BlockState state) {}

    protected void tickDisabled(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull BlockState state) {}

    protected void tickClient(@NotNull /*Client*/World world, @NotNull BlockPos pos, @NotNull BlockState state) {}

    protected abstract @NotNull MachineStatus tick(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull BlockState state);

    private @NotNull EnergyStorage getExposedEnergyStorage(@NotNull Direction direction) {
        assert this.world != null;
        return this.getExposedEnergyStorage(BlockFace.toFace(this.world.getBlockState(this.pos).get(Properties.HORIZONTAL_FACING), direction.getOpposite()));
    }

    private @NotNull EnergyStorage getExposedEnergyStorage(@NotNull BlockFace face) {
        return this.getIOConfig().get(face).getExposedStorage(this.energyStorage);
    }

    private @NotNull ExposedStorage<Item, ItemVariant> getExposedItemStorage(@NotNull Direction direction) {
        assert this.world != null;
        return this.getExposedItemStorage(BlockFace.toFace(this.world.getBlockState(this.pos).get(Properties.HORIZONTAL_FACING), direction.getOpposite()));
    }

    private @NotNull ExposedStorage<Item, ItemVariant> getExposedItemStorage(@NotNull BlockFace face) {
        return this.getIOConfig().get(face).getExposedStorage(this.itemStorage);
    }

    private @NotNull ExposedStorage<Fluid, FluidVariant> getExposedFluidInv(@NotNull Direction direction) {
        assert this.world != null;
        return this.getExposedFluidInv(BlockFace.toFace(this.world.getBlockState(this.pos).get(Properties.HORIZONTAL_FACING), direction.getOpposite()));
    }

    private @NotNull ExposedStorage<Fluid, FluidVariant> getExposedFluidInv(@NotNull BlockFace face) {
        return this.getIOConfig().get(face).getExposedStorage(this.fluidStorage);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put(Constant.Nbt.ENERGY_STORAGE, this.energyStorage.writeNbt());
        nbt.put(Constant.Nbt.ITEM_STORAGE, this.itemStorage.writeNbt());
        nbt.put(Constant.Nbt.FLUID_STORAGE, this.fluidStorage.writeNbt());
        this.configuration.writeNbt(nbt);
        nbt.putBoolean(Constant.Nbt.NO_DROP, this.noDrop);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains(Constant.Nbt.ENERGY_STORAGE)) this.energyStorage.readNbt(Objects.requireNonNull(nbt.get(Constant.Nbt.ENERGY_STORAGE)));
        if (nbt.contains(Constant.Nbt.ITEM_STORAGE)) this.itemStorage.readNbt(Objects.requireNonNull(nbt.get(Constant.Nbt.ITEM_STORAGE)));
        if (nbt.contains(Constant.Nbt.FLUID_STORAGE)) this.fluidStorage.readNbt(Objects.requireNonNull(nbt.get(Constant.Nbt.FLUID_STORAGE)));
        this.configuration.readNbt(nbt);
        this.noDrop = nbt.getBoolean(Constant.Nbt.NO_DROP);
        assert this.world != null;
        if (!this.world.isClient) {
            if (this.loaded) {
                this.sync();
            } else {
                this.loaded = true;
            }
        } else {
            MinecraftClient.getInstance().worldRenderer.scheduleBlockRender(this.pos.getX(), this.pos.getY(), this.pos.getZ());
        }
    }

    public void trySpreadEnergy() {
        for (Direction direction : DIRECTIONS) {
            ConfiguredMachineFace face = this.getIOConfig().get(BlockFace.toFace(this.world.getBlockState(this.pos).get(Properties.HORIZONTAL_FACING), direction.getOpposite()));
            if (face.getType() == ResourceType.ENERGY && face.getFlow().canFlowIn(ResourceFlow.OUTPUT)) {
                try (Transaction transaction = Transaction.openOuter()) {
                    EnergyStorageUtil.move(this.energyStorage, EnergyStorage.SIDED.find(this.world, this.pos.offset(direction), direction.getOpposite()), this.getEnergyExtractionRate(), transaction);
                    transaction.commit();
                }
            }
        }
    }

    public void trySpreadFluids() {
        for (Direction direction : DIRECTIONS) {
            Storage<FluidVariant> storage = this.getExposedFluidInv(direction);
            if (storage.supportsExtraction()) {
                Storage<FluidVariant> to = FluidStorage.SIDED.find(this.world, this.pos.offset(direction), direction.getOpposite());
                try (Transaction transaction = Transaction.openOuter()) {
                    GenericStorageUtil.moveAll(storage, to, Long.MAX_VALUE, transaction);
                    transaction.commit();
                }
            }
        }
    }

    public void trySpreadItems() {
        for (Direction direction : DIRECTIONS) {
            Storage<ItemVariant> storage = this.getExposedItemStorage(direction);
            if (storage.supportsExtraction()) {
                Storage<ItemVariant> to = ItemStorage.SIDED.find(this.world, this.pos.offset(direction), direction.getOpposite());
                try (Transaction transaction = Transaction.openOuter()) {
                    GenericStorageUtil.moveAll(storage, to, Long.MAX_VALUE, transaction);
                    transaction.commit();
                }
            }
        }
    }

    /**
     * Tries to charge this machine from the item in the given slot in this {@link #itemStorage}.
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
     * Tries to charge this machine from the item in the given slot in this {@link #itemStorage}.
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
     * Tries to drain some of this machine's power into the item in the given slot in this {@link #itemStorage}.
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

    public boolean isFaceLocked(BlockFace face) {
        return false;
    }

    public void sync() {
        assert !this.world.isClient();
        ((ServerWorld) this.world).getChunkManager().markForUpdate(getPos());
        this.world.updateNeighborsAlways(this.pos, this.getCachedState().getBlock());
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, @NotNull PacketByteBuf packetByteBuf) {
        packetByteBuf.writeBlockPos(this.getPos());
    }

    @Override
    public Text getDisplayName() {
        return this.getCachedState().getBlock().getName().copy().setStyle(Constant.Text.DARK_GRAY_STYLE);
    }

    public MachineConfiguration getConfiguration() {
        return this.configuration;
    }
}
