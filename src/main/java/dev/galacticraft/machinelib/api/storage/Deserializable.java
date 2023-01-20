package dev.galacticraft.machinelib.api.storage;

import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public interface Deserializable<T extends Tag> extends Serializable<T> {
    void readTag(@NotNull T tag);

    void readPacket(@NotNull FriendlyByteBuf buf);
}
