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

import dev.galacticraft.api.block.ConfiguredMachineFace;
import dev.galacticraft.api.block.util.BlockFace;
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.machine.storage.io.ResourceType;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public class MachineIOConfig {
    private final ConfiguredMachineFace front;
    private final ConfiguredMachineFace back;
    private final ConfiguredMachineFace left;
    private final ConfiguredMachineFace right;
    private final ConfiguredMachineFace top;
    private final ConfiguredMachineFace bottom;

    public MachineIOConfig() {
        this.front = new ConfiguredMachineFace(ResourceType.NONE, ResourceFlow.BOTH);
        this.back = new ConfiguredMachineFace(ResourceType.NONE, ResourceFlow.BOTH);
        this.left = new ConfiguredMachineFace(ResourceType.NONE, ResourceFlow.BOTH);
        this.right = new ConfiguredMachineFace(ResourceType.NONE, ResourceFlow.BOTH);
        this.top = new ConfiguredMachineFace(ResourceType.NONE, ResourceFlow.BOTH);
        this.bottom = new ConfiguredMachineFace(ResourceType.NONE, ResourceFlow.BOTH);
    }

    public NbtCompound toTag(NbtCompound nbt) {
        nbt.put("Front", this.front.toTag(new NbtCompound()));
        nbt.put("Back", this.back.toTag(new NbtCompound()));
        nbt.put("Left", this.left.toTag(new NbtCompound()));
        nbt.put("Right", this.right.toTag(new NbtCompound()));
        nbt.put("Top", this.top.toTag(new NbtCompound()));
        nbt.put("Bottom", this.bottom.toTag(new NbtCompound()));
        return nbt;
    }

    public void fromTag(NbtCompound nbt) {
        this.front.fromTag(nbt.getCompound("Front"));
        this.back.fromTag(nbt.getCompound("Back"));
        this.left.fromTag(nbt.getCompound("Left"));
        this.right.fromTag(nbt.getCompound("Right"));
        this.top.fromTag(nbt.getCompound("Top"));
        this.bottom.fromTag(nbt.getCompound("Bottom"));
    }

    /**
     * Please do not modify the returned {@link ConfiguredMachineFace}
     *
     * @param face the block face to pull the option from
     * @return a {@link ConfiguredMachineFace} assignd to the given face.
     */
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
