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

import dev.galacticraft.machinelib.api.misc.DeltaPacketSerializable;
import dev.galacticraft.machinelib.api.misc.PacketSerializable;
import dev.galacticraft.machinelib.api.misc.Serializable;
import dev.galacticraft.machinelib.impl.Constant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents the security settings of a machine.
 *
 * <p>A machine always has a default access level which determines whether a
 * player may use the machine normally. Additional independent access levels
 * may also be registered for machine-specific functionality.</p>
 *
 * <p>All access levels share the same owner. For example, a machine may be
 * configured as {@link AccessLevel#PRIVATE} for normal interaction while an
 * additional access level is configured as
 * {@link AccessLevel#PUBLIC}.</p>
 */
public class SecuritySettings implements Serializable<CompoundTag>, DeltaPacketSerializable<FriendlyByteBuf, SecuritySettings> {
    private static final String NBT_ADDITIONAL_ACCESS_LEVELS = "AccessLevels";
    /**
     * The profile of the player who owns the linked machine.
     */
    protected @Nullable UUID owner = null;
    /**
     * The default access level of the linked machine.
     */
    protected @NotNull AccessLevel accessLevel = AccessLevel.PUBLIC;
    /**
     * Additional registered access levels.
     */
    protected final @NotNull Map<ResourceLocation, AccessLevel> accessLevels = new HashMap<>();
    /**
     * Default values for additional registered access levels.
     *
     * <p>These are kept separately so missing or legacy NBT entries can fall
     * back to the access level defined by the machine.</p>
     */
    protected final @NotNull Map<ResourceLocation, AccessLevel> defaultAccessLevels = new HashMap<>();

    StreamCodec<FriendlyByteBuf, SecuritySettings> CODEC = PacketSerializable.createCodec(SecuritySettings::new);

    /**
     * Called whenever a security setting is modified.
     *
     * <p>Subclasses may override this to notify their owning object that the
     * security settings have changed.</p>
     */
    protected void onChanged() {}

    /**
     * Updates the owner of the linked machine if it is not already set.
     *
     * @param uuid the player to try to set as the owner
     */
    public void tryUpdate(@NotNull UUID uuid) {
        if (this.owner == null) {
            this.owner = uuid;
            this.onChanged();
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
     * {@return whether the player has access to the linked machine using the
     * default machine access level}
     *
     * @param player the player to check
     */
    @Contract(pure = true)
    public boolean hasAccess(@NotNull Player player) {
        return this.hasAccess(player, this.accessLevel);
    }

    /**
     * Determines whether a player has access using the supplied access level.
     *
     * <p>This method contains the actual access-level behaviour and is shared
     * by both the default machine access level and all additional registered
     * access levels.</p>
     *
     * @param player      the player to check
     * @param accessLevel the access level to check against
     * @return whether the player has access
     */
    @Contract(pure = true)
    public boolean hasAccess(
            @NotNull Player player,
            @NotNull AccessLevel accessLevel
    ) {
        return switch (accessLevel) {
            case PUBLIC -> true;

            case TEAM -> this.isOwner(player); // todo: teams

            case PRIVATE -> this.isOwner(player);
        };
    }

    /**
     * Determines whether a player has access to an additional registered
     * access level.
     *
     * @param id     the identifier of the registered access level
     * @param player the player to check
     * @return whether the player has access
     * @throws IllegalArgumentException if the access level has not been
     *                                  registered
     */
    @Contract(pure = true)
    public boolean hasAccess(
            @NotNull ResourceLocation id,
            @NotNull Player player
    ) {
        return this.hasAccess(
                player,
                this.getAccessLevel(id)
        );
    }

    /**
     * Registers an additional independent access level.
     *
     * <p>Registration defines that a machine supports the supplied security
     * option. Registration should normally occur during block entity
     * construction.</p>
     *
     * <p>Registering an access level does not mark the security settings as
     * changed because registration describes the structure of the machine,
     * rather than modifying its saved state.</p>
     *
     * @param id           the unique identifier for the access level
     * @param defaultLevel the default access level
     * @throws IllegalArgumentException if this identifier has already been
     *                                  registered
     */
    public void registerAccessLevel(
            @NotNull ResourceLocation id,
            @NotNull AccessLevel defaultLevel
    ) {
        if (this.defaultAccessLevels.containsKey(id)) {
            throw new IllegalArgumentException("Security access level already registered: " + id);
        }

        this.defaultAccessLevels.put(id, defaultLevel);
        this.accessLevels.put(id, defaultLevel);
    }

    /**
     * {@return whether an additional access level has been registered}
     *
     * @param id the access level identifier
     */
    @Contract(pure = true)
    public boolean hasAccessLevel(@NotNull ResourceLocation id) {
        return this.defaultAccessLevels.containsKey(id);
    }

    /**
     * {@return the default machine access level}
     */
    @Contract(pure = true)
    public @NotNull AccessLevel getAccessLevel() {
        return this.accessLevel;
    }

    /**
     * Returns an additional registered access level.
     *
     * @param id the identifier of the registered access level
     * @return the current access level
     * @throws IllegalArgumentException if the access level has not been
     *                                  registered
     */
    @Contract(pure = true)
    public @NotNull AccessLevel getAccessLevel(
            @NotNull ResourceLocation id
    ) {
        AccessLevel accessLevel = this.accessLevels.get(id);

        if (accessLevel == null) {
            throw new IllegalArgumentException("Unknown security access level: " + id);
        }

        return accessLevel;
    }

    /**
     * {@return an immutable view of all additional registered access levels}
     */
    @Contract(pure = true)
    public @NotNull Map<ResourceLocation, AccessLevel> getAccessLevels() {
        return Collections.unmodifiableMap(this.accessLevels);
    }

    /**
     * Sets the default machine access level.
     *
     * @param accessLevel the access level to set
     */
    public void setAccessLevel(@NotNull AccessLevel accessLevel) {
        if (this.accessLevel != accessLevel) {
            this.accessLevel = accessLevel;
            this.onChanged();
        }
    }

    /**
     * Sets an additional registered access level.
     *
     * @param id          the identifier of the registered access level
     * @param accessLevel the new access level
     * @throws IllegalArgumentException if the access level has not been
     *                                  registered
     */
    public void setAccessLevel(
            @NotNull ResourceLocation id,
            @NotNull AccessLevel accessLevel
    ) {
        AccessLevel previous = this.accessLevels.get(id);

        if (previous == null) {
            throw new IllegalArgumentException("Unknown security access level: " + id);
        }

        if (previous != accessLevel) {
            this.accessLevels.put(id, accessLevel);
            this.onChanged();
        }
    }

    /**
     * Resets an additional registered access level to the default value it was
     * registered with.
     *
     * @param id the identifier of the registered access level
     * @throws IllegalArgumentException if the access level has not been
     *                                  registered
     */
    public void resetAccessLevel(@NotNull ResourceLocation id) {
        AccessLevel defaultLevel = this.defaultAccessLevels.get(id);

        if (defaultLevel == null) {
            throw new IllegalArgumentException("Unknown security access level: " + id);
        }

        this.setAccessLevel(id, defaultLevel);
    }

    /**
     * {@return the uuid of the player that owns the linked machine}
     */
    @Contract(pure = true)
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

        if (!this.accessLevels.isEmpty()) {
            CompoundTag additionalAccessLevels = new CompoundTag();

            for (Map.Entry<ResourceLocation, AccessLevel> entry : this.accessLevels.entrySet()) {

                additionalAccessLevels.putString(entry.getKey().toString(), entry.getValue().getSerializedName());
            }

            nbt.put(NBT_ADDITIONAL_ACCESS_LEVELS, additionalAccessLevels);
        }

        return nbt;
    }

    @Override
    public void readTag(@NotNull CompoundTag tag) {
        if (tag.contains(Constant.Nbt.OWNER)) {
            this.owner = tag.getUUID(Constant.Nbt.OWNER);
        } else {
            this.owner = null;
        }

        if (tag.contains(Constant.Nbt.ACCESS_LEVEL)) {
            this.accessLevel = AccessLevel.fromString(tag.getString(Constant.Nbt.ACCESS_LEVEL));
        } else {
            this.accessLevel = AccessLevel.PUBLIC;
        }

        /*
         * Reset all registered additional access levels to their defaults
         * before applying values from NBT.
         */
        this.accessLevels.clear();
        this.accessLevels.putAll(this.defaultAccessLevels);

        if (tag.contains(NBT_ADDITIONAL_ACCESS_LEVELS, Tag.TAG_COMPOUND)) {
            CompoundTag additionalAccessLevels = tag.getCompound(NBT_ADDITIONAL_ACCESS_LEVELS);

            for (String key : additionalAccessLevels.getAllKeys()) {
                ResourceLocation id = ResourceLocation.tryParse(key);

                if (id == null || !this.defaultAccessLevels.containsKey(id)) {
                    continue;
                }

                String value = additionalAccessLevels.getString(key);

                try {
                    this.accessLevels.put(id, AccessLevel.fromString(value));
                } catch (IllegalArgumentException ignored) {
                    /*
                     * Invalid values fall back to the registered default.
                     */
                }
            }
        }
    }

    @Override
    public void writePacket(@NotNull FriendlyByteBuf buf) {
        buf.writeByte(this.accessLevel.ordinal());

        buf.writeBoolean(this.owner != null);

        if (this.owner != null) {
            buf.writeUUID(this.owner);
        }

        buf.writeVarInt(this.accessLevels.size());

        for (Map.Entry<ResourceLocation, AccessLevel> entry : this.accessLevels.entrySet()) {
            buf.writeUtf(entry.getKey().toString());
            buf.writeByte(entry.getValue().ordinal());
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

        /*
         * Reset registered values before applying the packet.
         */
        this.accessLevels.clear();
        this.accessLevels.putAll(this.defaultAccessLevels);

        int count = buf.readVarInt();

        for (int i = 0; i < count; i++) {
            ResourceLocation id = ResourceLocation.tryParse(buf.readUtf());

            AccessLevel level = AccessLevel.getByOrdinal(buf.readByte());

            if (id != null && this.defaultAccessLevels.containsKey(id)) {
                this.accessLevels.put(id, level);
            }
        }
    }

    @Override
    public void writeDeltaPacket(
            @NotNull FriendlyByteBuf buf,
            SecuritySettings previous
    ) {
        byte ref = 0b0000;

        if (previous.accessLevel != this.accessLevel) {
            ref |= 0b0001;
        }

        if (!Objects.equals(previous.owner, this.owner)) {
            if (this.owner != null) {
                ref |= 0b0010;
            } else {
                ref |= 0b0100;
            }
        }

        Map<ResourceLocation, AccessLevel> changedAccessLevels = new HashMap<>();

        for (Map.Entry<ResourceLocation, AccessLevel> entry : this.accessLevels.entrySet()) {

            AccessLevel previousLevel = previous.accessLevels.get(entry.getKey());

            if (previousLevel != entry.getValue()) {
                changedAccessLevels.put(entry.getKey(), entry.getValue());
            }
        }

        if (!changedAccessLevels.isEmpty()) {
            ref |= 0b1000;
        }

        buf.writeByte(ref);

        if ((ref & 0b0001) != 0) {
            buf.writeByte(this.accessLevel.ordinal());
        }

        if ((ref & 0b0010) != 0) {
            buf.writeUUID(this.owner);
        }

        if ((ref & 0b1000) != 0) {
            buf.writeVarInt(changedAccessLevels.size());

            for (Map.Entry<ResourceLocation, AccessLevel> entry : changedAccessLevels.entrySet()) {
                buf.writeUtf(entry.getKey().toString());
                buf.writeByte(entry.getValue().ordinal());
            }
        }
    }

    @Override
    public void readDeltaPacket(@NotNull FriendlyByteBuf buf) {
        byte ref = buf.readByte();

        if ((ref & 0b0001) != 0) {
            this.accessLevel = AccessLevel.getByOrdinal(buf.readByte());
        }

        if ((ref & 0b0010) != 0) {
            this.owner = buf.readUUID();
        } else if ((ref & 0b0100) != 0) {
            this.owner = null;
        }

        if ((ref & 0b1000) != 0) {
            int count = buf.readVarInt();

            for (int i = 0; i < count; i++) {
                ResourceLocation id = ResourceLocation.tryParse(buf.readUtf());

                AccessLevel level = AccessLevel.getByOrdinal(buf.readByte());

                if (id != null && this.defaultAccessLevels.containsKey(id)) {
                    this.accessLevels.put(id, level);
                }
            }
        }
    }

    @Override
    public SecuritySettings createEquivalent() {
        return new SecuritySettings();
    }

    @Override
    public boolean hasChanged(SecuritySettings previous) {
        return !Objects.equals(previous.owner, this.owner) || previous.accessLevel != this.accessLevel || !Objects.equals(previous.accessLevels, this.accessLevels);
    }

    @Override
    public void copyInto(SecuritySettings other) {
        other.owner = this.owner;
        other.accessLevel = this.accessLevel;

        other.defaultAccessLevels.clear();
        other.defaultAccessLevels.putAll(this.defaultAccessLevels);

        other.accessLevels.clear();
        other.accessLevels.putAll(this.accessLevels);
    }
}