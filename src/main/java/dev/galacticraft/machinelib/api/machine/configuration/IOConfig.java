/*
 * Copyright (c) 2021-2025 Team Galacticraft
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

package dev.galacticraft.machinelib.api.machine.configuration;

import dev.galacticraft.machinelib.api.machine.MachineRenderData;
import dev.galacticraft.machinelib.api.misc.DeltaPacketSerializable;
import dev.galacticraft.machinelib.api.misc.PacketSerializable;
import dev.galacticraft.machinelib.api.misc.Serializable;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.api.util.BlockFace;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

/**
 * Stores the configuration of a machine's I/O for all six faces.
 */
public final class IOConfig implements Serializable<ByteArrayTag>, MachineRenderData, DeltaPacketSerializable<ByteBuf, IOConfig> {
    public static final StreamCodec<ByteBuf, IOConfig> CODEC = PacketSerializable.createCodec(IOConfig::new);

    /**
     * The I/O configuration for each face.
     */
    private final IOFace[] faces;

    /**
     * Creates a new I/O configuration with all faces blank.
     */
    public IOConfig() {
        this.faces = new IOFace[6];

        for (int i = 0; i < this.faces.length; i++) {
            this.faces[i] = new IOFace();
        }
    }

    /**
     * Creates a new I/O configuration with the given faces.
     *
     * @param faces the faces to use
     */
    public IOConfig(IOFace[] faces) {
        this.faces = faces;
    }

    /**
     * {@return the I/O configuration for the given face}
     *
     * @param face the block face to pull the option from
     */
    @NotNull
    public IOFace get(@NotNull BlockFace face) {
        return this.faces[face.ordinal()];
    }

    @Override
    public @NotNull ByteArrayTag createTag() {
        ByteArrayTag nbt = new ByteArrayTag(new byte[6]);
        for (int i = 0; i < this.faces.length; i++) {
            nbt.set(i, this.faces[i].createTag());
        }
        return nbt;
    }

    @Override
    public void readTag(@NotNull ByteArrayTag tag) {
        if (tag.size() == 6) {
            for (int i = 0; i < tag.size(); i++) {
                this.faces[i].readTag(tag.get(i));
            }
        } else {
            for (IOFace face : faces) {
                face.setOption(ResourceType.NONE, ResourceFlow.BOTH);
            }
        }
    }

    @Override
    public void writePacket(@NotNull ByteBuf buf) {
        for (IOFace face : this.faces) {
            face.writePacket(buf);
        }
    }

    @Override
    public void readPacket(@NotNull ByteBuf buf) {
        for (IOFace face : this.faces) {
            face.readPacket(buf);
        }
    }

    @Override
    public IOConfig getIOConfig() {
        return this;
    }

    @Override
    public boolean hasChanged(@NotNull IOConfig previous) {
        for (int i = 0; i < this.faces.length; i++) {
            if (this.faces[i].hasChanged(previous.faces[i])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void readDeltaPacket(@NotNull ByteBuf buf) {
        byte n = buf.readByte();
        for (int j = 0; j < n; j++) {
            byte i = buf.readByte();
            this.faces[i].readPacket(buf);
        }
    }

    @Override
    public void writeDeltaPacket(@NotNull ByteBuf buf, @NotNull IOConfig previous) {
        byte n = 0;
        for (int i = 0; i < this.faces.length; i++) {
            if (this.faces[i].hasChanged(previous.faces[i])) {
                n++;
            }
        }
        buf.writeByte(n);
        for (int i = 0; i < this.faces.length; i++) {
            if (this.faces[i].hasChanged(previous.faces[i])) {
                buf.writeByte(i);
                this.faces[i].writePacket(buf);
            }
        }
    }

    @Override
    public IOConfig createEquivalent() {
        return new IOConfig();
    }

    @Override
    public void copyInto(@NotNull IOConfig other) {
        for (int i = 0; i < this.faces.length; i++) {
            this.faces[i].copyInto(other.faces[i]);
        }
    }
}
