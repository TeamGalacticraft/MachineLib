package dev.galacticraft.machinelib.impl.storage;

import dev.galacticraft.machinelib.api.storage.ItemStorage;

public interface InternalItemStorage extends ItemStorage {
    void markDirty();

    void setModCount(long value);
}
