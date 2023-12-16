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

import dev.galacticraft.machinelib.api.machine.MachineState;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.machine.configuration.RedstoneActivation;
import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.menu.sync.MachineStateSyncHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MachineStateImpl implements MachineState {
    private final @NotNull MachineType<?, ?> type;
    private @Nullable MachineStatus status = null;
    private boolean powered = false;

    public MachineStateImpl(@NotNull MachineType<?, ?> type) {
        this.type = type;
    }

    @Override
    public @Nullable MachineStatus getStatus() {
        return this.status;
    }

    @Override
    public @NotNull Component getStatusText(@NotNull RedstoneActivation activation) {
        return activation.isActive(this.powered) ? this.status != null ? this.status.getText() : Component.translatable(Constant.TranslationKey.UNKNOWN_STATUS).withStyle(ChatFormatting.GRAY) : Component.translatable(Constant.TranslationKey.DISABLED).withStyle(ChatFormatting.RED);
    }

    @Override
    public void setStatus(@Nullable MachineStatus status) {
        this.status = status;
    }

    @Override
    public boolean isActive() {
        return this.status != null && this.status.getType().isActive();
    }

    @Override
    public @NotNull CompoundTag createTag() {
        CompoundTag tag = new CompoundTag();
        if (this.status == null) {
            tag.putByte(Constant.Nbt.STATUS, (byte) -1);
        } else {
            tag.putByte(Constant.Nbt.STATUS, (byte) this.type.statusDomain().indexOf(this.status));
        }
        tag.putBoolean(Constant.Nbt.POWERED, this.powered);
        return tag;
    }

    @Override
    public void writePacket(@NotNull FriendlyByteBuf buf) {
        if (this.status == null) {
            buf.writeByte(-1);
        } else {
            buf.writeByte(this.type.statusDomain().indexOf(this.status));
        }
        buf.writeBoolean(this.powered);
    }

    @Override
    public void readTag(@NotNull CompoundTag tag) {
        byte b = tag.getByte(Constant.Nbt.STATUS);
        if (b == -1) {
            this.status = null;
        } else {
            this.status = this.type.statusDomain().get(b);
        }
        this.powered = tag.getBoolean(Constant.Nbt.POWERED);
    }

    @Override
    public void readPacket(@NotNull FriendlyByteBuf buf) {
        byte b = buf.readByte();
        if (b == -1) {
            this.status = null;
        } else {
            this.status = this.type.statusDomain().get(b);
        }
        this.powered = buf.readBoolean();
    }

    @Override
    public boolean isPowered() {
        return this.powered;
    }

    @Override
    public void setPowered(boolean powered) {
        this.powered = powered;
    }

    @Override
    public @Nullable MenuSyncHandler createSyncHandler() {
        return new MachineStateSyncHandler(this, this.type);
    }
}
