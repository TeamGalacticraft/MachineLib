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

package dev.galacticraft.machinelib.api.machine;

import dev.galacticraft.machinelib.api.storage.Deserializable;
import dev.galacticraft.machinelib.impl.machine.MachineConfigurationImpl;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A class that holds the configuration of a machine.
 * Can be used to save and load the configuration of a machine.
 */
public interface MachineConfiguration extends Deserializable<CompoundTag> {
    /**
     * Creates a new configuration.
     *
     * @return a new configuration.
     */
    @Contract(" -> new")
    static @NotNull MachineConfiguration create() {
        return new MachineConfigurationImpl();
    }

    /**
     * Returns the I/O configuration of the machine.
     *
     * @return The I/O configuration of the machine.
     */
    @Contract(pure = true)
    @NotNull MachineIOConfig getIOConfiguration();

    /**
     * Returns the security configuration of the machine.
     *
     * @return The security configuration of the machine.
     */
    @Contract(pure = true)
    @NotNull SecuritySettings getSecurity();

    /**
     * Returns the status of the machine.
     *
     * @return The status of the machine.
     */
    @Contract(pure = true)
    @NotNull MachineStatus getStatus();

    /**
     * Sets the status of the machine.
     *
     * @param status The status of the machine.
     */
    @Contract(mutates = "this")
    void setStatus(@NotNull MachineStatus status);

    /**
     * Returns the redstone activation of the machine.
     *
     * @return The redstone activation of the machine.
     */
    @Contract(pure = true)
    @NotNull RedstoneActivation getRedstoneActivation();

    /**
     * Sets the redstone activation of the machine.
     *
     * @param redstone The redstone activation of the machine.
     */
    @Contract(mutates = "this")
    void setRedstoneActivation(@NotNull RedstoneActivation redstone);
}
