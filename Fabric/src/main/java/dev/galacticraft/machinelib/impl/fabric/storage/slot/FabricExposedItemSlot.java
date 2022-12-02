package dev.galacticraft.machinelib.impl.fabric.storage.slot;

import com.google.common.collect.Iterators;
import dev.galacticraft.machinelib.api.storage.slot.MachineItemSlot;
import dev.galacticraft.machinelib.api.util.Maths;
import dev.galacticraft.machinelib.impl.fabric.storage.FabricExposedItemStorage;
import dev.galacticraft.machinelib.impl.storage.ResourceFilter;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;

public class FabricExposedItemSlot extends SnapshotParticipant<FabricExposedItemSlot.SlotSnapshot> implements Storage<ItemVariant>, StorageView<ItemVariant> {
    private final FabricExposedItemStorage storage;
    private final MachineItemSlot slot;
    private final ResourceFilter<Item> filter;
    private final boolean insertion;
    private final boolean extraction;

    public FabricExposedItemSlot(FabricExposedItemStorage storage, MachineItemSlot slot, ResourceFilter<Item> filter, boolean insertion, boolean extraction) {
        this.storage = storage;
        this.slot = slot;
        this.filter = filter;
        this.insertion = insertion;
        this.extraction = extraction;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (this.insertion && this.filter.matches(resource.getItem(), resource.getNbt())) {
            this.updateSnapshots(transaction);
            this.storage.updateSnapshots(transaction);
            return this.slot.insertCopyNbt(resource.getItem(), resource.getNbt(), Maths.floorLong(maxAmount));
        }
        return 0;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (this.extraction) {
            this.updateSnapshots(transaction);
            this.storage.updateSnapshots(transaction);
            return this.slot.extract(resource.getItem(), resource.getNbt(), Maths.floorLong(maxAmount));
        }
        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        return this.slot.isEmpty();
    }

    @Override
    public ItemVariant getResource() {
        return ItemVariant.of(this.slot.getStack());
    }

    @Override
    public long getAmount() {
        return this.slot.getAmount();
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
    public long getVersion() {
        return this.slot.getModCount();
    }

    @Override
    public long getCapacity() {
        return this.slot.getCurrentCapacity();
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return Iterators.singletonIterator(this);
    }

    @Override
    protected SlotSnapshot createSnapshot() {
        SlotSnapshot slotSnapshot = new SlotSnapshot(this.slot.getStack(), this.slot.getModCount());
        this.slot.silentSetStack(this.slot.copyStack()); // set stack to a copy
        return slotSnapshot;
    }

    @Override
    protected void readSnapshot(SlotSnapshot snapshot) {
        this.slot.silentSetStack(snapshot.stack);
        this.slot.setModCount(snapshot.modCount);
    }

    public record SlotSnapshot(ItemStack stack, long modCount) {}
}
