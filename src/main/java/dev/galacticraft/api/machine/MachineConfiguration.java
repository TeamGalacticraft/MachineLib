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

package dev.galacticraft.api.machine;

import dev.galacticraft.impl.machine.MachineConfigurationImpl;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public interface MachineConfiguration {
    @Contract(" -> new")
    static @NotNull MachineConfiguration create() {
        return new MachineConfigurationImpl();
    }

    void setStatus(@NotNull MachineStatus status);

    void setRedstoneActivation(@NotNull RedstoneActivation redstone);

    @NotNull MachineIOConfig getIOConfiguration();

    @NotNull SecuritySettings getSecurity();

    @NotNull MachineStatus getStatus();

    @NotNull RedstoneActivation getRedstoneActivation();

    @NotNull NbtCompound writeNbt(@NotNull NbtCompound nbt);

    void readNbt(@NotNull NbtCompound nbt);
}
