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

import dev.galacticraft.machinelib.api.block.face.BlockFace;
import dev.galacticraft.machinelib.api.block.face.ConfiguredMachineFace;
import dev.galacticraft.machinelib.api.machine.MachineIOConfig;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class MachineIOConfigImpl implements MachineIOConfig {
    private final @NotNull ConfiguredMachineFace front = ConfiguredMachineFace.create();
    private final @NotNull ConfiguredMachineFace back = ConfiguredMachineFace.create();
    private final @NotNull ConfiguredMachineFace left = ConfiguredMachineFace.create();
    private final @NotNull ConfiguredMachineFace right = ConfiguredMachineFace.create();
    private final @NotNull ConfiguredMachineFace top = ConfiguredMachineFace.create();
    private final @NotNull ConfiguredMachineFace bottom = ConfiguredMachineFace.create();

    @Override
    public @NotNull CompoundTag writeNbt(@NotNull SlotGroup @NotNull [] groups) {
        CompoundTag nbt = new CompoundTag();
        nbt.put("Front", this.front.writeNbt(groups));
        nbt.put("Back", this.back.writeNbt(groups));
        nbt.put("Left", this.left.writeNbt(groups));
        nbt.put("Right", this.right.writeNbt(groups));
        nbt.put("Top", this.top.writeNbt(groups));
        nbt.put("Bottom", this.bottom.writeNbt(groups));
        return nbt;
    }

    @Override
    public void readNbt(@NotNull CompoundTag nbt, @NotNull SlotGroup @Nullable [] groups) {
        this.front.readNbt(nbt.getCompound("Front"), groups);
        this.back.readNbt(nbt.getCompound("Back"), groups);
        this.left.readNbt(nbt.getCompound("Left"), groups);
        this.right.readNbt(nbt.getCompound("Right"), groups);
        this.top.readNbt(nbt.getCompound("Top"), groups);
        this.bottom.readNbt(nbt.getCompound("Bottom"), groups);
    }

    @Override
    public @NotNull ConfiguredMachineFace get(@NotNull BlockFace face) {
        return switch (face) {
            case FRONT -> this.front;
            case TOP -> this.top;
            case BACK -> this.back;
            case RIGHT -> this.right;
            case LEFT -> this.left;
            case BOTTOM -> this.bottom;
        };
    }
}
