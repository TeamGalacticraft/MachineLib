package dev.galacticraft.machinelib.impl.storage;

import dev.galacticraft.machinelib.api.storage.SlottedItemStorage;
import dev.galacticraft.machinelib.impl.storage.slot.InternalChangeTracking;
import dev.galacticraft.machinelib.impl.storage.slot.InternalItemSlot;

public interface InternalSlottedItemStorage extends SlottedItemStorage, InternalChangeTracking {
    @Override
    InternalItemSlot getSlot(int slot);
}
