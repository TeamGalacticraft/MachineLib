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

import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.configuration.IOFace;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.StorageSpec;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.api.util.StorageHelper;
import dev.galacticraft.machinelib.impl.Constant;
import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
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
 * This class handles three different types of storage and IO configurations:
 * {@link MachineEnergyStorage energy}, {@link MachineItemStorage item} and {@link MachineFluidStorage fluid} storage.
 */
public abstract class MachineBlockEntity extends ConfiguredBlockEntity implements RenderDataBlockEntity {
    private final @NotNull MachineItemStorage itemStorage;
    private final @NotNull MachineFluidStorage fluidStorage;
    private final @NotNull MachineEnergyStorage energyStorage;

    /**
     * Whether the machine will not drop items when broken.
     * <p>
     * Used for machines that are placed in structures to prevent players from collecting too many resources for free.
     * Set via NBT.
     *
     * @see Constant.Nbt#DISABLE_DROPS
     */
    @ApiStatus.Internal
    private boolean disableDrops = false;

    /**
     * Constructs a new machine block entity.
     *
     * @param type The type of block entity.
     * @param pos The position of the machine in the level.
     * @param state The block state of the machine.
     */
    protected MachineBlockEntity(BlockEntityType<? extends MachineBlockEntity> type,
                                 BlockPos pos,
                                 BlockState state,
                                 StorageSpec spec) {
        super(type, pos, state);

        this.itemStorage = spec.createItemStorage();
        this.fluidStorage = spec.createFluidStorage();
        this.energyStorage = spec.createEnergyStorage();

        this.itemStorage.setParent(this);
        this.fluidStorage.setParent(this);
        this.energyStorage.setParent(this);
    }

    public static <T extends MachineBlockEntity> void registerProviders(@NotNull BlockEntityType<? extends T> type) {
        EnergyStorage.SIDED.registerForBlockEntity((machine, direction) -> {
            if (direction == null) return machine.energyStorage().getExposedStorage(ResourceFlow.BOTH);
            IOFace ioFace = machine.getIOConfig().get(Objects.requireNonNull(BlockFace.from(machine.getBlockState(), direction)));
            return ioFace.getType().willAcceptResource(ResourceType.ENERGY) ? machine.energyStorage().getExposedStorage(ioFace.getFlow()) : null;
        }, type);
        ItemStorage.SIDED.registerForBlockEntity((machine, direction) -> {
            if (direction == null) return machine.itemStorage().getExposedStorage(ResourceFlow.BOTH);
            IOFace ioFace = machine.getIOConfig().get(Objects.requireNonNull(BlockFace.from(machine.getBlockState(), direction)));
            return ioFace.getType().willAcceptResource(ResourceType.ITEM) ? machine.itemStorage().getExposedStorage(ioFace.getFlow()) : null;
        }, type);
        FluidStorage.SIDED.registerForBlockEntity((machine, direction) -> {
            if (direction == null) return machine.fluidStorage().getExposedStorage(ResourceFlow.BOTH);
            IOFace ioFace = machine.getIOConfig().get(Objects.requireNonNull(BlockFace.from(machine.getBlockState(), direction)));
            return ioFace.getType().willAcceptResource(ResourceType.FLUID) ? machine.fluidStorage().getExposedStorage(ioFace.getFlow()) : null;
        }, type);
    }

    @SafeVarargs
    public static <T extends MachineBlockEntity> void registerProviders(BlockEntityType<? extends T> @NotNull ... types) {
        for (BlockEntityType<? extends T> type : types) {
            registerProviders(type);
        }
    }

    @Override
    protected abstract @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler);

    @Override
    public abstract @Nullable MachineMenu<? extends MachineBlockEntity> createMenu(int syncId, Inventory inventory, Player player);

    /**
     * {@return the item storage of this machine}
     */
    @Contract(pure = true)
    public final @NotNull MachineItemStorage itemStorage() {
        return this.itemStorage;
    }

    /**
     * {@return the fluid storage of this machine}
     */
    @Contract(pure = true)
    public final @NotNull MachineFluidStorage fluidStorage() {
        return this.fluidStorage;
    }

    /**
     * {@return the energy storage of this machine}
     */
    @Contract(pure = true)
    public final @NotNull MachineEnergyStorage energyStorage() {
        return this.energyStorage;
    }

    /**
     * Tries to extract energy from an item in the specified slot into this machine.
     *
     * @param slot the index of the input slot.
     */
    protected void chargeFromSlot(int slot) {
        if (this.energyStorage().isFull()) return;

        EnergyStorage energyStorage = this.itemStorage.slot(slot).find(EnergyStorage.ITEM);
        if (energyStorage != null && energyStorage.supportsExtraction()) {
            EnergyStorageUtil.move(energyStorage, this.energyStorage, this.energyStorage.externalInsertionRate(), null);
        }
    }

    /**
     * Tries to drain power from this machine's energy storage and insert it into the item in the given slot.
     *
     * @param slot the index of the input slot.
     */
    protected void drainPowerToSlot(int slot) {
        if (this.energyStorage().isEmpty()) return;
        EnergyStorage energyStorage = this.itemStorage.slot(slot).find(EnergyStorage.ITEM);
        if (energyStorage != null && energyStorage.supportsInsertion()) {
            EnergyStorageUtil.move(this.energyStorage, energyStorage, this.energyStorage.externalExtractionRate(), null);
        }
    }

    /**
     * Tries to extract the specified fluid from the item in the input slot
     * and move it into the tank of the fluid storage.
     *
     * @param inputSlot the index of the input slot from which the fluid will be extracted
     * @param tankSlot the index of the tank where the fluid will be moved to
     * @param fluid the fluid to be extracted and stored
     */
    protected void takeFluidFromSlot(int inputSlot, int tankSlot, @NotNull Fluid fluid) {
        FluidResourceSlot tank = this.fluidStorage().slot(tankSlot);
        if (tank.isFull()) return;
        ItemResourceSlot slot = this.itemStorage.slot(inputSlot);
        Storage<FluidVariant> storage = slot.find(FluidStorage.ITEM);
        if (storage != null && storage.supportsExtraction()) {
            StorageHelper.move(FluidVariant.of(fluid), storage, tank, Integer.MAX_VALUE, null);
        }
    }

    /**
     * Tries to extract fluids from the item in the input slot that match the slot's filter
     * and moves them into the tank of the fluid storage.
     *
     * @param inputSlot the index of the input slot from which the fluid will be extracted
     * @param tankSlot the index of the tank where the fluid will be moved to
     */
    protected void takeFluidFromSlot(int inputSlot, int tankSlot) {
        FluidResourceSlot tank = this.fluidStorage().slot(tankSlot);
        if (tank.isFull()) return;
        ItemResourceSlot slot = this.itemStorage.slot(inputSlot);
        Storage<FluidVariant> storage = slot.find(FluidStorage.ITEM);
        if (storage != null && storage.supportsExtraction()) {
            StorageHelper.move(storage, tank, Integer.MAX_VALUE, null);
        }
    }

    /**
     * Tries to extract fluids from the fluid storage and moves them into the tank of the item in the input slot.
     *
     * @param inputSlot the index of the input slot where the fluid will be inserted
     * @param tankSlot the index of the tank from which the fluid will be extracted
     */
    protected void drainFluidToSlot(int inputSlot, int tankSlot) {
        FluidResourceSlot tank = this.fluidStorage().slot(tankSlot);
        if (tank.isEmpty()) return;

        ItemResourceSlot slot = this.itemStorage.slot(inputSlot);
        Storage<FluidVariant> storage = slot.find(FluidStorage.ITEM);
        if (storage != null && storage.supportsInsertion()) {
            StorageHelper.move(tank, storage, Integer.MAX_VALUE, null);
        }
    }

    /**
     * {@return whether this machine will drop items when broken}
     */
    @Contract(pure = true)
    public boolean areDropsDisabled() {
        return this.disableDrops;
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
        tag.putBoolean(Constant.Nbt.DISABLE_DROPS, this.disableDrops);

        if (this.itemStorage.size() > 0) {
            tag.put(Constant.Nbt.ITEM_STORAGE, this.itemStorage.createTag());
        }
        if (this.fluidStorage.size() > 0) {
            tag.put(Constant.Nbt.FLUID_STORAGE, this.fluidStorage.createTag());
        }
        if (this.energyStorage.getCapacity() > 0) {
            tag.put(Constant.Nbt.ENERGY_STORAGE, this.energyStorage.createTag());
        }
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
        if (tag.contains(Constant.Nbt.ITEM_STORAGE, Tag.TAG_LIST))
            this.itemStorage.readTag(Objects.requireNonNull(tag.getList(Constant.Nbt.ITEM_STORAGE, Tag.TAG_COMPOUND)));
        if (tag.contains(Constant.Nbt.FLUID_STORAGE, Tag.TAG_LIST))
            this.fluidStorage.readTag(Objects.requireNonNull(tag.getList(Constant.Nbt.FLUID_STORAGE, Tag.TAG_COMPOUND)));
        if (tag.contains(Constant.Nbt.ENERGY_STORAGE, Tag.TAG_LONG))
            this.energyStorage.readTag((LongTag) tag.get(Constant.Nbt.ENERGY_STORAGE));

        this.disableDrops = tag.getBoolean(Constant.Nbt.DISABLE_DROPS);

        if (this.level != null && this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, Blocks.AIR.defaultBlockState(), this.getBlockState(), Block.UPDATE_IMMEDIATE);
        }
    }
}
