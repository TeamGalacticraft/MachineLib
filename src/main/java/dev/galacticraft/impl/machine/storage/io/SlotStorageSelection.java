package dev.galacticraft.impl.machine.storage.io;

import dev.galacticraft.api.machine.storage.io.SlotGroup;
import dev.galacticraft.api.machine.storage.io.StorageSelection;
import org.jetbrains.annotations.NotNull;

public record SlotStorageSelection(int slot) implements StorageSelection {
    @Override
    public boolean isSlot() {
        return true;
    }

    @Override
    public int getSlot() {
        return this.slot;
    }

    @Override
    public boolean isGroup() {
        return false;
    }

    @Override
    public @NotNull SlotGroup getGroup() {
        throw new UnsupportedOperationException();
    }
}
