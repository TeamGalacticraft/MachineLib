package dev.galacticraft.machinelib.impl.fabric.storage.slot;

import dev.galacticraft.machinelib.api.component.ItemContext;
import dev.galacticraft.machinelib.api.storage.slot.MachineItemSlot;
import dev.galacticraft.machinelib.api.util.Maths;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

import java.util.Collections;
import java.util.List;

public class FabricMachineItemContext extends SnapshotParticipant<FullSlotSnapshot> implements SingleSlotStorage<ItemVariant>, ContainerItemContext, ItemContext {
    private final MachineItemSlot slot;

    public FabricMachineItemContext(MachineItemSlot slot) {
        this.slot = slot;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        this.updateSnapshots(transaction);
        return this.slot.insert(resource.getItem(), resource.getNbt(), Maths.floorLong(maxAmount));
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        this.updateSnapshots(transaction);
        return this.slot.insert(resource.getItem(), resource.getNbt(), Maths.floorLong(maxAmount));
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
    public long getCapacity() {
        return this.slot.getCapacity();
    }

    @Override
    public SingleSlotStorage<ItemVariant> getMainSlot() {
        return this;
    }

    @Override
    public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
        return 0;
    }

    @Override
    public List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
        return Collections.emptyList();
    }

    @Override
    protected FullSlotSnapshot createSnapshot() {
        FullSlotSnapshot slotSnapshot = new FullSlotSnapshot(this.slot.getStack(), this.slot.getModCount(), this.slot.getBackingStorage().getModCount());
        this.slot.silentSetStack(this.slot.copyStack()); // set stack to a cop;
        return slotSnapshot;
    }

    @Override
    protected void readSnapshot(FullSlotSnapshot snapshot) {
        this.slot.silentSetStack(snapshot.stack());
        this.slot.setModCount(snapshot.slotModCount());
        this.slot.getBackingStorage().setModCount(snapshot.storageModCount());
    }
}
