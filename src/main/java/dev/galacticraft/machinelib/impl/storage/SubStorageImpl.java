package dev.galacticraft.machinelib.impl.storage;

import com.google.common.collect.Iterators;
import dev.galacticraft.machinelib.api.storage.SlottedStorageAccess;
import dev.galacticraft.machinelib.api.storage.StorageAccess;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class SubStorageImpl<Resource, Slot extends StorageAccess<Resource>> implements SlottedStorageAccess<Resource, Slot> {
    private final SlottedStorageAccess<Resource, Slot> parent;
    private final int start;
    private final int len;

    public SubStorageImpl(SlottedStorageAccess<Resource, Slot> parent, int start, int len) {
        this.parent = parent;
        this.start = start;
        this.len = len;
    }

    @Override
    public long getModifications() {
        return this.parent.getModifications(); // todo: track manually?
    }

    @Override
    public int size() {
        return this.len;
    }

    @Override
    public @Nullable Resource getResource(int slot) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.getResource(this.start + slot);
    }

    @Override
    public long getAmount(int slot) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.getAmount(this.start + slot);
    }

    @Override
    public @Nullable CompoundTag getTag(int slot) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.getTag(this.start + slot);
    }

    @Override
    public @Nullable CompoundTag copyTag(int slot) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.copyTag(this.start + slot);
    }

    @Override
    public long getCapacity(int slot) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.getCapacity(this.start + slot);
    }

    @Override
    public long getCapacityFor(int slot, @NotNull Resource resource) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.getCapacityFor(this.start + slot, resource);
    }

    @Override
    public long getRealCapacity(int slot) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.getRealCapacity(this.start + slot);
    }

    @Override
    public boolean isEmpty(int slot) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.isEmpty(this.start + slot);
    }

    @Override
    public boolean isFull(int slot) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.isFull(this.start + slot);
    }

    @Override
    public boolean canInsert(int slot, @NotNull Resource resource) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.canInsert(this.start + slot, resource);
    }

    @Override
    public boolean canInsert(int slot, @NotNull Resource resource, @Nullable CompoundTag tag) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.canInsert(this.start + slot, resource, tag);
    }

    @Override
    public boolean canInsert(int slot, @NotNull Resource resource, long amount) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.canInsert(this.start + slot, resource, amount);
    }

    @Override
    public boolean canInsert(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.canInsert(this.start + slot, resource, tag, amount);
    }

    @Override
    public long tryInsert(int slot, @NotNull Resource resource, long amount) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.tryInsert(this.start + slot, resource, amount);
    }

    @Override
    public long tryInsert(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.tryInsert(this.start + slot, resource, tag, amount);
    }

    @Override
    public long insert(int slot, @NotNull Resource resource, long amount) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.insert(this.start + slot, resource, amount);
    }

    @Override
    public long insert(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.insert(this.start + slot, resource, tag, amount);
    }

    @Override
    public boolean contains(int slot, @NotNull Resource resource) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.contains(this.start + slot, resource);
    }

    @Override
    public boolean contains(int slot, @NotNull Resource resource, @Nullable CompoundTag tag) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.contains(this.start + slot, resource, tag);
    }

    @Override
    public boolean canExtract(int slot, long amount) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.canExtract(this.start + slot, amount);
    }

    @Override
    public boolean canExtract(int slot, @NotNull Resource resource, long amount) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.canExtract(this.start + slot, resource, amount);
    }

    @Override
    public boolean canExtract(int slot, @NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.canExtract(this.start + slot, resource, tag, amount);
    }

    @Override
    public long tryExtract(int slot, long amount) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.tryExtract(this.start + slot, amount);
    }

    @Override
    public long tryExtract(int slot, @Nullable Resource resource, long amount) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.tryExtract(this.start + slot, resource, amount);
    }

    @Override
    public long tryExtract(int slot, @Nullable Resource resource, @Nullable CompoundTag tag, long amount) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.tryExtract(this.start + slot, resource, tag, amount);
    }

    @Override
    public boolean extractOne(int slot) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.extractOne(this.start + slot);
    }

    @Override
    public boolean extractOne(int slot, @Nullable Resource resource) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.extractOne(this.start + slot, resource);
    }

    @Override
    public boolean extractOne(int slot, @Nullable Resource resource, @Nullable CompoundTag tag) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.extractOne(this.start + slot, resource, tag);
    }

    @Override
    public long extract(int slot, long amount) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.extract(this.start + slot, amount);
    }

    @Override
    public long extract(int slot, @Nullable Resource resource, long amount) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.extract(this.start + slot, resource, amount);
    }

    @Override
    public long extract(int slot, @Nullable Resource resource, @Nullable CompoundTag tag, long amount) {
        if (slot >= this.len) throw new IndexOutOfBoundsException(slot);
        return this.parent.extract(this.start + slot, resource, tag, amount);
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < this.len; i++) {
            if (!this.parent.isEmpty(this.start + i)) return false;
        }
        return true;
    }

    @Override
    public boolean isFull() {
        for (int i = 0; i < this.len; i++) {
            if (!this.parent.isFull(this.start + i)) return false;
        }
        return true;
    }

    @Override
    public boolean canInsert(@NotNull Resource resource) {
        for (int i = 0; i < this.len; i++) {
            if (this.parent.canInsert(this.start + i, resource)) return true;
        }
        return false;
    }

    @Override
    public boolean canInsert(@NotNull Resource resource, @Nullable CompoundTag tag) {
        for (int i = 0; i < this.len; i++) {
            if (this.parent.canInsert(this.start + i, resource, tag)) return true;
        }
        return false;
    }

    @Override
    public boolean canInsert(@NotNull Resource resource, long amount) {
        long inserted = 0;
        for (int i = 0; i < this.len; i++) {
            inserted += this.parent.tryInsert(this.start + i, resource, amount - inserted);
            if (inserted == amount) return true;
        }
        return inserted == amount;
    }

    @Override
    public boolean canInsert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        long inserted = 0;
        for (int i = 0; i < this.len; i++) {
            inserted += this.parent.tryInsert(this.start + i, resource, tag, amount - inserted);
            if (inserted == amount) return true;
        }
        return inserted == amount;
    }

    @Override
    public long tryInsert(@NotNull Resource resource, long amount) {
        long inserted = 0;
        for (int i = 0; i < this.len; i++) {
            inserted += this.parent.tryInsert(this.start + i, resource, amount - inserted);
            if (inserted == amount) return inserted;
        }
        return inserted;
    }

    @Override
    public long tryInsert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        long inserted = 0;
        for (int i = 0; i < this.len; i++) {
            inserted += this.parent.tryInsert(this.start + i, resource, tag, amount - inserted);
            if (inserted == amount) return inserted;
        }
        return inserted;
    }

    @Override
    public long insert(@NotNull Resource resource, long amount) {
        long inserted = 0;
        for (int i = 0; i < this.len; i++) {
            inserted += this.parent.insert(this.start + i, resource, amount - inserted);
            if (inserted == amount) return inserted;
        }
        return inserted;
    }

    @Override
    public long insert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        long inserted = 0;
        for (int i = 0; i < this.len; i++) {
            inserted += this.parent.insert(this.start + i, resource, tag, amount - inserted);
            if (inserted == amount) return inserted;
        }
        return inserted;
    }

    @Override
    public long insertMatching(@NotNull Resource resource, long amount) {
        long inserted = 0;
        for (int i = 0; i < this.len; i++) {
            if (this.parent.contains(this.start + i, resource)) {
                inserted += this.parent.insert(this.start + i, resource, amount - inserted);
                if (inserted == amount) return inserted;
            }
        }

        for (int i = 0; i < this.len; i++) {
            inserted += this.parent.insert(this.start + i, resource, amount - inserted);
            if (inserted == amount) return inserted;
        }
        return inserted;
    }

    @Override
    public long insertMatching(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        long inserted = 0;
        for (int i = 0; i < this.len; i++) {
            if (this.parent.contains(this.start + i, resource)) {
                inserted += this.parent.insert(this.start + i, resource, tag, amount - inserted);
                if (inserted == amount) return inserted;
            }
        }

        for (int i = 0; i < this.len; i++) {
            inserted += this.parent.insert(this.start + i, resource, tag, amount - inserted);
            if (inserted == amount) return inserted;
        }
        return inserted;
    }

    @Override
    public boolean contains(@NotNull Resource resource) {
        for (int i = 0; i < this.len; i++) {
            if (this.parent.contains(this.start + i, resource)) return true;
        }
        return false;
    }

    @Override
    public boolean contains(@NotNull Resource resource, @Nullable CompoundTag tag) {
        for (int i = 0; i < this.len; i++) {
            if (this.parent.contains(this.start + i, resource, tag)) return true;
        }
        return false;
    }

    @Override
    public boolean canExtract(@NotNull Resource resource, long amount) {
        long extracted = 0;
        for (int i = 0; i < this.len; i++) {
            extracted += this.parent.tryExtract(this.start + i, resource, amount - extracted);
            if (extracted == amount) return true;
        }
        return extracted == amount;
    }

    @Override
    public boolean canExtract(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        long extracted = 0;
        for (int i = 0; i < this.len; i++) {
            extracted += this.parent.tryExtract(this.start + i, resource, tag, amount - extracted);
            if (extracted == amount) return true;
        }
        return extracted == amount;
    }

    @Override
    public long tryExtract(@NotNull Resource resource, long amount) {
        long extracted = 0;
        for (int i = 0; i < this.len; i++) {
            extracted += this.parent.tryExtract(this.start + i, resource, amount - extracted);
            if (extracted == amount) return extracted;
        }
        return extracted;
    }

    @Override
    public long tryExtract(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        long extracted = 0;
        for (int i = 0; i < this.len; i++) {
            extracted += this.parent.tryExtract(this.start + i, resource, tag, amount - extracted);
            if (extracted == amount) return extracted;
        }
        return extracted;
    }

    @Override
    public boolean extractOne(@NotNull Resource resource) {
        for (int i = 0; i < this.len; i++) {
            if (this.parent.extractOne(this.start + i, resource)) return true;
        }
        return false;
    }

    @Override
    public boolean extractOne(@NotNull Resource resource, @Nullable CompoundTag tag) {
        for (int i = 0; i < this.len; i++) {
            if (this.parent.extractOne(this.start + i, resource, tag)) return true;
        }
        return false;
    }

    @Override
    public long extract(@NotNull Resource resource, long amount) {
        long extracted = 0;
        for (int i = 0; i < this.len; i++) {
            extracted += this.parent.extract(this.start + i, resource, amount - extracted);
            if (extracted == amount) return extracted;
        }
        return extracted;
    }

    @Override
    public long extract(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        long extracted = 0;
        for (int i = 0; i < this.len; i++) {
            extracted += this.parent.extract(this.start + i, resource, tag, amount - extracted);
            if (extracted == amount) return extracted;
        }
        return extracted;
    }

    @Override
    public long insert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context) {
        long inserted = 0;
        for (Slot slot : this) {
            inserted += slot.insert(resource, tag, amount - inserted);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public long extract(@Nullable Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context) {
        long extracted = 0;
        for (Slot slot : this) {
            extracted += slot.extract(resource, tag, amount - extracted, context);
            if (extracted == amount) break;
        }
        return extracted;
    }

    @NotNull
    @Override
    public Iterator<Slot> iterator() {
        Iterator<Slot> iterator = this.parent.iterator();
        Iterators.advance(iterator, this.start);
        return Iterators.limit(iterator, this.len);
    }
}
