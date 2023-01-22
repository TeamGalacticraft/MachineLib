package dev.galacticraft.machinelib.impl.menu.sync;

import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class MachineEnergyStorageSyncHandler implements MenuSyncHandler {
    private final MachineEnergyStorage storage;
    private long prevValue;

    public MachineEnergyStorageSyncHandler(MachineEnergyStorage storage) {
        this.storage = storage;
        this.prevValue = storage.getAmount();
    }

    @Override
    public boolean needsSyncing() {
        return this.prevValue != this.storage.getAmount();
    }

    @Override
    public void sync(@NotNull FriendlyByteBuf buf) {
        this.prevValue = this.storage.getAmount();
        buf.writeLong(this.storage.getAmount());
    }

    @Override
    public void read(@NotNull FriendlyByteBuf buf) {
        this.storage.setEnergyUnsafe(buf.readLong());
    }
}
