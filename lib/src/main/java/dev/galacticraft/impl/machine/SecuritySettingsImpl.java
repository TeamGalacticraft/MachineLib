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

package dev.galacticraft.impl.machine;

import com.mojang.authlib.GameProfile;
import dev.galacticraft.api.machine.AccessLevel;
import dev.galacticraft.api.machine.SecuritySettings;
import dev.galacticraft.impl.MLConstant;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a security setting of a machine.
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public class SecuritySettingsImpl implements SecuritySettings {
    /**
     * The profile of the player who owns the linked machine.
     */
    private @Nullable GameProfile owner = null;
    /**
     * The team of the player who owns the linked machine.
     */
    private @Nullable Identifier team = null;
    /**
     * The access level of the linked machine.
     */
    private @NotNull AccessLevel accessLevel = AccessLevel.PUBLIC;

    /**
     * Returns whether the player is the owner of the linked machine.
     * @param player The player to check.
     * @return Whether the player is the owner of the linked machine.
     */
    @Override
    @Contract(pure = true)
    public boolean isOwner(@NotNull PlayerEntity player) {
        return this.isOwner(player.getGameProfile());
    }

    /**
     * Returns whether the game profile is the owner of the linked machine.
     * @param profile The game profile to check.
     * @return Whether the game profile is the owner of the linked machine.
     */
    @Override
    @Contract(pure = true)
    public boolean isOwner(GameProfile profile) {
        if (this.owner == null) return false;
        return this.owner.equals(profile);
    }

    /**
     * Whether the player is allowed to access the linked machine.
     * @param player The player to check.
     * @return Whether the player is allowed to access the linked machine.
     */
    @Override
    @Contract(pure = true)
    public boolean hasAccess(PlayerEntity player) {
        if (this.accessLevel == AccessLevel.PUBLIC) {
            return true;
        } else if (this.accessLevel == AccessLevel.TEAM) {
            return this.isOwner(player); //todo: teams
        } else if (this.accessLevel == AccessLevel.PRIVATE) {
            return this.isOwner(player);
        }
        return false;
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
     * @return The game profile of the owner of the linked machine.
     */
    @Override
    public @Nullable GameProfile getOwner() {
        return this.owner;
    }

    /**
     * Sets the player who owns the linked machine.
     * @param owner The player to set.
     */
    @Override
    public void setOwner(@NotNull PlayerEntity owner) { //todo: teams
        this.setOwner(owner.getGameProfile());
    }

    /**
     * Sets the game profile of the owner of the linked machine.
     * @param owner The game profile to set.
     */
    @Override
    public void setOwner(@NotNull GameProfile owner) {
        if (this.getOwner() == null) {
            this.owner = owner;
//            if (teams.getTeam(owner.getId()) != null) this.team = teams.getTeam(owner.getId()).id;  //todo: teams
        }
    }

    /**
     * Returns the team of the owner of the linked machine.
     * @return The team of the owner of the linked machine.
     */
    @Override
    public @Nullable Identifier getTeam() {
        return team;
    }

    /**
     * Serializes the security settings to nbt.
     * @return The serialized security settings.
     */
    @Override
    public @NotNull NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        if (this.getOwner() != null) {
            nbt.put(MLConstant.Nbt.OWNER, NbtHelper.writeGameProfile(new NbtCompound(), this.getOwner()));
        }
        nbt.putString(MLConstant.Nbt.ACCESS_LEVEL, this.accessLevel.asString());
        if (this.getTeam() != null) {
            nbt.putString(MLConstant.Nbt.TEAM, this.getTeam().toString());
        }
        return nbt;
    }

    /**
     * Deserializes the security settings from nbt.
     * @param nbt The nbt to deserialize from.
     */
    @Override
    public void fromNbt(@NotNull NbtCompound nbt) {
        if (nbt.contains(MLConstant.Nbt.OWNER)) {
            this.owner = NbtHelper.toGameProfile(nbt.getCompound(MLConstant.Nbt.OWNER));
        }

        if (nbt.contains(MLConstant.Nbt.TEAM)) {
            this.team = new Identifier(nbt.getString(MLConstant.Nbt.TEAM));
        }

        this.accessLevel = AccessLevel.fromString(nbt.getString(MLConstant.Nbt.ACCESS_LEVEL));
    }

    /**
     * Sends the security settings to the client.
     * @param pos The position of the machine.
     * @param player The player to send the settings to.
     */
    @Override
    public void sendPacket(@NotNull BlockPos pos, @NotNull ServerPlayerEntity player) {
        assert this.owner != null;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeByte(this.accessLevel.ordinal());
        buf.writeNbt(NbtHelper.writeGameProfile(new NbtCompound(), this.owner));
        ServerPlayNetworking.send(player, new Identifier(MLConstant.MOD_ID, "security_update"), buf);
    }
}
