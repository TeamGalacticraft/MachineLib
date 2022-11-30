package dev.galacticraft.machinelib.impl.storage.slot;

import dev.galacticraft.machinelib.api.storage.ChangeTracking;

public interface InternalChangeTracking extends ChangeTracking {
    void markDirty();

    void setModCount(long modCount);
}
