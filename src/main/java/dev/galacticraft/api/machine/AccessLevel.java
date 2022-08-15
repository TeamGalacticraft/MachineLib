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

package dev.galacticraft.api.machine;

import dev.galacticraft.impl.MLConstant;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the level of protection a machine has from other players.
 */
public enum AccessLevel implements StringRepresentable {
    /**
     * All players can use this machine.
     */
    PUBLIC(Component.translatable(MLConstant.TranslationKey.PUBLIC_ACCESS)),
    /**
     * Only team members can use this machine.
     */
    TEAM(Component.translatable(MLConstant.TranslationKey.TEAM_ACCESS)),
    /**
     * Only the owner can use this machine.
     */
    PRIVATE(Component.translatable(MLConstant.TranslationKey.PRIVATE_ACCESS));

    /**
     * The name of the access level.
     */
    private final @NotNull Component name;

    AccessLevel(@NotNull Component name) {
        this.name = name;
    }

    public static AccessLevel fromString(String string) {
        return switch (string) {
            case "public" -> PUBLIC;
            case "team" -> TEAM;
            case "private" -> PRIVATE;
            default -> throw new IllegalArgumentException("Invalid access level: " + string);
        };
    }

    /**
     * Returns the name of the access level.
     *
     * @return The name of the access level.
     */
    public @NotNull Component getName() {
        return this.name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return switch (this) {
            case PUBLIC -> "public";
            case TEAM -> "team";
            case PRIVATE -> "private";
        };
    }
}
