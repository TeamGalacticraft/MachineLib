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
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the level of protection a machine has from other players.
 */
public enum AccessLevel implements StringRepresentable {
    /**
     * All players can use this machine.
     */
    PUBLIC(Component.translatable(Constant.TranslationKey.PUBLIC_ACCESS)),
    /**
     * Only team members can use this machine.
     */
    TEAM(Component.translatable(Constant.TranslationKey.TEAM_ACCESS)),
    /**
     * Only the owner can use this machine.
     */
    PRIVATE(Component.translatable(Constant.TranslationKey.PRIVATE_ACCESS));

    public static final AccessLevel[] VALUES = AccessLevel.values();

    /**
     * The text of the access level.
     */
    private final @NotNull Component name;

    @Contract(pure = true)
    AccessLevel(@NotNull Component name) {
        this.name = name;
    }

    /**
     * Deserializes an access level from a string
     *
     * @param value the string to deserialize
     * @return the deserialized access level
     * @see #getSerializedName()
     */
    @Contract(pure = true)
    public static @NotNull AccessLevel fromString(@NotNull String value) {
        return switch (value) {
            case "public" -> PUBLIC;
            case "team" -> TEAM;
            case "private" -> PRIVATE;
            default -> throw new IllegalArgumentException("Invalid access level: " + value);
        };
    }

    public static @NotNull AccessLevel getByOrdinal(byte ordinal) {
        return VALUES[ordinal];
    }

    /**
     * Returns the text of the access level.
     *
     * @return The text of the access level.
     */
    @Contract(pure = true)
    public @NotNull Component getName() {
        return this.name;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String getSerializedName() {
        return switch (this) {
            case PUBLIC -> "public";
            case TEAM -> "team";
            case PRIVATE -> "private";
        };
    }
}
