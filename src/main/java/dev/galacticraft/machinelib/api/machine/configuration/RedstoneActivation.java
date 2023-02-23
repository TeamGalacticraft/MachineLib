/*
 * Copyright (c) 2021-2023 Team Galacticraft
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

import dev.galacticraft.machinelib.impl.Constant;
import net.minecraft.nbt.ByteTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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

    public static final RedstoneActivation[] VALUES = RedstoneActivation.values();

    /**
     * The name of the redstone activation state.
     */
    private final @NotNull Component name;

    /**
     * Constructs a redstone activation type with the given name.
     *
     * @param name the name of the interaction.
     */
    @Contract(pure = true)
    RedstoneActivation(@NotNull Component name) {
        this.name = name;
    }

    public static @NotNull RedstoneActivation readTag(@NotNull ByteTag tag) {
        return VALUES[tag.getAsByte()];
    }

    public static @NotNull RedstoneActivation readPacket(@NotNull FriendlyByteBuf buf) {
        return VALUES[buf.readByte()];
    }

    /**
     * Returns the name of the redstone activation state.
     *
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

    public @NotNull ByteTag createTag() {
        return ByteTag.valueOf((byte) this.ordinal());
    }

    public void writePacket(@NotNull FriendlyByteBuf buf) {
        buf.writeByte(this.ordinal());
    }
}
