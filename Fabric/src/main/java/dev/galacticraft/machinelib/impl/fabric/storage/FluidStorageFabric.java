package dev.galacticraft.machinelib.impl.fabric.storage;

import dev.galacticraft.machinelib.api.storage.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

public class FluidStorageFabric implements FluidStorage {
    private final Storage<FluidVariant> storage;

    public FluidStorageFabric(Storage<FluidVariant> storage) {
        this.storage = storage;
    }

    @Override
    public long insert(Fluid fluid, long amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = this.storage.insert(FluidVariant.of(fluid), amount, transaction);
            transaction.commit();
            return inserted;
        }
    }

    @Override
    public long insert(Fluid fluid, CompoundTag tag, long amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = this.storage.insert(FluidVariant.of(fluid, tag), amount, transaction);
            transaction.commit();
            return inserted;
        }
    }

    @Override
    public long simulateInsert(Fluid fluid, long amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            return this.storage.insert(FluidVariant.of(fluid), amount, transaction);
        }
    }

    @Override
    public long simulateInsert(Fluid fluid, CompoundTag tag, long amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            return this.storage.insert(FluidVariant.of(fluid, tag), amount, transaction);
        }
    }

    @Override
    public boolean extract(Fluid fluid) {
        try (Transaction transaction = Transaction.openOuter()) {
            boolean b = this.storage.extract(FluidVariant.of(fluid), 1, transaction) == 1;
            if (b) transaction.commit();
            return b;
        }
    }

    @Override
    public boolean extractExact(Fluid fluid, long amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            boolean b = this.storage.extract(FluidVariant.of(fluid), amount, transaction) == amount;
            if (b) transaction.commit();
            return b;
        }
    }

    @Override
    public long extract(Fluid fluid, long amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            long extracted = this.storage.extract(FluidVariant.of(fluid), amount, transaction);
            transaction.commit();
            return extracted;
        }
    }

    @Override
    public long extract(Fluid fluid, CompoundTag tag, long amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            long extracted = this.storage.extract(FluidVariant.of(fluid, tag), amount, transaction);
            transaction.commit();
            return extracted;
        }
    }

    @Override
    public boolean simulateExtract(Fluid fluid) {
        try (Transaction transaction = Transaction.openOuter()) {
            return this.storage.extract(FluidVariant.of(fluid), 1, transaction) == 1;
        }
    }

    @Override
    public boolean simulateExtractExact(Fluid fluid, long amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            return this.storage.extract(FluidVariant.of(fluid), amount, transaction) == amount;
        }
    }

    @Override
    public long simulateExtract(Fluid fluid, long amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            return this.storage.extract(FluidVariant.of(fluid), amount, transaction);
        }
    }

    @Override
    public long simulateExtract(Fluid fluid, CompoundTag tag, long amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            return this.storage.extract(FluidVariant.of(fluid, tag), amount, transaction);
        }
    }

    @Override
    public long getModCount() {
        return this.storage.getVersion();
    }
}
