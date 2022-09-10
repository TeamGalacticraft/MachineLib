package dev.galacticraft.impl.machine.storage.io;

import dev.galacticraft.api.machine.storage.ResourceStorage;
import dev.galacticraft.api.machine.storage.io.ExposedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Predicate;

public class ExposedStorageView<T, V extends TransferVariant<T>> implements ExposedStorage<T, V> {
    private final ResourceStorage<T, V, ?> storage;

    public ExposedStorageView(ResourceStorage<T, V, ?> storage) {
        this.storage = storage;
    }

    @Override
    public @NotNull V getResource(int slot) {
        return this.storage.getVariant(slot);
    }

    @Override
    public long getAmount(int slot) {
        return this.storage.getAmount(slot);
    }

    @Override
    public long getCapacity(int slot) {
        return this.storage.getCapacity(slot);
    }

    @Override
    public boolean supportsInsertion() {
        return false;
    }

    @Override
    public long simulateInsert(V resource, long maxAmount, @Nullable TransactionContext transaction) {
        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public long simulateExtract(V resource, long maxAmount, @Nullable TransactionContext transaction) {
        return 0;
    }

    @Override
    public long getVersion() {
        return this.storage.getVersion();
    }

    @Override
    public @NotNull Predicate<V> getFilter(int slot) {
        return this.storage.getFilter(slot);
    }

    @Override
    public long insert(V resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public long extract(V resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public Iterator<StorageView<V>> iterator() {
        return this.storage.iterator();
    }
}
