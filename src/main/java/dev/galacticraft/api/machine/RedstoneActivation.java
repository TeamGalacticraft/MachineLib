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

import dev.galacticraft.impl.machine.Constant;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Represents the behavior of a machine when it is activated by redstone.
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public enum RedstoneActivation implements StringIdentifiable {
    /**
     * Ignores redstone entirely (always running).
     */
    IGNORE(new TranslatableText("ui.galacticraft.redstone.ignore"), Constant.Text.GRAY_STYLE),

    /**
     * When powered with redstone, the machine turns off.
     */
    LOW(new TranslatableText("ui.galacticraft.redstone.low"), Constant.Text.DARK_RED_STYLE),

    /**
     * When powered with redstone, the machine turns on.
     */
    HIGH(new TranslatableText("ui.galacticraft.redstone.high"), Constant.Text.RED_STYLE);

    /**
     * The name of the redstone activation state.
     */
    private final Text name;

    RedstoneActivation(@NotNull TranslatableText name, @NotNull Style style) {
        this.name = name.setStyle(style);
    }

    /**
     * Returns the redstone activation state from the given string identifier.
     * @param string The string identifier.
     * @return The redstone activation state.
     */
    public static RedstoneActivation fromString(@NotNull String string) {
        return RedstoneActivation.valueOf(string.toUpperCase(Locale.ROOT));
    }

    /**
     * Sends a packet to the client to update the redstone activation state.
     * @param pos The position of the machine.
     * @param player The player to send the packet to.
     */
    public void sendPacket(BlockPos pos, ServerPlayerEntity player) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeByte(this.ordinal());
        ServerPlayNetworking.send(player, new Identifier(Constant.MOD_ID, "redstone_update"), buf);
    }

    /**
     * Returns the name of the redstone activation state.
     * @return The name of the redstone activation state.
     */
    public Text getName() {
        return this.name;
    }

    @Override
    public @NotNull String asString() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    /**
     * Serializes the redstone activation state to NBT.
     * @return The NBT element.
     */
    public NbtElement writeNbt() {
        return NbtString.of(this.asString());
    }

    /**
     * Deserializes the redstone activation state from NBT.
     * @param nbt The NBT element.
     * @return The redstone activation state.
     */
    public static @NotNull RedstoneActivation readNbt(@NotNull NbtElement nbt) {
        if (nbt.getType() == NbtElement.STRING_TYPE) {
            return fromString(nbt.asString());
        } else {
            throw new IllegalArgumentException("Expected a string, got " + nbt.getType());
        }
    }
}
