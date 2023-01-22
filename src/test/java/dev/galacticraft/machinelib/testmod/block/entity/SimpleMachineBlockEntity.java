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

package dev.galacticraft.machinelib.testmod.block.entity;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.menu.SimpleMachineMenu;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupTypes;
import dev.galacticraft.machinelib.testmod.block.TestModMachineTypes;
import dev.galacticraft.machinelib.testmod.slot.TestModSlotGroupTypes;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleMachineBlockEntity extends MachineBlockEntity {
    private int ticks = -1;

    public SimpleMachineBlockEntity(@NotNull BlockPos pos, BlockState state) {
        super(TestModMachineTypes.SIMPLE_MACHINE, pos, state);
    }

    @Override
    protected void tickConstant(@NotNull ServerLevel world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        super.tickConstant(world, pos, state, profiler);
        profiler.push("charge_stack");
        this.attemptChargeFromStack(SlotGroupTypes.CHARGE, 0);
        profiler.pop();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new SimpleMachineMenu<>(syncId, ((ServerPlayer) player), this, (MachineType<MachineBlockEntity, ? extends MachineMenu<MachineBlockEntity>>) this.getMachineType());
    }

    @Override
    protected @NotNull MachineStatus tick(@NotNull ServerLevel world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        if (this.ticks > 0) {
            this.ticks--;
            profiler.push("check");
            if (this.itemStorage().getGroup(TestModSlotGroupTypes.DIRT).containsAny(Items.DIRT)) {
                if (this.itemStorage().getGroup(TestModSlotGroupTypes.DIAMONDS).canInsertOne(Items.DIAMOND)) {
                    profiler.popPush("transaction");
                    try (Transaction transaction = Transaction.openOuter()) {
                        if (this.energyStorage().extract(150, transaction) == 150) {
                            transaction.commit();
                            if (this.ticks == 0) {
                                world.setBlockAndUpdate(pos.above(), Blocks.DRIED_KELP_BLOCK.defaultBlockState());
                            }
                            return MachineStatuses.ACTIVE;
                        } else {
                            this.ticks = -1;
                            return MachineStatuses.NOT_ENOUGH_ENERGY;
                        }
                    } finally {
                        profiler.pop();
                    }
                } else {
                    profiler.pop();
                    this.ticks = -1;
                    return MachineStatuses.OUTPUT_FULL;
                }
            } else {
                profiler.pop();
                this.ticks = -1;
                return MachineStatuses.INVALID_RECIPE;
            }
        } else {
            if (this.energyStorage().getAmount() > 150) {
                if (this.itemStorage().getGroup(TestModSlotGroupTypes.DIRT).containsAny(Items.DIRT)) {
                    if (this.itemStorage().getGroup(TestModSlotGroupTypes.DIAMONDS).canInsertOne(Items.DIAMOND)) {
                        this.ticks = 5 * 20;
                        return MachineStatuses.ACTIVE;
                    } else {
                        return MachineStatuses.OUTPUT_FULL;
                    }
                } else {
                    return MachineStatuses.INVALID_RECIPE;
                }
            } else {
                return MachineStatuses.NOT_ENOUGH_ENERGY;
            }
        }
    }
}
