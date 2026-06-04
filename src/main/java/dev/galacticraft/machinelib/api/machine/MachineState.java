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

package dev.galacticraft.machinelib.api.machine;

import dev.galacticraft.machinelib.api.machine.configuration.RedstoneMode;
import dev.galacticraft.machinelib.api.misc.DeltaPacketSerializable;
import dev.galacticraft.machinelib.api.misc.Serializable;
import dev.galacticraft.machinelib.impl.Constant;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ByteTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Stores the state of a machine.
 */
public class MachineState implements Serializable<ByteTag>, DeltaPacketSerializable<RegistryFriendlyByteBuf, MachineState> {
    /**
     * The current status of the machine.
     */
    protected @Nullable MachineStatus status = null;
    /**
     * The current redstone power state of the machine.
     */
    protected boolean powered = false;

    public MachineState() {
    }

    /**
     * {@return the current status of the machine} Can be null if not set.
     */
    public @Nullable MachineStatus getStatus() {
        return this.status;
    }

    /**
     * Sets the status of the machine.
     *
     * @param status the status to set for the machine.
     */
    public void setStatus(@Nullable MachineStatus status) {
        this.status = status;
    }

    @Override
    public @NotNull ByteTag createTag() {
        return ByteTag.valueOf(this.powered);
    }

    @Override
    public void readTag(@NotNull ByteTag tag) {
        this.powered = tag.getAsByte() != 0;
    }

    @Override
    public void writePacket(@NotNull RegistryFriendlyByteBuf buf) {
        MachineStatus.writePacket(this.status, buf);
        buf.writeBoolean(this.powered);
    }

    @Override
    public boolean hasChanged(MachineState previous) {
        return !Objects.equals(previous.status, this.status) || previous.powered != this.powered;
    }

    @Override
    public void copyInto(MachineState other) {
        other.status = this.status;
        other.powered = this.powered;
    }

    @Override
    public void readPacket(@NotNull RegistryFriendlyByteBuf buf) {
        this.status = MachineStatus.readPacket(buf);
        this.powered = buf.readBoolean();
    }

    /**
     * {@return the name of the current status of the machine} Can be 'Disabled' if the machine is currently disabled.
     */
    public @NotNull Component getStatusText(@NotNull RedstoneMode activation) {
        return activation.isActive(this.powered) ? this.status != null ? this.status.getText() : Component.translatable(Constant.TranslationKey.UNKNOWN_STATUS).withStyle(ChatFormatting.GRAY) : Component.translatable(Constant.TranslationKey.DISABLED).withStyle(ChatFormatting.RED);
    }

    /**
     * Checks if the machine is currently active (status-wise).
     *
     * @return {@code true} if the machine is active, {@code false} otherwise.
     */
    public boolean isActive() {
        return this.status != null && this.status.getType().isActive();
    }

    /**
     * Checks if the machine is currently receiving redstone signal.
     *
     * @return {@code true} if the machine is receiving redstone signal, {@code false} otherwise.
     */
    public boolean isPowered() {
        return this.powered;
    }

    /**
     * Sets the redstone power state.
     *
     * @param powered the new redstone power state
     */
    @ApiStatus.Internal
    public void setPowered(boolean powered) {
        this.powered = powered;
    }

    @Override
    public MachineState createEquivalent() {
        return new MachineState();
    }
}
