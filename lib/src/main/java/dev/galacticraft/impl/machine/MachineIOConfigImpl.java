/*
 * Copyright (c) 2021-${year} ${company}
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

package dev.galacticraft.impl.machine;

import dev.galacticraft.api.block.ConfiguredMachineFace;
import dev.galacticraft.api.block.util.BlockFace;
import dev.galacticraft.api.machine.MachineIOConfig;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public class MachineIOConfigImpl implements MachineIOConfig {
    private final ConfiguredMachineFace front = ConfiguredMachineFace.create();
    private final ConfiguredMachineFace back = ConfiguredMachineFace.create();
    private final ConfiguredMachineFace left = ConfiguredMachineFace.create();
    private final ConfiguredMachineFace right = ConfiguredMachineFace.create();
    private final ConfiguredMachineFace top = ConfiguredMachineFace.create();
    private final ConfiguredMachineFace bottom = ConfiguredMachineFace.create();

    @Override
    public @NotNull NbtCompound writeNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.put("Front", this.front.writeNbt());
        nbt.put("Back", this.back.writeNbt());
        nbt.put("Left", this.left.writeNbt());
        nbt.put("Right", this.right.writeNbt());
        nbt.put("Top", this.top.writeNbt());
        nbt.put("Bottom", this.bottom.writeNbt());
        return nbt;
    }

    @Override
    public void readNbt(@NotNull NbtCompound nbt) {
        this.front.readNbt(nbt.getCompound("Front"));
        this.back.readNbt(nbt.getCompound("Back"));
        this.left.readNbt(nbt.getCompound("Left"));
        this.right.readNbt(nbt.getCompound("Right"));
        this.top.readNbt(nbt.getCompound("Top"));
        this.bottom.readNbt(nbt.getCompound("Bottom"));
    }

    @Override
    public ConfiguredMachineFace get(@NotNull BlockFace face) {
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
