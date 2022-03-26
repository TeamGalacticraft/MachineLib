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

package dev.galacticraft.api.machine.storage;

import dev.galacticraft.api.machine.storage.io.ConfiguredStorage;
import dev.galacticraft.api.machine.storage.io.ExposedCapacitor;
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.machine.storage.io.SlotType;
import dev.galacticraft.impl.machine.storage.MachineEnergyStorageImpl;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import team.reborn.energy.api.EnergyStorage;

public interface MachineEnergyStorage extends EnergyStorage, ConfiguredStorage {
    SlotType[] NO_SLOTS = new SlotType[0];

    @Contract("_, _, _ -> new")
    static @NotNull MachineEnergyStorage of(long energyCapacity, long insertion, long extraction) {
        return new MachineEnergyStorageImpl(energyCapacity, insertion, extraction);
    }

    void setEnergy(long amount, TransactionContext context);

    @ApiStatus.Internal
    void setEnergyUnsafe(long amount);

    @Override
    default @NotNull SlotType @NotNull [] getTypes() {
        return NO_SLOTS;
    }

    @NotNull ExposedCapacitor getExposedStorage(@NotNull ResourceFlow flow);

    @NotNull ExposedCapacitor view();

    @NotNull NbtElement writeNbt();

    void readNbt(@NotNull NbtElement nbt);
}
