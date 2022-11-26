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

package dev.galacticraft.machinelib.api.machine;

import dev.galacticraft.machinelib.impl.Constant;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Dictates how a machine behaves when it interacts with redstone.
 */
public enum RedstoneActivation implements StringRepresentable {
    /**
     * Ignores redstone entirely (always running).
     */
    IGNORE(Component.translatable(Constant.TranslationKey.IGNORE_REDSTONE).setStyle(Constant.Text.GRAY_STYLE)),

    /**
     * When powered with redstone, the machine turns off.
     */
    LOW(Component.translatable(Constant.TranslationKey.LOW_REDSTONE).setStyle(Constant.Text.DARK_RED_STYLE)),

    /**
     * When powered with redstone, the machine turns on.
     */
    HIGH(Component.translatable(Constant.TranslationKey.HIGH_REDSTONE).setStyle(Constant.Text.RED_STYLE));

    /**
     * The name of the redstone activation state.
     */
    private final @NotNull Component name;

    /**
     * Constructs a redstone activation type with the given name.
     * @param name the name of the interaction.
     */
    @Contract(pure = true)
    RedstoneActivation(@NotNull Component name) {
        this.name = name;
    }

    /**
     * Returns the redstone activation state from the given string identifier.
     * @param string The string identifier.
     * @return The redstone activation state.
     */
    @Contract(pure = true)
    public static @NotNull RedstoneActivation fromString(@NotNull String string) {
        return switch (string) {
            case "low" -> LOW;
            case "high" -> HIGH;
            default -> IGNORE;
        };
    }

    /**
     * Sends a packet to the client to update the redstone activation state.
     * @param pos The position of the machine.
     * @param player The player to send the packet to.
     */
    public void sendPacket(@NotNull BlockPos pos, @NotNull ServerPlayer player) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeByte(this.ordinal());
        ServerPlayNetworking.send(player, Constant.id("redstone_update"), buf);
    }

    /**
     * Returns the name of the redstone activation state.
     * @return The name of the redstone activation state.
     */
    @Contract(pure = true)
    public @NotNull Component getName() {
        return this.name;
    }

    @Contract(pure = true)
    @Override
    public @NotNull String getSerializedName() {
        return switch (this) {
            case IGNORE -> "ignore";
            case LOW -> "low";
            case HIGH -> "high";
        };
    }

    /**
     * Serializes the redstone activation state to NBT.
     * @return The NBT element.
     */
    public @NotNull Tag writeNbt() {
        return StringTag.valueOf(this.getSerializedName());
    }

    /**
     * Deserializes the redstone activation state from NBT.
     * @param nbt The NBT element.
     * @return The redstone activation state.
     */
    public static @NotNull RedstoneActivation readNbt(@NotNull Tag nbt) {
        if (nbt.getId() == Tag.TAG_STRING) {
            return fromString(nbt.getAsString());
        } else {
            throw new IllegalArgumentException("Expected a string, got " + nbt.getId());
        }
    }
}
