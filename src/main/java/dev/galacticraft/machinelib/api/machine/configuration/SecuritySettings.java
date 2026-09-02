/*
 * Copyright (c) 2021-2026 Team Galacticraft
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

import com.mojang.authlib.GameProfile;
import dev.galacticraft.machinelib.api.misc.DeltaPacketSerializable;
import dev.galacticraft.machinelib.api.misc.PacketSerializable;
import dev.galacticraft.machinelib.api.misc.Serializable;
import dev.galacticraft.machinelib.impl.Constant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a security setting of a machine.
 */
public class SecuritySettings implements Serializable<CompoundTag>, DeltaPacketSerializable<FriendlyByteBuf, SecuritySettings> {
    /**
     * The profile of the player who owns the linked machine.
     */
    protected @Nullable UUID owner = null;
    /**
     * The access level of the linked machine.
     */
    protected @NotNull AccessLevel accessLevel = AccessLevel.PUBLIC;
    StreamCodec<FriendlyByteBuf, SecuritySettings> CODEC = PacketSerializable.createCodec(SecuritySettings::new);

    /**
     * Updates the owner of the linked machine if it is not already set.
     *
     * @param uuid the player to try to set as the owner
     */
    public void tryUpdate(@NotNull UUID uuid) {
        if (this.owner == null) {
            this.owner = uuid;
        }
    }

    /**
     * {@return whether the player is the owner of the linked machine}
     *
     * @param player the player to check
     */
    @Contract(pure = true)
    public boolean isOwner(@NotNull Player player) {
        return player.getUUID().equals(this.owner);
    }

    /**
     * {@return whether the player belongs to the same team as the owner of the linked machine}
     *
     * @param player the player to check
     */
    @Contract(pure = true)
    public boolean isOwnerOnTeam(@NotNull Player player) {
        if (isOwner(player)) {
            return true;
        }

        Optional<GameProfile> profile = SkullBlockEntity.fetchGameProfile(this.owner).getNow(Optional.empty());
        if (profile.isPresent()) {
            PlayerTeam playerTeam = player.level().getScoreboard().getPlayersTeam(profile.get().getName());
            if (playerTeam != null) {
                return playerTeam.getPlayers().contains(player.getScoreboardName());
            }
        }

        return false;
    }

    /**
     * {@return whether the player has access to the linked machine}
     *
     * @param player the player to check
     */
    public boolean hasAccess(@NotNull Player player) {
        return switch (this.accessLevel) {
            case PUBLIC -> true;
            case TEAM -> this.isOwnerOnTeam(player);
            case PRIVATE -> this.isOwner(player);
        };
    }

    /**
     * {@return the access level of the linked machine}
     */
    public @NotNull AccessLevel getAccessLevel() {
        return this.accessLevel;
    }

    /**
     * Sets the access level of the linked machine.
     *
     * @param accessLevel The access level to set.
     */
    public void setAccessLevel(@NotNull AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    /**
     * {@return the uuid of the player that owns the linked machine}
     */
    public @Nullable UUID getOwner() {
        return this.owner;
    }

    @Override
    public @NotNull CompoundTag createTag() {
        CompoundTag nbt = new CompoundTag();
        if (this.owner != null) {
            nbt.putUUID(Constant.Nbt.OWNER, this.owner);
        }
        nbt.putString(Constant.Nbt.ACCESS_LEVEL, this.accessLevel.getSerializedName());
        return nbt;
    }

    @Override
    public void readTag(@NotNull CompoundTag tag) {
        if (tag.contains(Constant.Nbt.OWNER)) {
            this.owner = tag.getUUID(Constant.Nbt.OWNER);
        }

        if (tag.contains(Constant.Nbt.ACCESS_LEVEL)) {
            this.accessLevel = AccessLevel.fromString(tag.getString(Constant.Nbt.ACCESS_LEVEL));
        }
    }

    @Override
    public void writePacket(@NotNull FriendlyByteBuf buf) {
        buf.writeByte(this.accessLevel.ordinal());
        buf.writeBoolean(this.owner != null);
        if (this.owner != null) {
            buf.writeUUID(this.owner);
        }
    }

    @Override
    public void readPacket(@NotNull FriendlyByteBuf buf) {
        this.accessLevel = AccessLevel.getByOrdinal(buf.readByte());
        if (buf.readBoolean()) {
            this.owner = buf.readUUID();
        } else {
            this.owner = null;
        }
    }

    @Override
    public void writeDeltaPacket(@NotNull FriendlyByteBuf buf, SecuritySettings previous) {
        byte ref = 0b000;

        if (previous.accessLevel != this.accessLevel) {
            ref |= 0b001;
        }

        if (previous.owner != this.owner) {
            if (this.owner != null) {
                ref |= 0b010;
            } else {
                ref |= 0b100;
            }
        }

        buf.writeByte(ref);
        if ((ref & 0b001) != 0) {
            buf.writeByte(this.accessLevel.ordinal());
        }
        if ((ref & 0b010) != 0) {
            buf.writeUUID(this.owner);
        }
    }

    @Override
    public void readDeltaPacket(@NotNull FriendlyByteBuf buf) {
        byte ref = buf.readByte();

        if ((ref & 0b001) != 0) {
            this.accessLevel = AccessLevel.getByOrdinal(buf.readByte());
        }

        if ((ref & 0b010) != 0) {
            this.owner = buf.readUUID();
        } else if ((ref & 0b100) != 0) {
            this.owner = null;
        }
    }

    @Override
    public SecuritySettings createEquivalent() {
        return new SecuritySettings();
    }

    @Override
    public boolean hasChanged(SecuritySettings previous) {
        return !Objects.equals(previous.owner, this.owner) || previous.accessLevel != this.accessLevel;
    }

    @Override
    public void copyInto(SecuritySettings other) {
        other.owner = this.owner;
        other.accessLevel = this.accessLevel;
    }
}
