package dev.galacticraft.machinelib.impl.fabric.storage;

import com.google.common.collect.Iterators;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.impl.fabric.storage.slot.FabricExposedFluidTank;
import dev.galacticraft.machinelib.impl.storage.slot.InternalChangeTracking;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class FabricExposedFluidStorage extends SnapshotParticipant<Long> implements Storage<FluidVariant> {
    private final InternalChangeTracking storage;
    private final int size;
    private final FabricExposedFluidTank[] slots;
    private final boolean insertion;
    private final boolean extraction;

    public FabricExposedFluidStorage(MachineFluidStorage storage, int[] slots, boolean insertion, boolean extraction) {
        this.storage = storage;
        this.size = slots.length;
        this.slots = new FabricExposedFluidTank[slots.length];
        boolean insert = false;
        boolean extract = false;
        for (int i = 0; i < slots.length; i++) {
            this.slots[i] = new FabricExposedFluidTank(this, storage.getTank(i), storage.getFilter(i), insert |= (insertion && storage.canExternalInsert(i)), extract |= (extraction && storage.canExternalExtract(i)));
        }
        this.insertion = insert;
        this.extraction = extract;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
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
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
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
    public Iterator<StorageView<FluidVariant>> iterator() {
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
    public @Nullable StorageView<FluidVariant> exactView(FluidVariant resource) {
        return null;
    }

    @Override
    public long getVersion() {
        return this.storage.getModCount();
    }

    @Override
    protected Long createSnapshot() {
        return this.storage.getModCount();
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        this.storage.setModCount(snapshot);
    }
}
