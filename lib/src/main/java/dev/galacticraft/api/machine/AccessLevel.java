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

import dev.galacticraft.impl.Constant;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the level of protection a machine has from other players.
 */
public enum AccessLevel implements StringIdentifiable {
    /**
     * All players can use this machine.
     */
    PUBLIC(new TranslatableText(Constant.TranslationKey.PUBLIC_ACCESS)),
    /**
     * Only team members can use this machine.
     */
    TEAM(new TranslatableText(Constant.TranslationKey.TEAM_ACCESS)),
    /**
     * Only the owner can use this machine.
     */
    PRIVATE(new TranslatableText(Constant.TranslationKey.PRIVATE_ACCESS));

    /**
     * The name of the access level.
     */
    private final @NotNull Text name;

    AccessLevel(@NotNull Text name) {
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
    public @NotNull Text getName() {
        return this.name;
    }

    @Override
    public @NotNull String asString() {
        return switch (this) {
            case PUBLIC -> "public";
            case TEAM -> "team";
            case PRIVATE -> "private";
        };
    }
}
