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
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.StorageSpec;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import dev.galacticraft.machinelib.api.util.EnergySource;
import dev.galacticraft.machinelib.testmod.menu.TestModMenuTypes;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class GeneratorBlockEntity extends MachineBlockEntity {
    public static final int BATTERY_SLOT = 0;
    public static final int FUEL_SLOT = 1;

    public static final int GENERATION_RATE = 250;

    public static final StorageSpec STORAGE_SPEC = StorageSpec.of(
            MachineItemStorage.spec(
                    ItemResourceSlot.builder(TransferType.PROCESSING)
                            .pos(8, 62)
                            .filter(ResourceFilters.CAN_INSERT_ENERGY)
                            .capacity(32),
                    ItemResourceSlot.builder(TransferType.INPUT)
                            .pos(80, 49)
                            .filter((item, tag) -> {
                                Integer time = FuelRegistry.INSTANCE.get(item);
                                return time != null && time > 0;
                            })
            ),
            MachineEnergyStorage.spec(30000, 0, GENERATION_RATE + GENERATION_RATE / 2)
    );
    private final ItemResourceSlot fuelInput;
    private final EnergySource energySource = new EnergySource(this);
    private int burnTime = 0;

    public GeneratorBlockEntity(@NotNull BlockPos pos, BlockState state) {
        super(TestModBlockEntityTypes.GENERATOR, pos, state, STORAGE_SPEC);
        this.fuelInput = this.itemStorage().slot(FUEL_SLOT);
    }

    @Override
    protected void tickConstant(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        super.tickConstant(level, pos, state, profiler);
        profiler.push("power_drain");
        this.drainPowerToSlot(BATTERY_SLOT);
        profiler.pop();
        this.energySource.trySpreadEnergy(level, pos, state);
    }

    @Override
    protected @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        if (!this.energyStorage().isFull()) {
            if (this.burnTime == 0) {
                Item item = this.fuelInput.consumeOne();
                if (item != null) {
                    Integer time = FuelRegistry.INSTANCE.get(item);
                    if (time == null) time = 0;
                    this.burnTime = time;
                }
            }
        }

        if (this.burnTime > 0) {
            this.energyStorage().insert(GENERATION_RATE);
            this.burnTime--;
        } else {
            return MachineStatuses.IDLE;
        }

        if (this.energyStorage().isFull()) return MachineStatuses.OUTPUT_FULL;
        return MachineStatuses.ACTIVE;
    }

    @Override
    public @Nullable MachineMenu<GeneratorBlockEntity> createMenu(int syncId, Inventory inventory, Player player) {
        return new MachineMenu<>(TestModMenuTypes.GENERATOR, syncId, player, this);
    }
}
