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

package dev.galacticraft.machinelib.impl.machine;

import dev.galacticraft.machinelib.api.machine.*;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.impl.Constant;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@ApiStatus.Internal
public final class MachineConfigurationImpl implements MachineConfiguration {
    private MachineStatus status = MachineStatus.INVALID;
    private RedstoneActivation redstone = RedstoneActivation.IGNORE;
    private final MachineIOConfig configuration = MachineIOConfig.create();
    private final SecuritySettings security = new SecuritySettingsImpl();

    @Override
    public void setStatus(@NotNull MachineStatus status) {
        this.status = status;
    }

    @Override
    public void setRedstoneActivation(@NotNull RedstoneActivation redstone) {
        this.redstone = redstone;
    }

    @Override
    public @NotNull MachineIOConfig getIOConfiguration() {
        return this.configuration;
    }

    @Override
    public @NotNull SecuritySettings getSecurity() {
        return this.security;
    }

    @Override
    public @NotNull MachineStatus getStatus() {
        return this.status;
    }

    @Override
    public @NotNull RedstoneActivation getRedstoneActivation() {
        return this.redstone;
    }

    @Override
    public @NotNull CompoundTag writeNbt(@NotNull CompoundTag nbt, @NotNull SlotGroup @Nullable [] groups) {
        nbt.put(Constant.Nbt.SECURITY, this.getSecurity().toNbt());
        nbt.put(Constant.Nbt.CONFIGURATION, this.getIOConfiguration().writeNbt(groups));
        nbt.put(Constant.Nbt.REDSTONE_ACTIVATION, this.getRedstoneActivation().writeNbt());
        return nbt;
    }

    @Override
    public void readNbt(@NotNull CompoundTag nbt, @NotNull SlotGroup @Nullable [] groups) {
        this.getSecurity().fromNbt(nbt.getCompound(Constant.Nbt.SECURITY));
        this.getIOConfiguration().readNbt(nbt.getCompound(Constant.Nbt.CONFIGURATION), groups);
        this.setRedstoneActivation(RedstoneActivation.readNbt(Objects.requireNonNull(nbt.get(Constant.Nbt.REDSTONE_ACTIVATION))));
    }
}
