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

import com.mojang.authlib.GameProfile;
import dev.galacticraft.impl.machine.SecuritySettingsImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a security setting of a machine.
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public interface SecuritySettings {
    @Contract(" -> new")
    static @NotNull SecuritySettings create() {
        return new SecuritySettingsImpl();
    }

    /**
     * Returns whether the player is the owner of the linked machine.
     * @param player The player to check.
     * @return Whether the player is the owner of the linked machine.
     */
    @Contract(pure = true)
    boolean isOwner(@NotNull Player player);

    /**
     * Returns whether the game profile is the owner of the linked machine.
     * @param profile The game profile to check.
     * @return Whether the game profile is the owner of the linked machine.
     */
    @Contract(pure = true)
    boolean isOwner(@Nullable GameProfile profile);

    /**
     * Whether the player is allowed to access the linked machine.
     * @param player The player to check.
     * @return Whether the player is allowed to access the linked machine.
     */
    @Contract(pure = true)
    boolean hasAccess(@Nullable Player player);

    /**
     * Returns the access level of the linked machine.
     * @return The access level of the linked machine.
     */
    @NotNull AccessLevel getAccessLevel();

    /**
     * Sets the access level of the linked machine.
     * @param accessLevel The access level to set.
     */
    void setAccessLevel(@NotNull AccessLevel accessLevel);

    /**
     * Returns the game profile of the owner of the linked machine.
     * @return The game profile of the owner of the linked machine.
     */
    @Nullable GameProfile getOwner();

    /**
     * Sets the player who owns the linked machine.
     * @param owner The player to set.
     */
    void setOwner(@NotNull Player owner);

    /**
     * Sets the game profile of the owner of the linked machine.
     * @param owner The game profile to set.
     */
    void setOwner(@NotNull GameProfile owner);

    /**
     * Returns the team of the owner of the linked machine.
     * @return The team of the owner of the linked machine.
     */
    @Nullable ResourceLocation getTeam();

    /**
     * Serializes the security settings to nbt.
     * @return The serialized security settings.
     */
    @NotNull CompoundTag toNbt();

    /**
     * Deserializes the security settings from nbt.
     * @param nbt The nbt to deserialize from.
     */
    void fromNbt(@NotNull CompoundTag nbt);

    /**
     * Sends the security settings to the client.
     * @param pos The position of the machine.
     * @param player The player to send the settings to.
     */
    void sendPacket(@NotNull BlockPos pos, @NotNull ServerPlayer player);
}
