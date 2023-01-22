package dev.galacticraft.machinelib.impl.menu.sync;

import dev.galacticraft.machinelib.api.machine.AccessLevel;
import dev.galacticraft.machinelib.api.machine.SecuritySettings;
import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class SecuritySettingsSyncHandler implements MenuSyncHandler {
    private final SecuritySettings settings;

    private @Nullable UUID prevOwner;
    private @Nullable String prevUsername;
    private @Nullable ResourceLocation prevTeam;
    private @NotNull AccessLevel prevAccessLevel;
    private @Nullable String prevTeamName;

    public SecuritySettingsSyncHandler(SecuritySettings settings) {
        this.settings = settings;

        this.prevOwner = settings.getOwner();
        this.prevUsername = settings.getUsername();
        this.prevTeam = settings.getTeam();
        this.prevAccessLevel = settings.getAccessLevel();
        this.prevTeamName = settings.getTeamName();
    }

    @Override
    public boolean needsSyncing() {
        return !Objects.equals(this.prevOwner, settings.getOwner()) ||
                !Objects.equals(this.prevUsername, settings.getUsername()) ||
                !Objects.equals(this.prevTeam, settings.getTeam()) ||
                this.prevAccessLevel != settings.getAccessLevel() ||
                !Objects.equals(this.prevTeamName, settings.getTeamName());
    }

    @Override
    public void sync(@NotNull FriendlyByteBuf buf) {
        byte ref = 0b00000;
        byte nullRef = 0b00000;
        if (!Objects.equals(this.prevOwner, settings.getOwner())) {
            ref |= 0b00001;
            if (settings.getOwner() == null) nullRef |= 0b00001;
        }
        if (!Objects.equals(this.prevUsername, settings.getUsername())) {
            ref |= 0b00010;
            if (settings.getUsername() == null) nullRef |= 0b00010;
        }
        if (!Objects.equals(this.prevTeam, settings.getTeam())) {
            ref |= 0b00100;
            if (settings.getTeam() == null) nullRef |= 0b00100;
        }
        if (this.prevAccessLevel != settings.getAccessLevel()) {
            ref |= 0b01000;
        }
        if (!Objects.equals(this.prevTeamName, settings.getTeamName())) {
            ref |= 0b10000;
            if (settings.getTeam() == null) nullRef |= 0b10000;
        }

        buf.writeByte(ref);
        buf.writeByte(nullRef);

        if (!Objects.equals(this.prevOwner, settings.getOwner())) {
            this.prevOwner = settings.getOwner();
            if (this.prevOwner != null) {
                buf.writeUUID(this.prevOwner);
            }
        }
        if (!Objects.equals(this.prevUsername, settings.getUsername())) {
            this.prevUsername = settings.getUsername();
            if (this.prevUsername != null) {
                buf.writeUtf(this.prevUsername);
            }
        }
        if (!Objects.equals(this.prevTeam, settings.getTeam())) {
            this.prevTeam = settings.getTeam();
            if (this.prevTeam != null) {
                buf.writeResourceLocation(this.prevTeam);
            }
        }
        if (this.prevAccessLevel != settings.getAccessLevel()) {
            this.prevAccessLevel = settings.getAccessLevel();
            buf.writeByte(this.prevAccessLevel.ordinal());
        }
        if (!Objects.equals(this.prevTeamName, settings.getTeamName())) {
            this.prevTeamName = settings.getTeamName();
            if (this.prevTeamName != null) {
                buf.writeUtf(this.prevTeamName);
            }
        }
    }

    @Override
    public void read(@NotNull FriendlyByteBuf buf) {
        byte ref = buf.readByte();
        byte nullRef = buf.readByte();
        ref ^= nullRef;

        if ((ref & 0b00001) != 0) {
            this.settings.setOwner(buf.readUUID(), null);
        } else if ((nullRef & 0b00001) != 0) {
            this.settings.setOwner(null, null);
        }
        if ((ref & 0b00010) != 0) {
            this.settings.setUsername(buf.readUtf());
        } else if ((nullRef & 0b00010) != 0) {
            this.settings.setUsername(null);
        }
        if ((ref & 0b00100) != 0) {
            this.settings.setTeam(buf.readResourceLocation(), this.settings.getTeamName());
        } else if ((nullRef & 0b00100) != 0) {
            this.settings.setTeam(null, null);
        }
        if ((ref & 0b01000) != 0) {
            this.settings.setAccessLevel(AccessLevel.getByOrdinal(buf.readByte()));
        }
        if ((ref & 0b10000) != 0) {
            this.settings.setTeam(this.settings.getTeam(), buf.readUtf());
        } else if ((nullRef & 0b10000) != 0) {
            this.settings.setTeam(this.settings.getTeam(), null);
        }
    }
}
