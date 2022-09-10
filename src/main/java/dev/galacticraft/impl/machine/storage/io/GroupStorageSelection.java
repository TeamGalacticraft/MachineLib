package dev.galacticraft.impl.machine.storage.io;

import dev.galacticraft.api.machine.storage.io.SlotGroup;
import dev.galacticraft.api.machine.storage.io.StorageSelection;
import org.jetbrains.annotations.NotNull;

public record GroupStorageSelection(@NotNull SlotGroup group) implements StorageSelection {
    @Override
    public boolean isSlot() {
        return false;
    }

    @Override
    public int getSlot() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isGroup() {
        return true;
    }

    @Override
    public @NotNull SlotGroup getGroup() {
        return this.group;
    }
}
