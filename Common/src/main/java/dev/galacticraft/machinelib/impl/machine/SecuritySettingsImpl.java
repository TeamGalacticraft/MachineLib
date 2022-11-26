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

package dev.galacticraft.machinelib.impl.machine;

import dev.galacticraft.machinelib.api.machine.AccessLevel;
import dev.galacticraft.machinelib.api.machine.SecuritySettings;
import dev.galacticraft.machinelib.impl.Constant;
import io.netty.buffer.Unpooled;
import lol.bai.badpackets.api.PacketSender;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents a security setting of a machine.
 */
@ApiStatus.Internal
public final class SecuritySettingsImpl implements SecuritySettings {
    /**
     * The profile of the player who owns the linked machine.
     */
    private @Nullable UUID owner = null;
    private @Nullable String username = null;
    /**
     * The team of the player who owns the linked machine.
     */
    private @Nullable ResourceLocation team = null;
    /**
     * The access level of the linked machine.
     */
    private @NotNull AccessLevel accessLevel = AccessLevel.PUBLIC;
    private @Nullable String teamName = null;

    /**
     * Returns whether the player is the owner of the linked machine.
     * @param player The player to check.
     * @return Whether the player is the owner of the linked machine.
     */
    @Override
    @Contract(pure = true)
    public boolean isOwner(@NotNull Player player) {
        return this.isOwner(player.getUUID());
    }

    /**
     * Returns whether the game profile is the owner of the linked machine.
     * @param uuid The uuid to check.
     * @return Whether the game profile is the owner of the linked machine.
     */
    @Override
    @Contract(pure = true)
    public boolean isOwner(UUID uuid) {
        if (this.owner == null) return false;
        return this.owner.equals(uuid);
    }

    public @Nullable String getUsername() {
        return this.username;
    }

    public @Nullable String getTeamName() {
        return this.teamName;
    }

    @Override
    public boolean hasAccess(@NotNull Player player) {
        return this.hasAccess(player.getUUID());
    }

    /**
     * Whether the player is allowed to access the linked machine.
     * @param uuid The uuid to check.
     * @return Whether the uuid is allowed to access the linked machine.
     */
    @Override
    @Contract(pure = true)
    public boolean hasAccess(@NotNull UUID uuid) {
        return switch (this.accessLevel) {
            case PUBLIC -> true;
            case TEAM -> this.isOwner(uuid); // todo: teams
            case PRIVATE -> this.isOwner(uuid);
        };
    }

    /**
     * Returns the access level of the linked machine.
     * @return The access level of the linked machine.
     */
    @Override
    public @NotNull AccessLevel getAccessLevel() {
        return this.accessLevel;
    }

    /**
     * Sets the access level of the linked machine.
     * @param accessLevel The access level to set.
     */
    @Override
    public void setAccessLevel(@NotNull AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    /**
     * Returns the game profile of the owner of the linked machine.
     *
     * @return The game profile of the owner of the linked machine.
     */
    @Override
    public @Nullable UUID getOwner() {
        return this.owner;
    }

    /**
     * Sets the player who owns the linked machine.
     * @param player The player to set.
     */
    @Override
    public void setOwner(@NotNull Player player) { //todo: teams
        this.setOwner(player.getUUID(), player.getGameProfile().getName());
    }

    /**
     * Sets the game profile of the owner of the linked machine.
     *
     * @param uuid The uuid to set.
     * @param name The name of the player
     */
    @Override
    public void setOwner(@NotNull UUID uuid, String name) {
        if (this.getOwner() == null) {
            this.owner = uuid;
//            if (teams.getTeam(uuid) != null) this.team = teams.getTeam(uuid).id;  //todo: teams
        }
    }

    /**
     * Returns the team of the owner of the linked machine.
     * @return The team of the owner of the linked machine.
     */
    @Override
    public @Nullable ResourceLocation getTeam() {
        return team;
    }

    /**
     * Serializes the security settings to nbt.
     * @return The serialized security settings.
     */
    @Override
    public @NotNull CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
        if (this.getOwner() != null) {
            nbt.putUUID(Constant.Nbt.OWNER, this.getOwner());
            if (this.getUsername() != null) {
                nbt.putString(Constant.Nbt.USERNAME, this.getUsername());
            }
        }
        nbt.putString(Constant.Nbt.ACCESS_LEVEL, this.accessLevel.getSerializedName());
        if (this.getTeam() != null) {
            nbt.putString(Constant.Nbt.TEAM, this.getTeam().toString());
            if (this.getTeamName() != null) {
                nbt.putString(Constant.Nbt.TEAM_NAME, this.getTeamName());
            }
        }
        return nbt;
    }

    /**
     * Deserializes the security settings from nbt.
     * @param nbt The nbt to deserialize from.
     */
    @Override
    public void fromNbt(@NotNull CompoundTag nbt) {
        if (nbt.contains(Constant.Nbt.OWNER)) {
            this.owner = nbt.getUUID(Constant.Nbt.OWNER);
        }
        if (nbt.contains(Constant.Nbt.USERNAME)) {
            this.username = nbt.getString(Constant.Nbt.USERNAME);
        }

        if (nbt.contains(Constant.Nbt.TEAM)) {
            this.team = new ResourceLocation(nbt.getString(Constant.Nbt.TEAM));
            if (nbt.contains(Constant.Nbt.TEAM_NAME)) {
                this.teamName = nbt.getString(Constant.Nbt.TEAM_NAME);
            }
        }

        this.accessLevel = AccessLevel.fromString(nbt.getString(Constant.Nbt.ACCESS_LEVEL));
    }

    @Override
    public void setTeam(ResourceLocation team, String name) { //todo: team validation
        if (team == null && name != null) throw new IllegalArgumentException("Team name without linked team??");
        this.team = team;
        this.teamName = name;
    }

    /**
     * Sends the security settings to the client.
     * @param pos The position of the machine.
     * @param player The player to send the settings to.
     */
    @Override
    public void sendPacket(@NotNull BlockPos pos, @NotNull ServerPlayer player) {
        assert this.owner != null;
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeByte(this.accessLevel.ordinal());
        buf.writeUUID(this.owner);
        if (this.username != null) {
            buf.writeBoolean(true);
            buf.writeUtf(this.username);
        } else {
            buf.writeBoolean(false);
        }
        if (this.team != null) {
            buf.writeBoolean(true);
            buf.writeUtf(this.team.toString());
        } else {
            buf.writeBoolean(false);
        }
        if (this.teamName != null) {
            buf.writeBoolean(true);
            buf.writeUtf(this.teamName);
        } else {
            buf.writeBoolean(false);
        }
        PacketSender.s2c(player).send(Constant.id("security_update"), buf);
    }
}
