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

import dev.galacticraft.machinelib.api.menu.sync.MenuSynchronizable;
import dev.galacticraft.machinelib.api.misc.Deserializable;
import dev.galacticraft.machinelib.impl.machine.SecuritySettingsImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents a security setting of a machine.
 */
public interface SecuritySettings extends Deserializable<CompoundTag>, MenuSynchronizable {
    /**
     * Constructs a new security settings storage with no owner attached.
     *
     * @return a new security settings storage.
     */
    @Contract(" -> new")
    static @NotNull SecuritySettings create() {
        return new SecuritySettingsImpl();
    }

    /**
     * Returns whether the player is the owner of the linked machine.
     *
     * @param player The player to check.
     * @return Whether the player is the owner of the linked machine.
     */
    @Contract(pure = true)
    boolean isOwner(@NotNull Player player);

    /**
     * Returns whether the game profile is the owner of the linked machine.
     *
     * @param uuid The uuid to check.
     * @return Whether the game profile is the owner of the linked machine.
     */
    @Contract(pure = true)
    boolean isOwner(@Nullable UUID uuid);

    /**
     * Returns the username of the owner or {@code null} if it has not been cached.
     *
     * @return the username of the owner.
     */
    @Contract(pure = true)
    @Nullable String getUsername();

    void setUsername(@Nullable String username);

    /**
     * Returns the name of the team or {@code null} if it has not been cached.
     *
     * @return the name of the team.
     */
    @Contract(pure = true)
    @ApiStatus.Experimental
    @Nullable String getTeamName();

    /**
     * Returns whether the player is allowed to access the linked machine.
     *
     * @param player The player to check.
     * @return whether the player is allowed to access the linked machine.
     */
    @Contract(pure = true)
    boolean hasAccess(@NotNull Player player);

    /**
     * Returns whether the player with the given UUID is allowed to access the linked machine.
     *
     * @param uuid the uuid to test.
     * @return whether the player with the given UUID is allowed to access the linked machine.
     */
    @Contract(pure = true)
    boolean hasAccess(@NotNull UUID uuid);

    /**
     * Returns the access level of the linked machine.
     *
     * @return The access level of the linked machine.
     */
    @Contract(pure = true)
    @NotNull AccessLevel getAccessLevel();

    /**
     * Sets the access level of the linked machine.
     *
     * @param accessLevel The access level to set.
     */
    @Contract(mutates = "this")
    void setAccessLevel(@NotNull AccessLevel accessLevel);

    /**
     * Returns the game profile of the owner of the linked machine.
     *
     * @return The game profile of the owner of the linked machine.
     */
    @Contract(pure = true)
    @Nullable UUID getOwner();

    /**
     * Sets the game profile of the owner of the linked machine.
     *
     * @param owner The uuid of the owner.
     * @param name  The name of the owner.
     */
    @Contract(mutates = "this")
    void setOwner(@Nullable UUID owner, String name);

    /**
     * Returns the team of the owner of the linked machine.
     *
     * @return The team of the owner of the linked machine.
     */
    @Nullable ResourceLocation getTeam();

    /**
     * Sets the team linked to these security settings
     *
     * @param team the team to be granted access to the linked machine can be {@code null}.
     * @param name the name of the team to be granted access to the linked machine.
     */
    @ApiStatus.Experimental
    @Contract(mutates = "this", value = "null, !null -> fail")
    void setTeam(@Nullable ResourceLocation team, @Nullable String name);

    boolean hasOwner();
}
