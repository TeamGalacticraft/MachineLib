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

package dev.galacticraft.machinelib.impl.network.c2s;

import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.impl.Constant;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record SideConfigurationClickPayload(BlockFace face, Action action) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SideConfigurationClickPayload> TYPE = new CustomPacketPayload.Type<>(Constant.id("io_click"));
    public static final StreamCodec<ByteBuf, SideConfigurationClickPayload> CODEC = StreamCodec.composite(
            BlockFace.CODEC, p -> p.face,
            Action.CODEC, p -> p.action,
            SideConfigurationClickPayload::new
    );
    public SideConfigurationClickPayload(BlockFace face, boolean reverse, boolean reset) {
        this(face, reset ? Action.RESET : reverse ? Action.PREVIOUS : Action.NEXT);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void apply(ServerPlayNetworking.Context context) {
        if (context.player().containerMenu instanceof MachineMenu<?> menu) {
            menu.cycleFaceConfig(this.face, this.action == Action.PREVIOUS, this.action == Action.RESET);
        }
    }

    public enum Action {
        NEXT,
        PREVIOUS,
        RESET;

        public static final StreamCodec<ByteBuf, Action> CODEC = ByteBufCodecs.BYTE.map(i -> i == -1 ? null : values()[i], face -> face == null ? -1 : (byte) face.ordinal());
    }
}
