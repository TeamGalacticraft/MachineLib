package dev.galacticraft.machinelib.impl.menu.sync;

import dev.galacticraft.machinelib.api.block.face.BlockFace;
import dev.galacticraft.machinelib.api.machine.MachineIOConfig;
import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class MachineIOConfigSyncHandler implements MenuSyncHandler {
    private final MenuSyncHandler[] syncHandlers = new MenuSyncHandler[6];

    public MachineIOConfigSyncHandler(MachineIOConfig config) {
        BlockFace[] values = BlockFace.values();
        for (int i = 0; i < values.length; i++) {
            this.syncHandlers[i] = config.get(values[i]).createSyncHandler();
        }
     }
    @Override
    public boolean needsSyncing() {
        for (MenuSyncHandler syncHandler : this.syncHandlers) {
            if (syncHandler.needsSyncing()) return true;
        }
        return false;
    }

    @Override
    public void sync(@NotNull FriendlyByteBuf buf) {
        byte total = 0;
        for (MenuSyncHandler syncHandler : this.syncHandlers) {
            if (syncHandler.needsSyncing()) total++;
        }
        buf.writeByte(total);

        for (byte i = 0; i < 6; i++) {
            MenuSyncHandler syncHandler = this.syncHandlers[i];
            if (syncHandler.needsSyncing()) {
                buf.writeByte(i);
                syncHandler.sync(buf);
            }
        }
    }

    @Override
    public void read(@NotNull FriendlyByteBuf buf) {
        byte total = buf.readByte();
        for (byte i = 0; i < total; i++) {
            byte b = buf.readByte();
            this.syncHandlers[b].sync(buf);
        }
    }
}
