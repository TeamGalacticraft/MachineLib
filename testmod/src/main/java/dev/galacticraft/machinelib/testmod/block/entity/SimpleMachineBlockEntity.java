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

import dev.galacticraft.api.block.entity.MachineBlockEntity;
import dev.galacticraft.api.machine.MachineStatus;
import dev.galacticraft.api.machine.MachineStatuses;
import dev.galacticraft.api.machine.storage.MachineItemStorage;
import dev.galacticraft.api.machine.storage.display.ItemSlotDisplay;
import dev.galacticraft.api.screen.SimpleMachineScreenHandler;
import dev.galacticraft.machinelib.testmod.TestMod;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.explosion.Explosion;
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
                .addSlot(TestMod.CHARGE_SLOT, new ItemSlotDisplay(32, 32))
                .build();
    }

    @Override
    public long getEnergyCapacity() {
        return 50_000;
    }

    @Override
    protected void tickConstant(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull BlockState state) {
        super.tickConstant(world, pos, state);
        this.attemptChargeFromStack(0);
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return SimpleMachineScreenHandler.create(syncId, player, this, TestMod.SIMPLE_MACHINE_SH_TYPE);
    }

    @Override
    protected @NotNull MachineStatus tick(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull BlockState state) {
        if (ticks > 0) {
            ticks--;
            world.getProfiler().push("transaction");
            try (Transaction transaction = Transaction.openOuter()){
                if (this.energyStorage().extract(100, transaction) == 100) {
                    transaction.commit();
                    if (ticks == 0) {
                        world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 1.0F, false, Explosion.DestructionType.BREAK);
                    }
                    return TestMod.WORKING;
                } else {
                    ticks = -1;
                    return MachineStatuses.NOT_ENOUGH_ENERGY;
                }
            } finally {
                world.getProfiler().pop();
            }
        } else {
            if (!this.energyStorage().isEmpty()) {
                ticks = 20*20;
                return TestMod.WORKING;
            } else {
                return MachineStatuses.NOT_ENOUGH_ENERGY;
            }
        }
    }
}
