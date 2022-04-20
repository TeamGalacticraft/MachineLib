package dev.galacticraft.impl.transfer;

import dev.galacticraft.api.transfer.StateCachingStorageProvider;
import dev.galacticraft.impl.machine.storage.slot.ResourceSlot;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StateCachingStorageProviderImpl<T> extends SnapshotParticipant<StateCachingStorageProviderImpl.LongBoolPair> implements StateCachingStorageProvider<T> {
    private ResourceSlot<Item, ItemVariant, ItemStack> slot;
    private final ContainerItemContext context;
    private final ItemApiLookup<T, ContainerItemContext> lookup;
    private boolean hasStorage = false;
    private long modCount = -1;

    public StateCachingStorageProviderImpl(ResourceSlot<Item, ItemVariant, ItemStack> slot, ItemApiLookup<T, ContainerItemContext> lookup) {
        this.slot = slot;
        this.context = ContainerItemContext.ofSingleSlot(slot);
        this.lookup = lookup;
    }

    @Nullable
    @Override
    public T getStorage() {
        long version = this.slot.getModCountUnsafe();
        if (this.modCount != version) {
            this.modCount = version;
            T storage = this.context.find(this.lookup);
            this.hasStorage = storage != null;
            return storage;
        }

        return this.hasStorage ? this.context.find(this.lookup) : null;
    }

    @Nullable
    @Override
    public T getStorageTransactionally(@NotNull TransactionContext context) {
        long version = this.slot.getModCountUnsafe();
        if (this.modCount != version) {
            T storage = this.context.find(this.lookup);
            this.updateSnapshots(context);
            this.modCount = version;
            this.hasStorage = storage != null;
            return storage;
        }

        return this.hasStorage ? this.context.find(this.lookup) : null;
    }

    @Contract(pure = true, value = "-> new")
    @Override
    protected @NotNull StateCachingStorageProviderImpl.LongBoolPair createSnapshot() {
        return new LongBoolPair(this.modCount, this.hasStorage);
    }

    @Override
    protected void readSnapshot(StateCachingStorageProviderImpl.@NotNull LongBoolPair snapshot) {
        this.modCount = snapshot.modCount;
        this.hasStorage = snapshot.hasStorage;
    }

    public record LongBoolPair(long modCount, boolean hasStorage) {}
}
