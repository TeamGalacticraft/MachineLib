package dev.galacticraft.machinelib.impl.fabric.storage;

import dev.galacticraft.machinelib.api.storage.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

public class ItemStorageFabric implements ItemStorage {
    private final Storage<ItemVariant> storage;

    public ItemStorageFabric(Storage<ItemVariant> storage) {
        this.storage = storage;
    }

    @Override
    public int insert(Item item, int amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            int inserted = (int) this.storage.insert(ItemVariant.of(item), amount, transaction);
            transaction.commit();
            return inserted;
        }
    }

    @Override
    public int insert(Item item, CompoundTag tag, int amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            int inserted = (int) this.storage.insert(ItemVariant.of(item, tag), amount, transaction);
            transaction.commit();
            return inserted;
        }
    }

    @Override
    public int simulateInsert(Item item, int amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            return (int) this.storage.insert(ItemVariant.of(item), amount, transaction);
        }
    }

    @Override
    public int simulateInsert(Item item, CompoundTag tag, int amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            return (int) this.storage.insert(ItemVariant.of(item, tag), amount, transaction);
        }
    }

    @Override
    public boolean extract(Item item) {
        try (Transaction transaction = Transaction.openOuter()) {
            boolean b = this.storage.extract(ItemVariant.of(item), 1, transaction) == 1;
            if (b) transaction.commit();
            return b;
        }
    }

    @Override
    public boolean extractExact(Item item, int amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            boolean b = this.storage.extract(ItemVariant.of(item), amount, transaction) == amount;
            if (b) transaction.commit();
            return b;
        }
    }

    @Override
    public int extract(Item item, int amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            int extracted = (int) this.storage.extract(ItemVariant.of(item), amount, transaction);
            transaction.commit();
            return extracted;
        }
    }

    @Override
    public int extract(Item item, CompoundTag tag, int amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            int extracted = (int) this.storage.extract(ItemVariant.of(item, tag), amount, transaction);
            transaction.commit();
            return extracted;
        }
    }

    @Override
    public boolean simulateExtract(Item item) {
        try (Transaction transaction = Transaction.openOuter()) {
            return (int) this.storage.extract(ItemVariant.of(item), 1, transaction) == 1;
        }
    }

    @Override
    public boolean simulateExtractExact(Item item, int amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            return (int) this.storage.extract(ItemVariant.of(item), amount, transaction) == amount;
        }
    }

    @Override
    public int simulateExtract(Item item, int amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            return (int) this.storage.extract(ItemVariant.of(item), amount, transaction);
        }
    }

    @Override
    public int simulateExtract(Item item, CompoundTag tag, int amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            return (int) this.storage.extract(ItemVariant.of(item, tag), amount, transaction);
        }
    }

    @Override
    public long getModCount() {
        return this.storage.getVersion();
    }
}
