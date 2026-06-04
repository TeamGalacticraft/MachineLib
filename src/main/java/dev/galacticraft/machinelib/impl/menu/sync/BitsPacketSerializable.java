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

package dev.galacticraft.machinelib.impl.menu.sync;

import dev.galacticraft.machinelib.api.misc.DeltaPacketSerializable;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public record BitsPacketSerializable(int len, boolean[] source, boolean[] dest) implements DeltaPacketSerializable<ByteBuf, boolean[]> {
    public BitsPacketSerializable {
        assert len == source.length;
        assert len == dest.length;
    }

    @Override
    public boolean hasChanged(boolean[] previous) {
        for (int i = 0; i < this.len; i++) {
            if (previous[i] != this.source[i]) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void copyInto(boolean[] other) {
        System.arraycopy(this.source, 0, other, 0, this.len);
    }

    @Override
    public void readPacket(@NotNull ByteBuf buf) {
        int bytes = (this.len / 8) + 1;
        for (int i = 0; i < bytes; i++) {
            byte b = buf.readByte();
            for (int j = 0; j < 8 && i * 8 + j < this.len; j++) {
                this.dest[i * 8 + j] = (b & (0b1 << j)) != 0;
            }
        }
    }

    @Override
    public void writePacket(@NotNull ByteBuf buf) {
        int bytes = (this.len / 8) + 1;
        for (int i = 0; i < bytes; i++) {
            byte b = 0;
            for (int j = 0; j < 8 && i * 8 + j < this.len; j++) {
                if (this.source[i * 8 + j]) {
                    b |= (byte) (0b1 << j);
                }
            }
            buf.writeByte(b);
        }
    }

    @Override
    public boolean[] createEquivalent() {
        return new boolean[this.len];
    }

}
