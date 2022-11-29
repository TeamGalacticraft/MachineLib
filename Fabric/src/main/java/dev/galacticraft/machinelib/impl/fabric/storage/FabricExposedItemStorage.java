package dev.galacticraft.machinelib.impl.fabric.storage;

import com.google.common.collect.Iterators;
import dev.galacticraft.machinelib.api.storage.ItemStorage;
import dev.galacticraft.machinelib.impl.fabric.storage.slot.FabricExposedItemSlot;
import dev.galacticraft.machinelib.impl.storage.InternalItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class FabricExposedItemStorage extends SnapshotParticipant<Long> implements Storage<ItemVariant> {
    private final ItemStorage storage;
    private final int size;
    private final FabricExposedItemSlot[] slots;
    private final boolean insertion;
    private final boolean extraction;

    public FabricExposedItemStorage(ItemStorage storage) {
        this.storage = storage;
        this.size = storage.size();
        this.slots = new FabricExposedItemSlot[this.size];
        boolean insertion = false;
        boolean extraction = false;
        for (int i = 0; i < this.slots.length; i++) {
            this.slots[i] = new FabricExposedItemSlot(this, storage.getSlot(i), storage.getFilter(i), insertion |= storage.canPlayerInsert(i), extraction |= storage.canExternalExtract(i));
        }
        this.insertion = insertion;
        this.extraction = extraction;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (this.insertion) {
            long available = maxAmount;
            for (int i = 0; i < this.size; i++) {
                available -= this.slots[i].insert(resource, available, transaction);
                if (available == 0) return maxAmount;
            }
            return maxAmount - available;
        }
        return 0;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (this.extraction) {
            long requested = maxAmount;
            for (int i = 0; i < this.size; i++) {
                requested -= this.slots[i].extract(resource, requested, transaction);
                if (requested == 0) return maxAmount;
            }
            return maxAmount - requested;
        }
        return 0;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return Iterators.forArray(this.slots);
    }

    @Override
    public boolean supportsInsertion() {
        return this.insertion;
    }

    @Override
    public boolean supportsExtraction() {
        return this.extraction;
    }

    @Override
    public @Nullable StorageView<ItemVariant> exactView(ItemVariant resource) {
        return null;
    }

    @Override
    public long getVersion() {
        return this.storage.getModCount();
    }

    @Override
    public Long createSnapshot() {
        return this.storage.getModCount();
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        ((InternalItemStorage) this.storage).setModCount(snapshot);
    }
}
