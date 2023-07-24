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
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.testmod.block.TestModMachineTypes;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class GeneratorBlockEntity extends MachineBlockEntity {
    public static final int BATTERY_SLOT = 0;
    public static final int FUEL_SLOT = 1;

    public static final int GENERATION_RATE = 250;
    private final ItemResourceSlot fuelInput;
    private int burnTime = 0;


    public GeneratorBlockEntity(@NotNull BlockPos pos, BlockState state) {
        super(TestModMachineTypes.GENERATOR, pos, state);
        this.fuelInput = this.itemStorage().getSlot(FUEL_SLOT);

    }

    @Override
    protected void tickConstant(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        super.tickConstant(level, pos, state, profiler);
        profiler.push("power_drain");
        this.drainPowerToStack(BATTERY_SLOT);
        profiler.pop();
        this.trySpreadEnergy(level, state);
    }

    @Override
    protected @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        if (this.burnTime > 0) {
            if (this.energyStorage().isFull()) return MachineStatuses.OUTPUT_FULL;
            this.energyStorage().insert(GENERATION_RATE);
            this.burnTime--;
        }
        if (!this.energyStorage().isFull()) {
            if (this.burnTime == 0) {
                Item item = this.fuelInput.consumeOne();
                if (item != null) {
                    Integer time = FuelRegistry.INSTANCE.get(item);
                    if (time == null) return MachineStatuses.IDLE;
                    this.burnTime = time;
                } else {
                    return MachineStatuses.IDLE;
                }
            }
        } else {
            return MachineStatuses.IDLE;
        }
        return MachineStatuses.ACTIVE;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new MachineMenu<>(syncId, ((ServerPlayer) player), this);
    }
}
