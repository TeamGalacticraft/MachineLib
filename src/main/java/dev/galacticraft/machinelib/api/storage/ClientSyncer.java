package dev.galacticraft.machinelib.api.storage;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public interface ClientSyncer {
    void writePacket(@NotNull FriendlyByteBuf buf);
    void readPacket(@NotNull FriendlyByteBuf buf);

}
