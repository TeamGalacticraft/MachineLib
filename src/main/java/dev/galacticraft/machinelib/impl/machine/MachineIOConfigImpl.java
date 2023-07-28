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

import dev.galacticraft.machinelib.api.machine.configuration.MachineIOConfig;
import dev.galacticraft.machinelib.api.machine.configuration.MachineIOFace;
import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.impl.menu.sync.MachineIOConfigSyncHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class MachineIOConfigImpl implements MachineIOConfig {
    private final @NotNull MachineIOFace front = MachineIOFace.blank();
    private final @NotNull MachineIOFace back = MachineIOFace.blank();
    private final @NotNull MachineIOFace left = MachineIOFace.blank();
    private final @NotNull MachineIOFace right = MachineIOFace.blank();
    private final @NotNull MachineIOFace top = MachineIOFace.blank();
    private final @NotNull MachineIOFace bottom = MachineIOFace.blank();
    private final @NotNull MachineIOFace nullFace = MachineIOFace.directionless();

    @Override
    public @NotNull MachineIOFace get(@Nullable BlockFace face) {
        if (face == null) return this.nullFace;
        return switch (face) {
            case FRONT -> this.front;
            case TOP -> this.top;
            case BACK -> this.back;
            case RIGHT -> this.right;
            case LEFT -> this.left;
            case BOTTOM -> this.bottom;
//            null -> this.nullFace;
        };
    }

    @Override
    public @NotNull CompoundTag createTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("Front", this.front.createTag());
        nbt.put("Back", this.back.createTag());
        nbt.put("Left", this.left.createTag());
        nbt.put("Right", this.right.createTag());
        nbt.put("Top", this.top.createTag());
        nbt.put("Bottom", this.bottom.createTag());
        return nbt;
    }

    @Override
    public void readTag(@NotNull CompoundTag tag) {
        this.front.readTag(tag.getCompound("Front"));
        this.back.readTag(tag.getCompound("Back"));
        this.left.readTag(tag.getCompound("Left"));
        this.right.readTag(tag.getCompound("Right"));
        this.top.readTag(tag.getCompound("Top"));
        this.bottom.readTag(tag.getCompound("Bottom"));
    }

    @Override
    public void writePacket(@NotNull FriendlyByteBuf buf) {
        this.front.writePacket(buf);
        this.back.writePacket(buf);
        this.left.writePacket(buf);
        this.right.writePacket(buf);
        this.top.writePacket(buf);
        this.bottom.writePacket(buf);
    }

    @Override
    public void readPacket(@NotNull FriendlyByteBuf buf) {
        this.front.readPacket(buf);
        this.back.readPacket(buf);
        this.left.readPacket(buf);
        this.right.readPacket(buf);
        this.top.readPacket(buf);
        this.bottom.readPacket(buf);
    }

    @Contract(" -> new")
    @Override
    public @NotNull MenuSyncHandler createSyncHandler() {
        return new MachineIOConfigSyncHandler(this);
    }
}
