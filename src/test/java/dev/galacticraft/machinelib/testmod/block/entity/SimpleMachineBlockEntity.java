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

package dev.galacticraft.machinelib.testmod.block.entity;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.api.screen.SimpleMachineMenu;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import dev.galacticraft.machinelib.testmod.TestMod;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleMachineBlockEntity extends MachineBlockEntity {
    private int ticks = -1;

    public SimpleMachineBlockEntity(@NotNull BlockPos pos, BlockState state) {
        super(TestMod.SIMPLE_MACHINE_BE_TYPE, pos, state);
    }

    @Override
    protected @NotNull MachineItemStorage createItemStorage() {
        return MachineItemStorage.builder()
                .addSlot(TestMod.CHARGE_SLOT, TestMod.ANY_ITEM, true, ItemSlotDisplay.create(32, 32))
                .addSlot(TestMod.NO_DIAMOND_SLOT, TestMod.NO_DIAMONDS, true, ItemSlotDisplay.create(64, 64))
                .build();
    }

    @Override
    protected @NotNull MachineFluidStorage createFluidStorage() {
        return MachineFluidStorage.builder()
                .addTank(TestMod.ANY_FLUID_SLOT, FluidConstants.BUCKET, TestMod.ANY_FLUID, true, TankDisplay.create(12, 8), false)
                .build();
    }

    @Override
    public boolean canExposedInsertEnergy() {
        return true;
    }

    @Override
    public long getEnergyCapacity() {
        return 50_000;
    }

    @Override
    protected void tickConstant(@NotNull ServerLevel world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        super.tickConstant(world, pos, state, profiler);
        this.attemptChargeFromStack(0);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return SimpleMachineMenu.create(syncId, player, this, TestMod.SIMPLE_MACHINE_SH_TYPE);
    }

    @Override
    protected @NotNull MachineStatus tick(@NotNull ServerLevel world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        if (ticks > 0 && world.getBlockState(pos.above()).isAir()) {
            ticks--;
            profiler.push("transaction");
            try (Transaction transaction = Transaction.openOuter()){
                if (this.energyStorage().extract(100, transaction) == 100) {
                    transaction.commit();
                    if (ticks == 0) {
                        world.setBlockAndUpdate(pos.above(), Blocks.DRIED_KELP_BLOCK.defaultBlockState());
                    }
                    return TestMod.WORKING;
                } else {
                    ticks = -1;
                    return MachineStatuses.NOT_ENOUGH_ENERGY;
                }
            } finally {
                profiler.pop();
            }
        } else {
            if (!this.energyStorage().isEmpty() && world.getBlockState(pos.above()).isAir()) {
                ticks = 20*20;
                return TestMod.WORKING;
            } else {
                return MachineStatuses.NOT_ENOUGH_ENERGY;
            }
        }
    }
}
