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

package dev.galacticraft.machinelib.testmod.block.entity;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.filter.ResourceFilters;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.StorageSpec;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import dev.galacticraft.machinelib.api.util.FluidSource;
import dev.galacticraft.machinelib.testmod.menu.TestModMenuTypes;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MelterBlockEntity extends MachineBlockEntity {
    public static final int BATTERY_SLOT = 0;
    public static final int INPUT_SLOT = 1;
    public static final int LAVA_OUTPUT_SLOT = 2;
    public static final int LAVA_TANK = 0;

    public static final long GENERATION_RATE = (FluidConstants.BUCKET / 4) * 3; // 75% efficiency
    public static final long ENERGY_USAGE = 250;
    public static final int PROCESSING_TIME = 200;

    private static final StorageSpec STORAGE_SPEC = StorageSpec.of(
            MachineItemStorage.spec(
                    ItemResourceSlot.builder(TransferType.TRANSFER)
                            .pos(8, 8)
                            .filter(ResourceFilters.CAN_EXTRACT_ENERGY)
                            .capacity(32),
                    ItemResourceSlot.builder(TransferType.INPUT)
                            .pos(59, 42)
                            .filter(ResourceFilters.ofResource(Items.COBBLESTONE)),
                    ItemResourceSlot.builder(TransferType.PROCESSING)
                            .pos(152, 62)
                            .filter(ResourceFilters.canInsertFluid(Fluids.LAVA))
            ),
            MachineEnergyStorage.spec(30000, ENERGY_USAGE * 2, 0),
            MachineFluidStorage.spec(FluidResourceSlot.builder(TransferType.OUTPUT)
                    .pos(152, 8)
                    .capacity(FluidConstants.BUCKET * 16)
                    .filter(ResourceFilters.ofResource(Fluids.LAVA))
            )
    );

    private final ItemResourceSlot itemInput;
    private final FluidResourceSlot fluidOutput;
    private final FluidSource fluidSource = new FluidSource(this);

    private int progress = 0;

    public MelterBlockEntity(@NotNull BlockPos pos, BlockState state) {
        super(TestModBlockEntityTypes.MELTER, pos, state, STORAGE_SPEC);
        this.itemInput = this.itemStorage().slot(INPUT_SLOT);
        this.fluidOutput = this.fluidStorage().slot(LAVA_TANK);
    }

    @Override
    protected void tickConstant(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        super.tickConstant(level, pos, state, profiler);
        profiler.push("charge_stack");
        this.chargeFromSlot(BATTERY_SLOT);
        this.drainFluidToSlot(LAVA_OUTPUT_SLOT, LAVA_TANK);
        profiler.pop();
        this.fluidSource.trySpreadFluids(level, pos, state);
    }

    @Override
    protected @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        Item resource = this.itemInput.getResource();
        if (resource == Items.COBBLESTONE) {
            if (this.fluidOutput.getAmount() + GENERATION_RATE <= this.fluidOutput.getCapacity()) {
                if (this.energyStorage().extractExact(ENERGY_USAGE)) {
                    if (++this.progress == PROCESSING_TIME) {
                        this.progress = 0;
                        this.itemInput.extractOne();
                        this.fluidOutput.insert(Fluids.LAVA, GENERATION_RATE);
                    }
                    return MachineStatuses.ACTIVE;
                } else {
                    if (this.progress > 0) this.progress--;
                    return MachineStatuses.NOT_ENOUGH_ENERGY;
                }
            } else {
                if (this.progress > 0) this.progress--;
                return MachineStatuses.OUTPUT_FULL;
            }
        } else {
            this.progress = 0;
            return MachineStatuses.IDLE;
        }
    }

    @Nullable
    @Override
    public MachineMenu<MelterBlockEntity> createMenu(int syncId, Inventory inv, Player player) {
        return new MachineMenu<>(TestModMenuTypes.MELTER, syncId, player, this);
    }
}
