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

package dev.galacticraft.machinelib.impl.machine;

import dev.galacticraft.machinelib.api.machine.AccessLevel;
import dev.galacticraft.machinelib.api.machine.SecuritySettings;
import dev.galacticraft.machinelib.impl.Constant;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
     *
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
     *
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
     *
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
     *
     * @return The access level of the linked machine.
     */
    @Override
    public @NotNull AccessLevel getAccessLevel() {
        return this.accessLevel;
    }

    /**
     * Sets the access level of the linked machine.
     *
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
     *
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
     *
     * @return The team of the owner of the linked machine.
     */
    @Override
    public @Nullable ResourceLocation getTeam() {
        return team;
    }

    @Override
    public void setTeam(ResourceLocation team, String name) { //todo: team validation
        if (team == null && name != null) throw new IllegalArgumentException("Team name without linked team??");
        this.team = team;
        this.teamName = name;
    }

    /**
     * Sends the security settings to the client.
     *
     * @param pos    The position of the machine.
     * @param player The player to send the settings to.
     */
    @Override
    public void sendPacket(@NotNull BlockPos pos, @NotNull ServerPlayer player) {
        assert this.owner != null;
        FriendlyByteBuf buf = PacketByteBufs.create();
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
        ServerPlayNetworking.send(player, Constant.id("security_update"), buf);
    }

    @Override
    public @NotNull CompoundTag createTag() {
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

    @Override
    public void readTag(@NotNull CompoundTag tag) {
        if (tag.contains(Constant.Nbt.OWNER)) {
            this.owner = tag.getUUID(Constant.Nbt.OWNER);
        }
        if (tag.contains(Constant.Nbt.USERNAME)) {
            this.username = tag.getString(Constant.Nbt.USERNAME);
        }

        if (tag.contains(Constant.Nbt.TEAM)) {
            this.team = new ResourceLocation(tag.getString(Constant.Nbt.TEAM));
            if (tag.contains(Constant.Nbt.TEAM_NAME)) {
                this.teamName = tag.getString(Constant.Nbt.TEAM_NAME);
            }
        }

        this.accessLevel = AccessLevel.fromString(tag.getString(Constant.Nbt.ACCESS_LEVEL));
    }

    @Override
    public void writePacket(@NotNull FriendlyByteBuf buf) {
        buf.writeByte(this.accessLevel.ordinal());
        byte bits = 0b0000;
        if (this.owner != null) bits |= 0b0001;
        if (this.username != null) bits |= 0b0010;
        if (this.team != null) bits |= 0b0100;
        if (this.teamName != null) bits |= 0b1000;

        if (this.owner == null) {
            buf.writeByte(0b0000);
        } else {
            buf.writeByte(bits);
            buf.writeUUID(this.owner);
            if (this.username != null) buf.writeUtf(this.username);
            if (this.team != null) buf.writeResourceLocation(this.team);
            if (this.teamName != null) buf.writeUtf(this.teamName);
        }
    }

    @Override
    public void readPacket(@NotNull FriendlyByteBuf buf) {
        this.accessLevel = AccessLevel.getByOrdinal(buf.readByte());
        byte bits = buf.readByte();

        if (bits == 0b0000) {
            this.owner = null;
            this.username = null;
            this.team = null;
            this.teamName = null;
        } else {
            this.owner = buf.readUUID();
            if ((bits & 0b0010) != 0) this.username = buf.readUtf();
            if ((bits & 0b0100) != 0) this.team = buf.readResourceLocation();
            if ((bits & 0b1000) != 0) this.teamName = buf.readUtf();
        }
    }
}
