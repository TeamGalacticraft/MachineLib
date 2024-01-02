/*
 * Copyright (c) 2021-2024 Team Galacticraft
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

package dev.galacticraft.machinelib.impl.menu.sync.simple;

import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public final class BooleansMenuSyncHandler implements MenuSyncHandler {
    private final boolean[] input;
    private final boolean[] output;

    @Contract(pure = true)
    public BooleansMenuSyncHandler(boolean @NotNull [] input, boolean @NotNull [] output) {
        assert input.length == output.length;
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean needsSyncing() {
        return !Arrays.equals(this.input, this.output);
    }

    @Override
    public void sync(@NotNull FriendlyByteBuf buf) {
        int len = this.input.length;
        int bLen = (len - len % 8) / 8 + 1;
        byte[] bytes = new byte[bLen];
        Arrays.fill(bytes, (byte) 0);
        int b = 0;
        int j = 0;
        for (int i = 0; i < len; i++) {
            bytes[b] |= (byte) ((this.input[b * 8 + j] ? 0b1 : 0b0) << j++);
            if (j == 8) {
                j = 0;
                b++;
            }
        }

        for (byte value : bytes) {
            buf.writeByte(value);
        }
    }

    @Override
    public void read(@NotNull FriendlyByteBuf buf) {
        int len = this.output.length;
        int bLen = (len - len % 8) / 8 + 1;
        byte[] bytes = new byte[bLen];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = buf.readByte();
        }
        int b = 0;
        int j = 0;
        for (int i = 0; i < len; i++) {
            this.output[i] = ((bytes[b] >> j++) & 0b1) != 0;
            if (j == 8) {
                j = 0;
                b++;
            }
        }
    }
}
