/*
 * Copyright (c) 2019-2022 Team Galacticraft
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

import dev.galacticraft.api.block.MachineBlock;
import dev.galacticraft.api.block.util.BlockFace;
import dev.galacticraft.api.gas.GasVariant;
import dev.galacticraft.api.machine.*;
import dev.galacticraft.api.machine.storage.MachineEnergyStorage;
import dev.galacticraft.api.machine.storage.MachineFluidStorage;
import dev.galacticraft.api.machine.storage.MachineGasStorage;
import dev.galacticraft.api.machine.storage.MachineItemStorage;
import dev.galacticraft.api.machine.storage.io.ConfiguredStorage;
import dev.galacticraft.api.machine.storage.io.ResourceType;
import dev.galacticraft.api.transfer.v1.gas.GasStorage;
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
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
@SuppressWarnings("UnstableApiUsage")
public abstract class MachineBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ResourceProvider {
    private final MachineConfiguration configuration = new MachineConfiguration();

    private boolean noDrop = false;
    private boolean loaded = false;

    public final @NotNull MachineEnergyStorage capacitor = MachineEnergyStorage.of(this.getEnergyCapacity(), this.energyInsertionRate(), this.energyExtractionRate());
    private final @NotNull MachineItemStorage itemStorage = this.createInventory(MachineItemStorage.Builder.create()).build();
    private final @NotNull MachineGasStorage gasStorage = this.createGasStorage(MachineGasStorage.Builder.create()).build();

    private final @NotNull MachineFluidStorage fluidStorage = this.createFluidInv(MachineFluidStorage.Builder.create()).build();

    public MachineBlockEntity(BlockEntityType<? extends MachineBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void registerComponents(BlockEntityType<? extends MachineBlockEntity> type) {
        EnergyStorage.SIDED.registerForBlockEntity(MachineBlockEntity::getExposedCapacitor, type);
        ItemStorage.SIDED.registerForBlockEntity(MachineBlockEntity::getExposedItemStorage, type);
        FluidStorage.SIDED.registerForBlockEntity(MachineBlockEntity::getExposedFluidInv, type);
        GasStorage.SIDED.registerForBlockEntity(MachineBlockEntity::getExposedGasInv, type);
    }

    /**
     * The maximum amount of energy that this machine can hold.
     *
     * @return Energy capacity of this machine.
     */
    public long getEnergyCapacity() {
        return 30000; //todo config
    }

    /**
     * The amount of energy that the machine consumes in a tick.
     *
     * @return The amount of energy that the machine consumes in a tick.
     */
    protected long energyConsumption() {
        return 0;
    }

    /**
     * The amount of energy that the machine will consume this tick.
     *
     * @return The amount of energy that the machine will consume this tick.
     */
    public long getEnergyConsumption() {
        if (this.getStatus().getType().isActive()) return energyConsumption();
        return 0;
    }

    /**
     * The maximum amount of energy that the machine can intake per transaction.
     * If adjacent machines should not be able to insert energy, return zero.
     *
     * @return The maximum amount of energy that the machine can intake per transaction.
     */
    public long energyInsertionRate() {
        return 500;
    }

    /**
     * The maximum amount of energy that the machine can extract per transaction.
     * If adjacent machines should not be able to extract energy, return zero.
     *
     * @return The maximum amount of energy that the machine can eject per transaction.
     */
    public long energyExtractionRate() {
        return 500;
    }

    /**
     * The amount of energy that the machine generates in a tick.
     * @return The amount of energy that the machine generates in a tick.
     */
    public long energyGeneration() {
        return 0;
    }

    /**
     * The amount of energy that the machine generates in a tick, in the current context.
     * @return The amount of energy that the machine generates in a tick, in the current context.
     */
    public long getEnergyGeneration() {
        if (this.getStatus().getType().isActive()) return energyGeneration();
        return 0;
    }

    protected MachineItemStorage.Builder createInventory(MachineItemStorage.Builder builder) {
        return builder;
    }

    protected MachineGasStorage.Builder createGasStorage(MachineGasStorage.Builder builder) {
        return builder;
    }

    protected MachineFluidStorage.Builder createFluidInv(MachineFluidStorage.Builder builder) {
        return builder;
    }

    public void setRedstone(@NotNull RedstoneInteractionType redstone) {
        this.configuration.setRedstone(redstone);
    }

    public @NotNull MachineStatus getStatus() {
        return this.configuration.getStatus();
    }

    public void setStatus(MachineStatus status) {
        assert this.world != null;
        if (!this.world.isClient()) this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(MachineBlock.ACTIVE, status.getType().isActive()));
        this.configuration.setStatus(status);
    }

    public void setStatusById(int index) {
        this.setStatus(this.getStatusById(index));
    }

    protected abstract MachineStatus getStatusById(int index);

    /**
     * @return The maximum amount of energy that can be transferred to or from a battery in this machine per call to
     * {@link #attemptChargeFromStack(int)} or {@link #attemptDrainPowerToStack(int)}
     */
    protected int getBatteryTransferRate() {
        return 500;
    }

    public final @NotNull MachineEnergyStorage capacitor() {
        return this.capacitor;
    }

    public final @NotNull MachineItemStorage itemStorage() {
        return this.itemStorage;
    }

    public final @NotNull MachineGasStorage gasStorage() {
        return this.gasStorage;
    }

    public final @NotNull MachineFluidStorage fluidStorage() {
        return this.fluidStorage;
    }

    public final @NotNull EnergyStorage getExposedCapacitor(@NotNull Direction direction) {
        return this.getExposedCapacitor(BlockFace.toFace(this.world.getBlockState(this.pos).get(Properties.HORIZONTAL_FACING), direction.getOpposite()));
    }

    public final @NotNull EnergyStorage getExposedCapacitor(@NotNull BlockFace face) {
        return this.getIOConfig().get(face).getExposedStorage(this.capacitor);
    }

    public final @NotNull Storage<ItemVariant> getExposedItemStorage(@NotNull Direction direction) {
        return this.getExposedItemStorage(BlockFace.toFace(this.world.getBlockState(this.pos).get(Properties.HORIZONTAL_FACING), direction.getOpposite()));
    }

    public final @NotNull Storage<ItemVariant> getExposedItemStorage(@NotNull BlockFace face) {
        return this.getIOConfig().get(face).getExposedStorage(this.itemStorage);

    }

    public final @NotNull Storage<FluidVariant> getExposedFluidInv(@NotNull Direction direction) {
        return this.getExposedFluidInv(BlockFace.toFace(this.world.getBlockState(this.pos).get(Properties.HORIZONTAL_FACING), direction.getOpposite()));
    }

    public final @NotNull Storage<FluidVariant> getExposedFluidInv(@NotNull BlockFace face) {
        return this.getIOConfig().get(face).getExposedStorage(this.fluidStorage);
    }

    public final @NotNull Storage<GasVariant> getExposedGasInv(@NotNull Direction direction) {
        return this.getExposedGasInv(BlockFace.toFace(this.world.getBlockState(this.pos).get(Properties.HORIZONTAL_FACING), direction.getOpposite()));
    }

    public final @NotNull Storage<GasVariant> getExposedGasInv(@NotNull BlockFace face) {
        return this.getIOConfig().get(face).getExposedStorage(this.gasStorage);
    }


    public final @NotNull SecurityInfo security() {
        return this.configuration.getSecurity();
    }

    public final @NotNull RedstoneInteractionType redstoneInteraction() {
        return this.configuration.getRedstoneInteraction();
    }

    public final @NotNull MachineIOConfig getIOConfig() {
        return this.configuration.getSideConfiguration();
    }

    public boolean noDrops() {
        return noDrop;
    }

    @Override
    public ConfiguredStorage<?, ?> getResource(@NotNull ResourceType<?, ?> type) {
        if (type == ResourceType.ENERGY) return this.capacitor();
        if (type == ResourceType.ITEM) return this.itemStorage();
        if (type == ResourceType.FLUID) return this.fluidStorage();
        if (type == ResourceType.GAS) return this.gasStorage();
        return NullConfiguredStorage.INSTANCE;
    }

    /**
     * Whether the current machine is enabled
     *
     * @return The state of the machine
     */
    public boolean disabled() {
        return switch (this.redstoneInteraction()) {
            case LOW -> this.getWorld().isReceivingRedstonePower(pos);
            case HIGH -> !this.getWorld().isReceivingRedstonePower(pos);
            default -> false;
        };
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        assert this.world == world;
        assert this.pos == pos;

        this.setCachedState(state);
        if (!world.isClient) {
            this.updateComponents();
            if (this.disabled()) {
                this.tickDisabled();
                return;
            }
            try (Transaction transaction = Transaction.openOuter()) {
                this.setStatus(this.updateStatus());
            }
            this.tickWork();
            if (this.getStatus().getType().isActive()) {
                if (this.energyConsumption() > 0) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        this.capacitor().extract(this.getEnergyConsumption(), transaction);
                        transaction.commit();
                    }
                } else if (this.energyGeneration() > 0) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        this.capacitor().insert(this.getEnergyGeneration(), transaction);
                        transaction.commit();
                    }
                }
            } else {
                this.idleEnergyDecrement();
            }
        } else {
            this.clientTick();
        }
    }

    protected abstract void tickDisabled();

    protected void clientTick() {}

    /**
     * Returns the updated machine status
     * Should not have any side effects
     * @return The updated status
     */
    @Contract(pure = true)
    public abstract @NotNull MachineStatus updateStatus();

    /**
     * Update the work/progress and/or create the outputted items in this method
     */
    public abstract void tickWork();

    public void updateComponents() {
        this.trySpreadEnergy();
    }

    public boolean hasEnergyToWork() {
        return this.capacitor().getAmount() >= this.energyConsumption();
    }

    public boolean isTankFull(int tank) {
        StorageView<FluidVariant> tank1 = this.fluidStorage().getSlot(tank);
        return tank1.getAmount() >= tank1.getCapacity();
    }

    @NotNull
    public <C extends Inventory, T extends Recipe<C>> Optional<T> getRecipe(RecipeType<T> type, C inventory) {
        if (this.world == null) return Optional.empty();
        return this.world.getRecipeManager().getFirstMatch(type, inventory, this.world);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put(Constant.Nbt.ENERGY_STORAGE, this.capacitor.writeNbt());
        nbt.put(Constant.Nbt.ITEM_STORAGE, this.itemStorage.writeNbt());
        nbt.put(Constant.Nbt.FLUID_STORAGE, this.fluidStorage.writeNbt());
        nbt.put(Constant.Nbt.GAS_STORAGE, this.gasStorage.writeNbt());
        this.configuration.writeNbt(nbt);
        nbt.putBoolean(Constant.Nbt.NO_DROP, this.noDrop);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains(Constant.Nbt.ENERGY_STORAGE)) this.capacitor.readNbt(nbt.get(Constant.Nbt.ENERGY_STORAGE));
        if (nbt.contains(Constant.Nbt.ITEM_STORAGE)) this.itemStorage.readNbt(nbt.get(Constant.Nbt.ITEM_STORAGE));
        if (nbt.contains(Constant.Nbt.FLUID_STORAGE)) this.fluidStorage.readNbt(nbt.get(Constant.Nbt.FLUID_STORAGE));
        if (nbt.contains(Constant.Nbt.GAS_STORAGE)) this.gasStorage.readNbt(nbt.get(Constant.Nbt.GAS_STORAGE));
        this.configuration.readNbt(nbt);
        this.noDrop = nbt.getBoolean(Constant.Nbt.NO_DROP);
        if (!this.world.isClient) {
            if (loaded) {
                this.sync();
            } else {
                loaded = true;
            }
        } else {
            MinecraftClient.getInstance().worldRenderer.scheduleBlockRender(pos.getX(), pos.getY(), pos.getZ());
        }
    }

    public void trySpreadEnergy() {
        for (Direction direction : Direction.values()) {
            EnergyStorage capacitor = this.getExposedCapacitor(direction);
            if (capacitor.supportsExtraction()) {
                try (Transaction transaction = Transaction.openOuter()) {
                    EnergyStorageUtil.move(capacitor, EnergyStorage.SIDED.find(world, pos.offset(direction), direction.getOpposite()), Long.MAX_VALUE, transaction);
                    transaction.commit();
                }
            }
        }
    }

    public void trySpreadFluids() {
        for (Direction direction : Direction.values()) {
            Storage<FluidVariant> storage = this.getExposedFluidInv(direction);
            if (storage.supportsExtraction()) {
                Storage<FluidVariant> to = FluidStorage.SIDED.find(world, pos.offset(direction), direction.getOpposite());
                try (Transaction transaction = Transaction.openOuter()) {
                    GenericStorageUtil.moveAll(storage, to, Long.MAX_VALUE, transaction);
                    transaction.commit();
                }
            }
        }

    }

    public void idleEnergyDecrement() {
        if (this.energyConsumption() > 0) {
            assert this.world != null;
            if (this.world.random.nextInt(20) == 0) {
                try (Transaction transaction = Transaction.openOuter()) {
                    this.capacitor().extract(this.energyConsumption() / 20, transaction);
                    transaction.commit();
                }
            }
        }
    }

    /**
     * Tries to charge this machine from the item in the given slot in this {@link #itemStorage}.
     */
    protected void attemptChargeFromStack(int slot) {
        if (this.capacitor().getAmount() >= this.capacitor().getCapacity()) return;

        EnergyStorage energyStorage = ContainerItemContext.ofSingleSlot(this.itemStorage().getSlot(slot)).find(EnergyStorage.ITEM);
        if (energyStorage.supportsExtraction()) {
            try (Transaction transaction = Transaction.openOuter()) {
                EnergyStorageUtil.move(energyStorage, this.capacitor, Math.min(Long.MAX_VALUE, this.getBatteryTransferRate()), transaction);
                transaction.commit();
            }
        }
    }

    /**
     * Tries to drain some of this machine's power into the item in the given slot in this {@link #itemStorage}.
     *
     * @param slot The slot id of the item
     */
    protected void attemptDrainPowerToStack(int slot) {
        EnergyStorage energyStorage = ContainerItemContext.ofSingleSlot(this.itemStorage().getSlot(slot)).find(EnergyStorage.ITEM);
        if (energyStorage.supportsInsertion()) {
            try (Transaction transaction = Transaction.openOuter()) {
                EnergyStorageUtil.move(this.capacitor, energyStorage, Math.min(Long.MAX_VALUE, this.getBatteryTransferRate()), transaction);
                transaction.commit();
            }
        }
    }

    /**
     * Returns a list of non-configurable machine faces.
     * @return a list of non-configurable machine faces.
     */
    public List<BlockFace> getLockedFaces() {
        return Collections.emptyList();
    }

    public void sync() {
        ((ServerWorld) world).getChunkManager().markForUpdate(getPos());
        BlockState state = this.getCachedState();
        this.world.setBlockState(this.pos, state.with(MachineBlock.ARBITRARY_BOOLEAN_PROPERTY, !state.get(MachineBlock.ARBITRARY_BOOLEAN_PROPERTY)), 11);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
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
