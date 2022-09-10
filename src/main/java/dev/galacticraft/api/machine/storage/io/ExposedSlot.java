package dev.galacticraft.api.machine.storage.io;

import com.google.common.base.Preconditions;
import dev.galacticraft.api.machine.storage.ResourceStorage;
import dev.galacticraft.impl.machine.storage.io.ExposedStorageSlot;
import dev.galacticraft.impl.machine.storage.io.PlayerExposedStorageSlot;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface ExposedSlot<T, V extends TransferVariant<T>> extends ExposedStorage<T, V>, SingleSlotStorage<V> {
    @Contract("_, _, _ -> new")
    static <T, V extends TransferVariant<T>> @NotNull ExposedSlot<T, V> ofPlayerSlot(@NotNull ResourceStorage<T, V, ?> storage, int slot, boolean insert) {
        Preconditions.checkNotNull(storage);
        return new PlayerExposedStorageSlot<>(storage, slot, insert);
    }

    /**
     * Creates a new storage that restricts insertion or extraction to a specific slot.
     * @param storage The storage to expose.
     * @param slot The slot to expose.
     * @param insert Whether to allow insertion.
     * @param extract Whether to allow extraction.
     * @param <T> The inner type of the storage.
     * @param <V> The {@link TransferVariant} to expose.
     * @return The exposed storage.
     */
    @Contract("_, _, _, _ -> new")
    static <T, V extends TransferVariant<T>> @NotNull ExposedSlot<T, V> ofSlot(@NotNull ResourceStorage<T, V, ?> storage, int slot, boolean insert, boolean extract) {
        Preconditions.checkNotNull(storage);
        return new ExposedStorageSlot<>(storage, slot, insert, extract);
    }
}
