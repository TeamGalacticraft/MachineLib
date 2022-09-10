package dev.galacticraft.api.machine.storage.io;

import dev.galacticraft.impl.machine.storage.io.GroupStorageSelection;
import dev.galacticraft.impl.machine.storage.io.SlotStorageSelection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface StorageSelection {
    @Contract("_ -> new")
    static @NotNull StorageSelection createSlot(int slot) {
        if (slot < 0) throw new IndexOutOfBoundsException();
        return new SlotStorageSelection(slot);
    }

    @Contract("_ -> new")
    static @NotNull StorageSelection createGroup(@NotNull SlotGroup group) {
        return new GroupStorageSelection(group);
    }

    boolean isSlot();

    int getSlot();

    boolean isGroup();

    @NotNull SlotGroup getGroup();
}
