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

package dev.galacticraft.machinelib.impl.machine;

import dev.galacticraft.machinelib.api.machine.*;
import dev.galacticraft.machinelib.impl.Constant;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@ApiStatus.Internal
public final class MachineConfigurationImpl implements MachineConfiguration {
    private final MachineIOConfig configuration = MachineIOConfig.create();
    private final SecuritySettings security = new SecuritySettingsImpl();
    private MachineStatus status = MachineStatus.INVALID;
    private RedstoneActivation redstone = RedstoneActivation.IGNORE;

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
    public void setStatus(@NotNull MachineStatus status) {
        this.status = status;
    }

    @Override
    public @NotNull RedstoneActivation getRedstoneActivation() {
        return this.redstone;
    }

    @Override
    public void setRedstoneActivation(@NotNull RedstoneActivation redstone) {
        this.redstone = redstone;
    }

    @Override
    public @NotNull CompoundTag createTag() {
        CompoundTag tag = new CompoundTag();
        tag.put(Constant.Nbt.SECURITY, this.security.createTag());
        tag.put(Constant.Nbt.CONFIGURATION, this.configuration.createTag());
        tag.put(Constant.Nbt.REDSTONE_ACTIVATION, this.redstone.createTag());
        return tag;
    }

    @Override
    public void readTag(@NotNull CompoundTag tag) {
        this.security.readTag(tag.getCompound(Constant.Nbt.SECURITY));
        this.configuration.readTag(tag.getCompound(Constant.Nbt.CONFIGURATION));
        this.redstone = RedstoneActivation.readTag(Objects.requireNonNull((ByteTag) tag.get(Constant.Nbt.REDSTONE_ACTIVATION)));
    }

    @Override
    public void writePacket(@NotNull FriendlyByteBuf buf) {
        this.security.writePacket(buf);
        this.configuration.writePacket(buf);
        this.redstone.writePacket(buf);
    }

    @Override
    public void readPacket(@NotNull FriendlyByteBuf buf) {
        this.security.readPacket(buf);
        this.configuration.readPacket(buf);
        this.redstone = RedstoneActivation.readPacket(buf);
    }
}
