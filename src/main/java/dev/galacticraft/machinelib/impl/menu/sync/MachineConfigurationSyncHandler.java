package dev.galacticraft.machinelib.impl.menu.sync;

import dev.galacticraft.machinelib.api.machine.MachineConfiguration;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.RedstoneActivation;
import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import dev.galacticraft.machinelib.impl.MachineLib;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class MachineConfigurationSyncHandler implements MenuSyncHandler {
    private final MachineConfiguration configuration;
    private final MenuSyncHandler ioConfig;
    private final MenuSyncHandler security;
    private MachineStatus status;
    private RedstoneActivation redstone;

    public MachineConfigurationSyncHandler(MachineConfiguration configuration) {
        this.ioConfig = configuration.getIOConfiguration().createSyncHandler();
        this.security = configuration.getSecurity().createSyncHandler();
        this.status = configuration.getStatus();
        this.redstone = configuration.getRedstoneActivation();
        this.configuration = configuration;
    }

    @Override
    public boolean needsSyncing() {
        return this.ioConfig.needsSyncing() || this.security.needsSyncing() || this.status != configuration.getStatus() || this.redstone != configuration.getRedstoneActivation();
    }

    @Override
    public void sync(@NotNull FriendlyByteBuf buf) {
        byte ref = 0b0000;
        if (this.ioConfig.needsSyncing()) ref |= 0b0001;
        if (this.security.needsSyncing()) ref |= 0b0010;
        if (this.status != configuration.getStatus()) ref |= 0b0100;
        if (this.redstone != configuration.getRedstoneActivation()) ref |= 0b1000;

        buf.writeByte(ref);

        if (this.ioConfig.needsSyncing()) {
            this.ioConfig.sync(buf);
        }
        if (this.security.needsSyncing()) {
            this.security.sync(buf);
        }
        if (this.status != configuration.getStatus()) {
            this.status = configuration.getStatus();
            buf.writeResourceLocation(MachineLib.MACHINE_STATUS_REGISTRY.getKey(configuration.getStatus()));
        }
        if (this.redstone != configuration.getRedstoneActivation()) {
            this.redstone = configuration.getRedstoneActivation();
            buf.writeByte(configuration.getRedstoneActivation().ordinal());
        }
    }

    @Override
    public void read(@NotNull FriendlyByteBuf buf) {
        byte ref = buf.readByte();
        if ((ref & 0b0001) != 0) {
            this.ioConfig.read(buf);
        }
        if ((ref & 0b0010) != 0) {
            this.security.read(buf);
        }
        if ((ref & 0b0100) != 0) {
            this.status = MachineLib.MACHINE_STATUS_REGISTRY.get(buf.readResourceLocation());
        }
        if ((ref & 0b1000) != 0) {
            this.redstone = RedstoneActivation.VALUES[buf.readByte()];
        }
    }
}
