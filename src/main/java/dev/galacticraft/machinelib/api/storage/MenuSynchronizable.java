package dev.galacticraft.machinelib.api.storage;

import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import org.jetbrains.annotations.Nullable;

public interface MenuSynchronizable {
    @Nullable MenuSyncHandler createSyncHandler();
}
