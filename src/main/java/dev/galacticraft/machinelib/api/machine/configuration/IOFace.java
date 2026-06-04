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

package dev.galacticraft.machinelib.api.machine.configuration;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.misc.Equivalent;
import dev.galacticraft.machinelib.api.misc.PacketSerializable;
import dev.galacticraft.machinelib.api.misc.Serializable;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.ByteTag;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * Represents a face of a {@link MachineBlockEntity} that has been configured to
 * accept certain types of resources.
 */
public class IOFace implements Serializable<ByteTag>, PacketSerializable<ByteBuf>, Equivalent<IOFace> {
    public static final StreamCodec<ByteBuf, IOFace> CODEC = PacketSerializable.createCodec(IOFace::new);

    /**
     * The type of resource that this face is configured to accept.
     */
    protected @NotNull ResourceType type;
    /**
     * The flow direction of this face.
     */
    protected @NotNull ResourceFlow flow;

    public IOFace() {
        this(ResourceType.NONE, ResourceFlow.BOTH);
    }

    public IOFace(@NotNull ResourceType type, @NotNull ResourceFlow flow) {
        this.type = type;
        this.flow = flow;
    }

    @VisibleForTesting
    public static byte pack(ResourceType type, ResourceFlow flow) {
        return (byte) (flow.getId() << 3 | type.getId());
    }

    @VisibleForTesting
    public static ResourceFlow unpackFlow(byte packed) {
        return ResourceFlow.getFromId((byte) (packed >> 3));
    }

    @VisibleForTesting
    public static ResourceType unpackType(byte packed) {
        return ResourceType.getFromId((byte) (packed & 0b111));
    }

    /**
     * {@return the type of resource this face is configured to accept}
     */
    public @NotNull ResourceType getType() {
        return this.type;
    }

    /**
     * {@return the flow direction of this face}
     * If the resource type is {@code NONE} this should always be {@code BOTH}.
     */
    public @NotNull ResourceFlow getFlow() {
        return this.flow;
    }

    /**
     * Sets the type and flow of this face.
     *
     * @param type the resource type to accept
     * @param flow the flow direction of this face
     */
    public void setOption(@NotNull ResourceType type, @NotNull ResourceFlow flow) {
        this.type = type;
        this.flow = flow;
    }

    @Override
    public @NotNull ByteTag createTag() {
        return ByteTag.valueOf(pack(this.type, this.flow));
    }

    @Override
    public void readTag(@NotNull ByteTag tag) {
        byte packed = tag.getAsByte();
        this.type = unpackType(packed);
        this.flow = unpackFlow(packed);
    }

    @Override
    public void writePacket(@NotNull ByteBuf buf) {
        buf.writeByte(pack(this.type, this.flow));
    }

    @Override
    public void readPacket(@NotNull ByteBuf buf) {
        byte packed = buf.readByte();
        this.type = unpackType(packed);
        this.flow = unpackFlow(packed);
    }

    @Override
    public boolean hasChanged(@NotNull IOFace previous) {
        return previous.type != this.type
                || previous.flow != this.flow;
    }

    @Override
    public void copyInto(@NotNull IOFace other) {
        other.type = this.type;
        other.flow = this.flow;
    }
}
