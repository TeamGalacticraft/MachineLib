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

package dev.galacticraft.machinelib.testmod.block.entity;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.testmod.block.TestModMachineTypes;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MixerBlockEntity extends MachineBlockEntity {
    public static final int BATTERY_SLOT = 0;
    public static final int WATER_INPUT_SLOT = 1;
    public static final int LAVA_INPUT_SLOT = 2;
    public static final int OUTPUT_SLOT = 3;

    public static final int WATER_TANK = 0;
    public static final int LAVA_TANK = 1;

    public static final long FLUID_REQUIRED = FluidConstants.BUCKET / 2;
    public static final int ENERGY_USAGE = 50;
    public static final int PROCESS_TIME = 15 * 20;

    private int progress = 0;

    public MixerBlockEntity(@NotNull BlockPos pos, BlockState state) {
        super(TestModMachineTypes.MIXER, pos, state);
    }

    @Override
    protected void tickConstant(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        super.tickConstant(level, pos, state, profiler);
        profiler.push("charge_stack");
        this.chargeFromStack(BATTERY_SLOT);
        this.takeFluidFromStack(WATER_INPUT_SLOT, WATER_TANK, Fluids.WATER);
        this.takeFluidFromStack(LAVA_INPUT_SLOT, LAVA_TANK, Fluids.LAVA);
        profiler.pop();
    }

    @Override
    protected @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        ItemResourceSlot output = this.itemStorage().getSlot(OUTPUT_SLOT);
        if (!output.canInsert(Items.OBSIDIAN)) {
            this.progress = 0;
            return MachineStatuses.OUTPUT_FULL;
        }
        FluidResourceSlot water = this.fluidStorage().getSlot(WATER_TANK);
        FluidResourceSlot lava = this.fluidStorage().getSlot(LAVA_TANK);
        if (water.tryExtract(Fluids.WATER, FLUID_REQUIRED) != FLUID_REQUIRED || lava.tryExtract(Fluids.LAVA, FLUID_REQUIRED) != FLUID_REQUIRED) {
            this.progress = 0;
            return MachineStatuses.IDLE;
        }

        if (!this.energyStorage().extractExact(ENERGY_USAGE)) {
            this.progress = 0;
            return MachineStatuses.NOT_ENOUGH_ENERGY; //todo
        }

        if (++this.progress == PROCESS_TIME) {
            water.extract(Fluids.WATER, FLUID_REQUIRED);
            lava.extract(Fluids.WATER, FLUID_REQUIRED);
            output.insert(Items.OBSIDIAN, 1);
            this.progress = 0;
        }
        return MachineStatuses.ACTIVE;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new MachineMenu<>(syncId, ((ServerPlayer) player), this);
    }
}
