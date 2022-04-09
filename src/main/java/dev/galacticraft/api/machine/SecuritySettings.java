/*
 * Copyright (c) 2019-2022 Team Galacticraft
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
import dev.galacticraft.impl.machine.Constant;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a security setting of a machine.
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public class SecuritySettings {
    /**
     * The profile of the player who owns the linked machine.
     */
    private @Nullable GameProfile owner = null;
    /**
     * The team of the player who owns the linked machine.
     */
    private @Nullable Identifier team = null;
    /**
     * The security level of the linked machine.
     */
    private @NotNull SecuritySettings.SecurityLevel securityLevel = SecurityLevel.PUBLIC;

    /**
     * Returns whether the player is the owner of the linked machine.
     * @param player The player to check.
     * @return Whether the player is the owner of the linked machine.
     */
    @Contract(pure = true)
    public boolean isOwner(@NotNull PlayerEntity player) {
        return this.isOwner(player.getGameProfile());
    }

    /**
     * Returns whether the game profile is the owner of the linked machine.
     * @param profile The game profile to check.
     * @return Whether the game profile is the owner of the linked machine.
     */
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
    @Contract(pure = true)
    public boolean hasAccess(PlayerEntity player) {
        if (this.securityLevel == SecurityLevel.PUBLIC) {
            return true;
        } else if (this.securityLevel == SecurityLevel.TEAM) {
            if (this.isOwner(player)) return true;
            return false; //todo: teams
        } else if (this.securityLevel == SecurityLevel.PRIVATE) {
            return this.isOwner(player);
        }
        return false;
    }

    /**
     * Returns the security level of the linked machine.
     * @return The security level of the linked machine.
     */
    public @NotNull SecuritySettings.SecurityLevel getSecurityLevel() {
        return this.securityLevel;
    }

    /**
     * Sets the security level of the linked machine.
     * @param securityLevel The security level to set.
     */
    public void setSecurityLevel(@NotNull SecuritySettings.SecurityLevel securityLevel) {
        this.securityLevel = securityLevel;
    }

    /**
     * Returns the game profile of the owner of the linked machine.
     * @return
     */
    public @Nullable GameProfile getOwner() {
        return this.owner;
    }

    /**
     * Sets the player who owns the linked machine.
     * @param owner The playrt to set.
     */
    public void setOwner(/*@NotNull Teams teams, */@NotNull PlayerEntity owner) { //todo: teams
        this.setOwner(/*teams, */owner.getGameProfile());
    }

    /**
     * Sets the game profile of the owner of the linked machine.
     * @param owner The game profile to set.
     */
    public void setOwner(/*@NotNull Teams teams, */@NotNull GameProfile owner) {
        if (this.getOwner() == null) {
            this.owner = owner;
//            if (teams.getTeam(owner.getId()) != null) this.team = teams.getTeam(owner.getId()).id;  //todo: teams
        }
    }

    /**
     * Returns the team of the owner of the linked machine.
     * @return The team of the owner of the linked machine.
     */
    public @Nullable Identifier getTeam() {
        return team;
    }

    /**
     * Serializes the security settings to nbt.
     * @return The serialized security settings.
     */
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        if (this.getOwner() != null) {
            nbt.put(Constant.Nbt.OWNER, NbtHelper.writeGameProfile(new NbtCompound(), this.getOwner()));
        }
        nbt.putString(Constant.Nbt.ACCESSIBILITY, this.securityLevel.name());
        if (this.getTeam() != null) {
            nbt.putString(Constant.Nbt.TEAM, this.team.toString());
        }
        return nbt;
    }

    /**
     * Deserializes the security settings from nbt.
     * @param nbt The nbt to deserialize from.
     */
    public void fromNbt(@NotNull NbtCompound nbt) {
        if (nbt.contains(Constant.Nbt.OWNER)) {
            this.owner = NbtHelper.toGameProfile(nbt.getCompound(Constant.Nbt.OWNER));
        }

        if (nbt.contains(Constant.Nbt.TEAM)) {
            this.team = new Identifier(nbt.getString(Constant.Nbt.TEAM));
        }

        this.securityLevel = SecurityLevel.valueOf(nbt.getString(Constant.Nbt.ACCESSIBILITY));
    }

    /**
     * Sends the security settings to the client.
     * @param pos The position of the machine.
     * @param player The player to send the settings to.
     */
    public void sendPacket(@NotNull BlockPos pos, @NotNull ServerPlayerEntity player) {
        assert this.owner != null;
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeByte(this.securityLevel.ordinal());
        buf.writeNbt(NbtHelper.writeGameProfile(new NbtCompound(), this.owner));
        ServerPlayNetworking.send(player, new Identifier(Constant.MOD_ID, "security_update"), buf);
    }

    /**
     * Represents the level of protection a machine has from other players.
     */
    public enum SecurityLevel implements StringIdentifiable {
        /**
         * All players can use this machine.
         */
        PUBLIC(new TranslatableText("ui.galacticraft.machine.security.accessibility.public")),
        /**
         * Only team members can use this machine.
         */
        TEAM(new TranslatableText("ui.galacticraft.machine.security.accessibility.team")),
        /**
         * Only the owner can use this machine.
         */
        PRIVATE(new TranslatableText("ui.galacticraft.machine.security.accessibility.private"));

        /**
         * The name of the security level.
         */
        private final Text name;

        SecurityLevel(TranslatableText name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.toString();
        }

        /**
         * Returns the name of the security level.
         * @return The name of the security level.
         */
        public Text getName() {
            return this.name;
        }
    }
}
