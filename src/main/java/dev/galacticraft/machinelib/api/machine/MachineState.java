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

package dev.galacticraft.machinelib.api.machine;

import dev.galacticraft.machinelib.api.machine.configuration.RedstoneActivation;
import dev.galacticraft.machinelib.api.menu.sync.MenuSynchronizable;
import dev.galacticraft.machinelib.api.misc.Deserializable;
import dev.galacticraft.machinelib.impl.machine.MachineStateImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stores the state of a machine.
 */
public interface MachineState extends MenuSynchronizable, Deserializable<CompoundTag> {
    /**
     * Creates a new state instance of the specified type.
     *
     * @return a new MachineState instance
     */
    static @NotNull MachineState create() {
        return new MachineStateImpl();
    }

    /**
     * Returns the current status of the machine. Can be null if not set.
     *
     * @return the current status of the machine.
     */
    @Nullable MachineStatus getStatus();

    /**
     * Returns the name of the current status of the machine or 'disabled' if the machine is currently disabled.
     *
     * @return the current status of the machine.
     */
    @NotNull Component getStatusText(@NotNull RedstoneActivation activation);

    /**
     * Sets the status of the machine.
     *
     * @param status the status to set for the machine.
     */
    void setStatus(@Nullable MachineStatus status);

    /**
     * Checks if the machine is currently active (status-wise).
     *
     * @return true if the machine is active, false otherwise.
     */
    boolean isActive();

    /**
     * Checks if the machine is currently receiving redstone signal.
     *
     * @return true if the machine is receiving redstone signal, false otherwise.
     */
    boolean isPowered();

    /**
     * Sets the redstone power state.
     *
     * @param powered the new redstone power state
     */
    @ApiStatus.Internal
    void setPowered(boolean powered);
}
